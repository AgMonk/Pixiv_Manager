package com.gin.pixiv_manager.sys.exception;

import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 业务异常
 * @author bx002
 */
@AllArgsConstructor
public enum BusinessExceptionEnum implements IException {
    /**
     * 业务异常
     */
    PERMISSION_VALUE_NOT_NULL(3000, "权限属性禁止为空"),

    USER_EXISTS(4000, "用户已存在"),
    ROLE_EXISTS(4000, "角色已存在"),
    USER_NOT_EXISTS(4000, "用户不存在"),
    USER_OLD_PASS_ERROR(4001, "旧密码错误"),
    FORBIDDEN_UPDATE(4002, "禁止的操作"),
    USER_NOT_AVAILABLE(4004, "用户不可用"),

    RECORD_UUID_NOT_EXISTS(5000, "记录已存在"),
    RECORD_UUID_EXISTS(5000, "记录不存在"),
    UUID_NOT_EMPTY(5000, "UUID不允许为空"),
    UUID_NOT_EXISTS(5000, "UUID不存在"),
    UUID_EXISTS(5000, "UUID已存在"),

    PROJECT_NOT_EXISTS(6000, "工程不存在"),
    PASSWORD_NOT_EXISTS(7000, "密码不存在"),
    ID_NOT_EXISTS(7000, "ID不存在"),

    FILE_CREATE_FAILED(8000, "文件创建失败"),
    FILE_NOT_EXISTS(8001, "文件或目录不存在"),
    FILE_DEL_FAILED(8002, "文件删除失败"),
    FILE_IS_NULL(8003, "未指定文件"),
    FILE_IS_NOT_DIR(8004, "指定文件非目录"),
    FILE_NOT_UPLOAD(8005, "文件未上传"),
    ;

    int code;
    String message;

    @Override
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void assertTrue(boolean b) {
        if (!b) {
            throw new BusinessException(this);
        }
    }

    public void assertEqu(Object o1, Object o2) {
        assertNotNull(o1);
        assertNotNull(o2);
        assertTrue(o1.equals(o2));
    }

    public void assertFalse(boolean b) {
        assertTrue(!b);
    }

    public void assertNull(Object obj) {
        assertTrue(obj == null);
    }

    public void assertNotNull(Object obj) throws BusinessException {
        assertTrue(obj != null);
    }

    public void assertNotEmpty(Object obj) {
        assertTrue(obj != null && !"".equals(obj));
    }

    public void assertNotEmpty(List<?> list) {
        assertTrue(list != null && list.size() != 0);
    }

}
