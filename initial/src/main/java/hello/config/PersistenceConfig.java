package hello.config;

import javax.sql.DataSource;
import java.sql.*;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import hello.redis.MyRedisson;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import com.google.common.base.Preconditions;
import org.springframework.jdbc.core.JdbcTemplate;


@Configuration
//@PropertySource({ "classpath:persistence-mysql.properties" })
public class PersistenceConfig {

//    @Autowired
//    private Environment env;



    @Bean
    public DataSource restDataSource() {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://192.168.1.70:3306/max5?serverTimezone=UTC&autoReconnect=true&useSSL=false&allowPublicKeyRetrieval=true");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        JdbcTemplate template = new JdbcTemplate(dataSource);


        try (Connection con = dataSource.getConnection();
             CallableStatement cstmt = con.prepareCall("{call sp_get_uids}");) {
            // Execute a stored procedure that returns some data.
            //cstmt.setInt(1, 50);
            ResultSet rs = cstmt.executeQuery();

            // Iterate through the data in the result set and display it.
            while (rs.next()) {
                System.out.println("EMPLOYEE: " + rs.getString("id") + ", " + rs.getString("uid"));

                System.out.println();

            }
        }
        // Handle any errors that may have occurred.
        catch (SQLException e) {
            e.printStackTrace();
        }
        return dataSource;
    }
//
//    @Bean
//    public DataSource mssqlDataSource() {
//        // Create datasource.
//        SQLServerDataSource ds = new SQLServerDataSource();
//        ds.setUser(Preconditions.checkNotNull(env.getProperty("jdbc.user")));
//        ds.setPassword(Preconditions.checkNotNull(env.getProperty("jdbc.pass")));
//        ds.setServerName(Preconditions.checkNotNull(env.getProperty("jdbc.server")));
//       // ds.setPortNumber( < port >);
//        ds.setDatabaseName(Preconditions.checkNotNull(env.getProperty("jdbc.database")));
//        System.out.println("HJHHHHHHHHHHHHHHHHHHHHHH");
//        JdbcTemplate template = new JdbcTemplate(ds);
//        try (Connection con = ds.getConnection();
//             CallableStatement cstmt = con.prepareCall("{call dbo.get_test}");) {
//            // Execute a stored procedure that returns some data.
//            //cstmt.setInt(1, 50);
//            ResultSet rs = cstmt.executeQuery();
//
//            // Iterate through the data in the result set and display it.
//            while (rs.next()) {
//                System.out.println("EMPLOYEE: " + rs.getString("id") + ", " + rs.getString("driver-class-name"));
//
//                System.out.println();
//            }
//
//
//        }
//        // Handle any errors that may have occurred.
//        catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return ds;
//    }

}
