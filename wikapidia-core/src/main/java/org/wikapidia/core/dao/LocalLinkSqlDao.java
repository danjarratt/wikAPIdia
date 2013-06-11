package org.wikapidia.core.dao;

import com.typesafe.config.Config;
import org.apache.commons.io.IOUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.wikapidia.conf.Configuration;
import org.wikapidia.conf.ConfigurationException;
import org.wikapidia.conf.Configurator;
import org.wikapidia.core.dao.sql.LocalPageSqlDao;
import org.wikapidia.core.jooq.Tables;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.model.LocalLink;
import org.wikapidia.core.model.PageType;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LocalLinkSqlDao implements LocalLinkDao {
    public static final Logger LOG = Logger.getLogger(LocalPageSqlDao.class.getName());

    private final SQLDialect dialect;
    private final DataSource ds;

    public LocalLinkSqlDao (DataSource dataSource) throws DaoException{
        ds = dataSource;
        Connection conn = null;
        try {
            conn = ds.getConnection();
            this.dialect = JooqUtils.dialect(conn);
        } catch (SQLException e){
            throw new DaoException(e);
        } finally {
            quietlyCloseConn(conn);
        }
    }


    public WikapidiaIterable<LocalLink> getLinks(Language language, int localId, boolean outlinks, boolean isParseable, LocalLink.LocationType locationType) throws DaoException{
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DSLContext context = DSL.using(conn, dialect);
            TableField idField;
            if (outlinks){
                idField = Tables.LOCAL_LINK.SOURCE_ID;
            } else {
                idField = Tables.LOCAL_LINK.DEST_ID;
            }
            Cursor<Record> result = context.select()
                    .from(Tables.LOCAL_LINK)
                    .where(Tables.LOCAL_LINK.LANG_ID.equal(language.getId()))
                    .and(idField.equal(localId))
                    .and(Tables.LOCAL_LINK.IS_PARSEABLE.equal(isParseable))
                    .and(Tables.LOCAL_LINK.LOCATION_TYPE.equal((short)locationType.ordinal()))
                    .fetchLazy();
            return buildLocalLinks(result, outlinks);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            quietlyCloseConn(conn);
        }
    }

    @Override
    public WikapidiaIterable<LocalLink> getLinks(Language language, int localId, boolean outlinks) throws DaoException{
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DSLContext context = DSL.using(conn, dialect);
            TableField idField;
            if (outlinks){
                idField = Tables.LOCAL_LINK.SOURCE_ID;
            } else {
                idField = Tables.LOCAL_LINK.DEST_ID;
            }
            Cursor<Record> result = context.select()
                    .from(Tables.LOCAL_LINK)
                    .where(Tables.LOCAL_LINK.LANG_ID.equal(language.getId()))
                    .and(idField.equal(localId))
                    .fetchLazy();
            return buildLocalLinks(result, outlinks);
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            quietlyCloseConn(conn);
        }
    }

    public int getNumLinks(Language language, boolean isParseable, LocalLink.LocationType locationType) throws DaoException{
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DSLContext context = DSL.using(conn, dialect);
            Cursor<Record> result = context.select()
                    .from(Tables.LOCAL_LINK)
                    .where(Tables.LOCAL_LINK.LANG_ID.equal(language.getId()))
                    .and(Tables.LOCAL_LINK.IS_PARSEABLE.equal(isParseable))
                    .and(Tables.LOCAL_LINK.LOCATION_TYPE.equal((short)locationType.ordinal()))
                    .fetchLazy();
            int i = 0;
            for (Record r : result){
                i++;
            }
            return i;
        } catch (SQLException e){
            throw new DaoException(e);
        } finally {
            quietlyCloseConn(conn);
        }
    }

    public void save(LocalLink localLink) throws DaoException {
        Connection conn=null;
        try {
            conn = ds.getConnection();
            DSLContext context = DSL.using(conn, dialect);
            context.insertInto(Tables.LOCAL_LINK).values(
                    localLink.getLanguage().getId(),
                    localLink.getAnchorText(),
                    localLink.getSourceId(),
                    localLink.getDestId(),
                    localLink.getLocation(),
                    localLink.isParseable(),
                    localLink.getLocType().ordinal()
            ).execute();
        } catch (SQLException e) {
            throw new DaoException(e);
        } finally {
            quietlyCloseConn(conn);
        }
    }

    public void beginLoad() throws DaoException {
        Connection conn=null;
        try {
            conn = ds.getConnection();
            conn.createStatement().execute(
                    IOUtils.toString(
                            LocalPageSqlDao.class.getResource("/db/local-link-schema.sql")
                    ));
        } catch (IOException e) {
            throw new DaoException(e);
        } catch (SQLException e){
            throw new DaoException(e);
        } finally {
            quietlyCloseConn(conn);
        }
    }

    public void endLoad() throws DaoException {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            conn.createStatement().execute(
                    IOUtils.toString(
                            LocalPageSqlDao.class.getResource("/db/local-link-indexes.sql")
                    ));
        } catch (IOException e) {
            throw new DaoException(e);
        } catch (SQLException e){
            throw new DaoException(e);
        } finally {
            quietlyCloseConn(conn);
        }
    }

    /**
     * Close a connection without generating an exception if it fails.
     * @param conn
     */
    public static void quietlyCloseConn(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                LOG.log(Level.WARNING, "Failed to close connection: ", e);
            }
        }
    }


    private WikapidiaIterable<LocalLink> buildLocalLinks(Cursor<Record> result, boolean outlink){
        final boolean o = outlink;
        return new WikapidiaIterable<LocalLink>(result,
                new DaoTransformer<LocalLink>() {
                    @Override
                    public LocalLink transform(Record r) {
                        return buildLocalLink(r, o);
                    }
                }
        );
    }

    private LocalLink buildLocalLink(Record record, boolean outlink){
        if (record == null){
            return null;
        }
        return new LocalLink(
                Language.getById(record.getValue(Tables.LOCAL_LINK.LANG_ID)),
                record.getValue(Tables.LOCAL_LINK.ANCHOR_TEXT),
                record.getValue(Tables.LOCAL_LINK.SOURCE_ID),
                record.getValue(Tables.LOCAL_LINK.DEST_ID),
                outlink,
                record.getValue(Tables.LOCAL_LINK.LOCATION),
                record.getValue(Tables.LOCAL_LINK.IS_PARSEABLE),
                LocalLink.LocationType.values()[record.getValue(Tables.LOCAL_LINK.LOCATION_TYPE)]
        );
    }

    public static class Provider extends org.wikapidia.conf.Provider<LocalLinkDao> {
        public Provider(Configurator configurator, Configuration config) throws ConfigurationException {
            super(configurator, config);
        }

        @Override
        public Class getType() {
            return LocalPageDao.class;
        }

        @Override
        public String getPath() {
            return "dao.localPage";
        }

        @Override
        public LocalLinkSqlDao get(String name, Config config) throws ConfigurationException {
            if (!config.getString("type").equals("sql")) {
                return null;
            }
            try {
                return new LocalLinkSqlDao(
                        (DataSource) getConfigurator().get(
                                DataSource.class,
                                config.getString("dataSource"))
                );
            } catch (DaoException e) {
                throw new ConfigurationException(e);
            }

        }
    }
}
