package com.gin.pixiv_manager.sys.request;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

import static com.gin.pixiv_manager.sys.utils.StringUtils.humpToLine;

/**
 * 查询的过滤条件
 * @author bx002
 */
@Getter
@Setter
public class Filter implements Serializable {
    public static final String TYPE_EQ = "eq";
    public static final String TYPE_IN = "in";
    public static final String TYPE_LIKE = "like";
    public static final String TYPE_BETWEEN = "between";
    public static final String TYPE_GE = "ge";
    public static final String TYPE_GT = "gt";
    public static final String TYPE_LE = "le";
    public static final String TYPE_LT = "lt";


    /**
     * 条件类型
     */
    String type;
    /**
     * 字段名
     */
    String field;
    /**
     * 值
     */
    String value;
    /**
     * 数组值
     */
    List<String> array;

    public void setField(String field) {
        this.field = humpToLine(field);
    }


    public void addFilter(QueryWrapper<?> queryWrapper) {
        //noinspection EnhancedSwitchMigration
        switch (this.type) {
            case TYPE_EQ:
                queryWrapper.eq(field, value);
                break;
            case TYPE_IN:
                queryWrapper.in(field, array);
                break;
            case TYPE_LIKE:
                queryWrapper.like(field, value);
                break;
            case TYPE_BETWEEN:
                queryWrapper.between(field, array.get(0), array.get(1));
                break;
            case TYPE_GE:
                queryWrapper.ge(field, value);
                break;
            case TYPE_GT:
                queryWrapper.gt(field, value);
                break;
            case TYPE_LE:
                queryWrapper.le(field, value);
                break;
            case TYPE_LT:
                queryWrapper.lt(field, value);
                break;
            default:
                System.out.printf("未识别条件 type: %s field: %s value: %s array:[%s]", type, field, value, array == null ? "" : String.join(",", array));
                break;
        }
    }

    public static void addFilters(QueryWrapper<?> queryWrapper, List<Filter> list) {
        list.forEach(item -> item.addFilter(queryWrapper));
    }
}
