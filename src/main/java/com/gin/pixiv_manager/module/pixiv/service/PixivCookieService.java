package com.gin.pixiv_manager.module.pixiv.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gin.pixiv_manager.module.pixiv.entity.PixivCookie;
import com.gin.pixiv_manager.module.pixiv.utils.pixiv.response.res.PixivResUserInfo;
import com.gin.pixiv_manager.sys.exception.BusinessException;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gin.pixiv_manager.module.pixiv.utils.pixiv.request.PixivRequest.findUserInfo;

/**
 * @author bx002
 */
@Transactional(rollbackFor = Exception.class)
public interface PixivCookieService extends IService<PixivCookie> {

     Pattern USER_ID_PATTERN = Pattern.compile("user_id=(\\d+)");

     default PixivCookie get(){
         final QueryWrapper<PixivCookie> qw = new QueryWrapper<>();
         qw.last("limit 1");
         return getOne(qw);
     }

    /**
     * 根据名称查询
     * @param name 名称
     * @return PixivCookie
     */
    default PixivCookie getByName(String name) {
        return getOne(new QueryWrapper<PixivCookie>().eq("name", name));
    }

    /**
     * 校验cookie有消息
     * @param cookie cookie
     * @param token token
     */
    default void validateCookie(String cookie, String token) throws IOException {
        final Matcher matcher = USER_ID_PATTERN.matcher(cookie);
        if (!matcher.find()) {
            throw new BusinessException(4000,"未找到userId 请检查cookie合法性");
        }
        final long userId = Long.parseLong(matcher.group(1));
        final PixivResUserInfo userInfo = findUserInfo(cookie, userId);
        if (userInfo.getError()) {
            throw new BusinessException(4001,userInfo.getMessage());
        }
        final PixivCookie entity = new PixivCookie();
        entity.setCookie(cookie);
        entity.setToken(token);
        entity.setName(userInfo.getBody().getName());
        entity.setUid(userId);
        saveOrUpdate(entity);
    }
}