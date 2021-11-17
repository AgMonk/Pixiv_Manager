package com.gin.pixiv_manager.module.pixiv.utils.pixiv.request;

import com.alibaba.fastjson.JSONObject;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.params.PixivParamsBookmarksAdd;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res.*;
import com.gin.pixiv_manager.sys.exception.BusinessException;
import com.gin.pixiv_manager.sys.utils.StringUtils;
import com.gin.pixiv_manager.sys.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pixiv请求
 * @author bx002
 */
@Slf4j
public class PixivRequest {

    public static <T> T get(String url, HashMap<String, Object> params, String cookie, Class<T> clazz) throws IOException {
        String requestUrl = url;
        long start = System.currentTimeMillis();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            if (params != null && params.size() > 0) {
                final String param = params.keySet().stream()
                        .map(i -> i + "=" + URLEncoder.encode(String.valueOf(params.get(i)), StandardCharsets.UTF_8))
                        .collect(Collectors.joining("&"));
                requestUrl += "?" + param;
            }
            log.info("请求开始 url = {}", requestUrl);
            HttpGet httpGet = new HttpGet(requestUrl);
            httpGet.addHeader("cookie", cookie);
            String finalRequestUrl = requestUrl;
            String responseBody = httpClient.execute(httpGet, httpResponse -> {
                int status = httpResponse.getStatusLine().getStatusCode();
                if (status < 200 || status >= 300) {
                    // ... handle unsuccessful request
                    if (status == 504) {
                        throw new BusinessException(5040, "网络错误 等待重试");
                    }
                }
                log.info("请求结束 status = {} url = {}", status, finalRequestUrl);
                HttpEntity entity = httpResponse.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            });
            long end = System.currentTimeMillis();
            log.info("请求完成 耗时 {} url = {}", TimeUtils.timeCost(start, end), requestUrl);
            // ... do something with response
            return JSONObject.parseObject(responseBody, clazz);
        } catch (IOException e) {
            // ... handle IO exception
            e.printStackTrace();
            throw e;
        }
    }

    public static <T> T post(String url, String cookie, String token, Object json, HashMap<String, Object> formData, Class<T> clazz) throws IOException {
        long start = System.currentTimeMillis();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.addHeader("cookie", cookie);
            httpPost.addHeader("x-csrf-token", token);
            if (json != null) {
                httpPost.setEntity(new StringEntity(JSONObject.toJSONString(json), ContentType.APPLICATION_JSON));
            } else if (formData != null) {
                final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                formData.forEach((k, v) -> builder.addPart(k, new StringBody(String.valueOf(v), ContentType.TEXT_PLAIN)));
                httpPost.setEntity(builder.build());
            }
            log.info("请求开始 url = {}", url);
            String responseBody = httpClient.execute(httpPost, httpResponse -> {
                int status = httpResponse.getStatusLine().getStatusCode();
                if (status < 200 || status >= 300) {
                    // ... handle unsuccessful request
                    if (status == 504) {
                        throw new BusinessException(5040, "网络错误 等待重试");
                    }
                }
                log.info("请求结束 status = {} url = {}", status, url);
                HttpEntity entity = httpResponse.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            });
            // ... do something with response
            long end = System.currentTimeMillis();
            log.info("请求完成 耗时 {} url = {}", TimeUtils.timeCost(start, end), url);
            return JSONObject.parseObject(responseBody, clazz);
        } catch (IOException e) {
            // ... handle IO exception
            e.printStackTrace();
            throw e;
        }
    }


    public static PixivResUserInfo findUserInfo(String cookie, Long uid) throws IOException {
        return get(String.format(PixivApi.USER_INFO, uid), null, cookie, PixivResUserInfo.class);
    }

    public static PixivResIllustDetail findIllustDetail(String cookie, Long pid) throws IOException {
        final PixivResIllustDetail detail = get(String.format(PixivApi.ILLUST_DETAIL, pid), null, cookie, PixivResIllustDetail.class);
        if (detail.getError()) {
            throw new IOException(detail.getMessage() + "pid = " + pid);
        }
        return detail;
    }

    public static PixivResBookmarks findBookmarks(String cookie, long userId, int offset, int limit, String tag) throws IOException {
        final HashMap<String, Object> params = new HashMap<>();
        params.put("lang", "zh");
        params.put("rest", "show");
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("tag", tag);
        return get(String.format(PixivApi.USER_BOOKMARKS, userId), params, cookie, PixivResBookmarks.class);
    }

    public static PixivResSearchResult search(String cookie, String keywords, Integer page, String mode) throws IOException {
        final HashMap<String, Object> params = new HashMap<>(2);
        params.put("mode", StringUtils.isEmpty(mode) ? "all" : mode);
        params.put("p", page);
        return get(String.format(PixivApi.SEARCH_ARTWORKS
                        , URLEncoder.encode(keywords, StandardCharsets.UTF_8).replace("+", "%20"))
                , params
                , cookie, PixivResSearchResult.class);
    }


    public static PixivResBookmarksAdd bookmarksAdd(String cookie, String token, Long pid, List<String> tags) throws IOException {
        log.info("为作品添加TAG pid = {} tags = {}", pid, tags);
        final PixivResBookmarksAdd res = post(PixivApi.ADD_BOOKMARKS, cookie, token, new PixivParamsBookmarksAdd(pid, tags), null, PixivResBookmarksAdd.class);
        log.info("添加TAG完成 https://www.pixiv.net/bookmark_add.php?type=illust&illust_id={}", pid);
        return res;
    }

    public static PixivResponse<Void> bookmarksDelete(String cooke, String token, Long bookmarkId) throws IOException {
        final HashMap<String, Object> body = new HashMap<>();
        body.put("mode", "delete_illust_bookmark");
        body.put("bookmark_id", bookmarkId);
        return post(PixivApi.DEL_BOOKMARKS, cooke, token, null, body, PixivResponse.class);
    }
}
