package ink.zfei.mybatis.jdbc.spring;

import com.sun.org.apache.xpath.internal.operations.Bool;
import ink.zfei.mybatis.jdbc.TransactionContext;
import ink.zfei.mybatis.jdbc.annotations.Sql;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.*;

public class MapperCreatorInvocationHandler implements InvocationHandler {

    private DataSource dataSource;


    public static final String DEFAULT_PLACEHOLDER_PREFIX = "#{";
    /**
     * Default placeholder suffix: {@value}.
     */
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

    public MapperCreatorInvocationHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws SQLException {

        Object arg1 = args[0];

        Sql annotation = method.getDeclaredAnnotation(Sql.class);
        if (annotation == null) {
            throw new RuntimeException("Sql value must not be null!");
        }
        //通过 jdbc 从数据库查询数据
        Connection conn = null;
        ResultSet resultSet = null;
        Statement stmt = null;
        try {
//            conn = DriverManager.getConnection("jdbc:mysql://118.190.155.151:3306/demo", "root", "123456");
            if (TransactionContext.inTransEnv()) {
                if (TransactionContext.getConnection() == null) {
                    //事务里的第一条sql，获取连接
                    conn = dataSource.getConnection();
                    TransactionContext.setConnection(conn);
                    //开启事务
                    conn.setAutoCommit(false);
                } else {
                    conn = TransactionContext.getConnection();
                }
            } else {
                conn = dataSource.getConnection();
            }

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

            if (sql.contains("select")) {
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
            } else if (sql.contains("insert") || sql.contains("update")) {
                if (method.getReturnType() == Integer.class) {
                    Boolean res = stmt.execute(sql);
                    return res ? 1 : 0;
                } else if (method.getReturnType() == Boolean.class) {
                    return stmt.execute(sql);
                } else {
                    stmt.execute(sql);
                    return null;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (resultSet != null) {
                    resultSet.close();
                }
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
