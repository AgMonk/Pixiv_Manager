package com.gin.pixiv_manager.module.pixiv.utils.pixiv.request;

/**
 * PivixApi地址
 * @author bx002
 */
public class PixivApi {
    /**
     * 站点
     */
    public static final String DOMAIN = "https://www.pixiv.net";
    /**
     * AJAX前缀接口
     */
    public static final String AJAX_API = DOMAIN + "/ajax";
    /**
     * 用户信息
     */
    public static final String USER_INFO = AJAX_API + "/user/%d";
    /**
     * 用户收藏
     */
    public static final String USER_BOOKMARKS = AJAX_API + "/user/%d/illusts/bookmarks";
    /**
     * 添加收藏
     */
    public static final String ADD_BOOKMARKS = AJAX_API + "/illusts/bookmarks/add";
    /**
     * 删除收藏
     */
    public static final String DEL_BOOKMARKS = DOMAIN + "/rpc/index.php";
    /**
     * 作品详情
     */
    public static final String ILLUST_DETAIL = AJAX_API + "/illust/%d";
    /**
     * 搜索作品
     */
    public static final String SEARCH_ARTWORKS = AJAX_API + "/search/artworks/%s";

}
