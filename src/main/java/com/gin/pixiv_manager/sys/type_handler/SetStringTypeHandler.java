package com.gin.pixiv_manager.sys.type_handler;

import com.gin.pixiv_manager.sys.exception.BusinessException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 列表
 * @author bx002
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(Set.class)
public class SetStringTypeHandler extends BaseTypeHandler<Set<String>> {

    public static final String DELIMITER = ",";

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Set<String> list, JdbcType jdbcType) throws SQLException {
        for (String item : list) {
            if (item.contains(DELIMITER)) {
                throw new BusinessException(3000, "非法字符：" + DELIMITER);
            }
        }
        String s = String.join(DELIMITER, list);
        preparedStatement.setString(i, isEmpty(s) ? null : s);
    }

    @Override
    public Set<String> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String data = resultSet.getString(s);
        return isEmpty(data) ? new HashSet<>() : new HashSet<>(Arrays.asList(data.split(DELIMITER)));
    }

    @Override
    public Set<String> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String data = resultSet.getString(i);
        return isEmpty(data) ? new HashSet<>() : new HashSet<>(Arrays.asList(data.split(DELIMITER)));
    }

    @Override
    public Set<String> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String data = callableStatement.getString(i);
        return isEmpty(data) ? new HashSet<>() : new HashSet<>(Arrays.asList(data.split(DELIMITER)));
    }

    private boolean isEmpty(String data) {
        return data == null || "".equals(data);
    }
}
