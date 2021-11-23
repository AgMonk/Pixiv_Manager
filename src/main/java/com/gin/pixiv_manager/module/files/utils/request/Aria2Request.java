package com.gin.pixiv_manager.module.files.utils.request;

import com.alibaba.fastjson.JSONObject;
import com.gin.pixiv_manager.module.files.utils.method.Aria2Method;
import com.gin.pixiv_manager.module.files.utils.response.Aria2Response;
import com.gin.pixiv_manager.module.files.utils.response.Aria2ResponseMessage;
import com.gin.pixiv_manager.module.files.utils.response.Aria2ResponseQuest;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Collection;

/**
 * @author bx002
 */
public class Aria2Request {
    public static final String RPC_URL = "http://localhost:6800/jsonrpc";

    private static <T extends Aria2Response> T send(Aria2RequestParam param, Class<T> clazz) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(RPC_URL);
            final String string = JSONObject.toJSONString(param);
            httpPost.setEntity(new StringEntity(string, ContentType.APPLICATION_JSON));
            String responseBody = httpClient.execute(httpPost, httpResponse -> {
                int status = httpResponse.getStatusLine().getStatusCode();
                if (status < 200 || status >= 300) {
                    // ... handle unsuccessful request
                }
                HttpEntity entity = httpResponse.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            });
            // ... do something with response
            return JSONObject.parseObject(responseBody, clazz);
        } catch (IOException e) {
            // ... handle IO exception
        }
        return null;
    }

    public static Aria2ResponseQuest tellStop() {
        final Aria2RequestParam param = Aria2Method.TELL_STOPPED.toParam();
        param.addParam(-1);
        param.addParam(1000);
        return send(param, Aria2ResponseQuest.class);
    }

    public static Aria2ResponseQuest tellActive() {
        return send(Aria2Method.TELL_ACTIVE.toParam(), Aria2ResponseQuest.class);
    }

    public static Aria2ResponseQuest tellWaiting() {
        final Aria2RequestParam param = Aria2Method.TELL_WAITING.toParam();
        param.addParam(0);
        param.addParam(1000);
        return send(param, Aria2ResponseQuest.class);
    }

    public static Aria2ResponseMessage removeQuest(String gid) {
        final Aria2RequestParam param = Aria2Method.REMOVE_DOWNLOAD_RESULT.toParam();
        param.addParam(gid);
        return send(param, Aria2ResponseMessage.class);
    }

    public static Aria2ResponseMessage addUri(Collection<String> url, Aria2UriOption option) {
        final Aria2RequestParam param = Aria2Method.ADD_URI.toParam();
        param.addParam(url);
        param.addParam(option);
        return send(param, Aria2ResponseMessage.class);
    }
}
