package ink.zfei.mybatis.jdbc;

import javax.sql.DataSource;

public class MybtisDataSource {

    public static MybtisDataSource thisInstance = new MybtisDataSource();

    private DataSource dataSource;


    private MybtisDataSource() {

    }

    public static DataSource get() {
        return thisInstance.getDataSource();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
