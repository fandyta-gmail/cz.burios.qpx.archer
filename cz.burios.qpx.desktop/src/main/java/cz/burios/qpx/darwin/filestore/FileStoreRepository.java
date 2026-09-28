package cz.burios.qpx.darwin.filestore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import cz.burios.qpx.darwin.db.DBContext;
import cz.burios.uniql.metadata.TableMetaData;
import cz.burios.uniql.model.DynamicRecord;
import cz.burios.uniql.sql.QLCondition;
import cz.burios.uniql.sql.QLInsert;
import cz.burios.uniql.sql.QLSelect;
import cz.burios.uniql.sql.QLSql;
import cz.burios.uniql.sql.QLTable;
import cz.burios.uniql.sql.QLValue;
import cz.burios.uniql.sql.QLWhere;

/** FileStore metadata access implemented with UniQL SQL builders. */
@Repository
public class FileStoreRepository {

    public void insertDirectory(DynamicRecord record) throws Exception {
        executeInsert("FILESTORE", record);
    }

    public void insertFile(DynamicRecord record) throws Exception {
        executeInsert("FILESTORE_RECORD", record);
    }

    public DynamicRecord findDirectory(String id) throws Exception {
        return findOne("FILESTORE", id);
    }

    public DynamicRecord findFile(String id) throws Exception {
        return findOne("FILESTORE_RECORD", id);
    }

    public List<DynamicRecord> listDirectories(String parentId) throws Exception {
        return list("FILESTORE", "PARENT_ID", parentId);
    }

    public List<DynamicRecord> listFiles(String fileStoreId) throws Exception {
        return list("FILESTORE_RECORD", "FILESTORE_ID", fileStoreId);
    }

    private void executeInsert(String tableName, DynamicRecord record) throws Exception {
        QLInsert insert = new QLInsert(new QLTable(tableName)).row(record);
        execute(insert);
    }

    private void execute(cz.burios.uniql.sql.QLStatement statement) throws Exception {
        QLSql.Result rendered = QLSql.render(statement);
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(rendered.sql())) {
            bind(ps, rendered.parameters());
            ps.executeUpdate();
        }
    }

    private DynamicRecord findOne(String tableName, String id) throws Exception {
        TableMetaData meta = table(tableName);
        QLSelect select = new QLSelect();
        select.from = new QLTable(tableName);
        select.where = new QLWhere(new QLCondition(
                new cz.burios.uniql.sql.QLColumn("ID"), "=", new QLValue(id)));

        QLSql.Result rendered = QLSql.render(select);
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(rendered.sql())) {
            bind(ps, rendered.parameters());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? read(meta, rs) : null;
            }
        }
    }

    private List<DynamicRecord> list(String tableName, String column, String value) throws Exception {
        TableMetaData meta = table(tableName);
        QLSelect select = new QLSelect();
        select.from = new QLTable(tableName);
        select.where = new QLWhere(new QLCondition(
                new cz.burios.uniql.sql.QLColumn(column), "=", new QLValue(value)));

        QLSql.Result rendered = QLSql.render(select);
        List<DynamicRecord> result = new ArrayList<>();
        try (Connection c = DBContext.getConnection();
             PreparedStatement ps = c.prepareStatement(rendered.sql())) {
            bind(ps, rendered.parameters());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(read(meta, rs));
            }
        }
        return result;
    }

    private TableMetaData table(String name) {
        if (DBContext.getMetaData() == null) throw new IllegalStateException("DB metadata is not initialized");
        TableMetaData table = DBContext.getMetaData().table(name);
        if (table == null) throw new IllegalStateException("Unknown DB table: " + name);
        return table;
    }

    private DynamicRecord read(TableMetaData meta, ResultSet rs) throws Exception {
        DynamicRecord record = new DynamicRecord(meta);
        for (var column : meta.columns) {
            record.put(column.name, rs.getObject(column.name));
        }
        return record;
    }

    private void bind(PreparedStatement ps, List<Object> values) throws Exception {
        for (int i = 0; i < values.size(); i++) ps.setObject(i + 1, values.get(i));
    }
}
