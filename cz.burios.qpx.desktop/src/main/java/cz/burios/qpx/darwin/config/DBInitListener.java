package cz.burios.qpx.darwin.config;

import java.sql.Connection;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import cz.burios.qpx.darwin.db.DBContext;
import cz.burios.uniql.dialect.DBDialects;
import cz.burios.uniql.metadata.DBMetaData;
import cz.burios.uniql.metadata.DBSchemaManager;
import cz.burios.uniql.metadata.JpaMetaDataReader;
import cz.burios.uniql.metadata.SchemaDiff;

public class DBInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/JPADataSource");

            try (Connection connection = ds.getConnection()) {
                DBMetaData actual = DBMetaData.load(connection);

                EntityManagerFactory emf = Persistence.createEntityManagerFactory("archer");
                try {
                    DBMetaData desired = new JpaMetaDataReader().read(emf);
                    SchemaDiff diff = SchemaDiff.compare(actual, desired);

                    if (!diff.isEmpty()) {
                        System.out.println("Applying schema changes: " + diff.size());
                        diff.apply(connection, new DBSchemaManager(DBDialects.forConnection(connection)));
                        actual = DBMetaData.load(connection);
                    }
                } finally {
                    emf.close();
                }

                DBContext.setDataSource(ds);
                DBContext.setMetaData(actual);
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
