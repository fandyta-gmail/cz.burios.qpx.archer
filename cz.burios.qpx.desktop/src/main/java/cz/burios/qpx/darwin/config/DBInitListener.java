package cz.burios.qpx.darwin.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Connection;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import cz.burios.qpx.darwin.db.DBContext;
import cz.burios.uniql.metadata.DBMetaData;

@WebListener
public class DBInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/JPADataSource");

            try (Connection connection = ds.getConnection()) {
                DBMetaData metaData = DBMetaData.load(connection);
                DBContext.setDataSource(ds);
                DBContext.setMetaData(metaData);
            }

            System.out.println("DBContext initialized with DataSource and DBMetaData");
        } catch (Exception e) {
            DBContext.clear();
            throw new RuntimeException("Failed to initialize DBContext", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBContext.clear();
    }
}
