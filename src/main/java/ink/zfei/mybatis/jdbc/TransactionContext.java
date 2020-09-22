package ink.zfei.mybatis.jdbc;

import java.sql.Connection;

public class TransactionContext {

    private static ThreadLocal<Boolean> transactionEnv = new ThreadLocal<>();
    private static ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    static {
        transactionEnv.set(false);
        connectionHolder.set(null);
    }

    public static void setFlag(boolean flag) {
        transactionEnv.set(flag);
    }

    public static void setConnection(Connection flag) {
        connectionHolder.set(flag);
    }

    public static boolean inTransEnv() {
        return transactionEnv.get();
    }

    public static Connection getConnection() {
        return connectionHolder.get();
    }
}
