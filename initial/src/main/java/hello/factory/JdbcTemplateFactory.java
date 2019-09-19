package hello.factory;

import com.google.common.base.Preconditions;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import hello.model.connectors.JdbConnector;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcTemplateFactory {

    public static JdbcTemplate getJdbcTemplate(JdbConnector conn) {
        switch (conn.getCode()) {
            case mysql:
                return createMySql(conn);
            case sqlserver:
                return createSqlServer(conn);
            case postgress:
                return createPostgress(conn);
            case redis:
                return createRedis(conn);
        }
        return null;
    }

    private static JdbcTemplate createMySql(JdbConnector conn) {
        final BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(conn.getDriverClassName());
        ds.setUrl(conn.getDbURL());
        ds.setUsername(conn.getLogin());
        ds.setPassword(conn.getPsw());
        return new JdbcTemplate(ds);
    }

    private static JdbcTemplate createSqlServer(JdbConnector conn) {
        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setURL(conn.getDbURL());
        //ds.setServerName(conn.getDbServerName());
        //ds.setDatabaseName(conn.getDbName());
        ds.setUser(conn.getLogin());
        ds.setPassword(conn.getPsw());

        return new JdbcTemplate(ds);
    }

    //TODO add connector
    private static JdbcTemplate createPostgress(JdbConnector conn) {
        return null;
    }

    //TODO add connector
    private static JdbcTemplate createRedis(JdbConnector conn) {
        return null;
    }

}
