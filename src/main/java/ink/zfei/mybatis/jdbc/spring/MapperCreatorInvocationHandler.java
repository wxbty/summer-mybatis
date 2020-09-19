package ink.zfei.mybatis.jdbc.spring;

import ink.zfei.mybatis.jdbc.annotations.Sql;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

public class MapperCreatorInvocationHandler implements InvocationHandler {

    public static final String DEFAULT_PLACEHOLDER_PREFIX = "#{";
    /**
     * Default placeholder suffix: {@value}.
     */
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

    public Object invoke(Object proxy, Method method, Object[] args) {


        Object arg1 = args[0];
        if (arg1 instanceof Long) {

        }

        Sql annotation = method.getDeclaredAnnotation(Sql.class);
        if (annotation == null) {
            throw new RuntimeException("Sql value must not be null!");
        }
        //通过 jdbc 从数据库查询数据
        Connection conn = null;
        ResultSet resultSet = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection("jdbc:mysql://118.190.155.151:3306/demo", "root", "123456");
            String sql = annotation.value();
            //把#{} 解析出来出来，用args来替换
            String placeholder = parseSql(sql);
            String newVal;
            if (arg1 instanceof String) {
                newVal = "'" + arg1.toString() + "'";
            } else {
                newVal = arg1.toString();
            }
            sql = replaceSql(sql, newVal);
            stmt = conn.createStatement();
            resultSet = stmt.executeQuery(sql);
            Class returnType = method.getReturnType();

            Field[] fields = returnType.getDeclaredFields();
            Object result = returnType.newInstance();
            while (resultSet.next()) {
                for (Field field : fields) {
                    Object val = resultSet.getObject(field.getName());
                    field.setAccessible(true);
                    field.set(result, val);
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private String parseSql(String value) {

        int startIndex = value.indexOf(DEFAULT_PLACEHOLDER_PREFIX);
        if (startIndex == -1) {
            return value;
        }

        int endIndex = value.indexOf(DEFAULT_PLACEHOLDER_SUFFIX);
        if (endIndex == -1) {
            return value;
        }
        String placeholder = value.substring(startIndex, endIndex);
        return placeholder;
    }

    private String replaceSql(String value, String newVal) {

        int startIndex = value.indexOf(DEFAULT_PLACEHOLDER_PREFIX);
        if (startIndex == -1) {
            return value;
        }

        int endIndex = value.indexOf(DEFAULT_PLACEHOLDER_SUFFIX);
        if (endIndex == -1) {
            return value;
        }
        String placeholder = value.substring(startIndex + 2, endIndex);
        return value.replace(DEFAULT_PLACEHOLDER_PREFIX + placeholder + DEFAULT_PLACEHOLDER_SUFFIX, newVal);

    }


}
