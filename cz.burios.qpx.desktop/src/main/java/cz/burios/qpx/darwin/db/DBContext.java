package cz.burios.qpx.darwin.db;

import java.sql.Connection;

import javax.sql.DataSource;

import cz.burios.uniql.metadata.DBMetaData;

public class DBContext {

    private static DataSource dataSource;
    private static DBMetaData metaData;

    private DBContext() {
    }

    public static void setDataSource(DataSource ds) {
        dataSource = ds;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static Connection getConnection() throws Exception {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }

    public static void setMetaData(DBMetaData value) {
        metaData = value;
    }

    public static DBMetaData getMetaData() {
        return metaData;
    }

    public static void clear() {
        dataSource = null;
        metaData = null;
    }
}
