package hello.config;

import hello.factory.JdbcTemplateFactory;
import hello.model.connectors.JdbConnector;
import hello.model.getter.DbGetter;
import hello.model.getter.SecureDbGetter;
import hello.security.model.Login;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.config.Config;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;


//mvnw package && java -jar target/gs-spring-boot-docker-0.1.0.jar

@Service("appConfig")
@Scope("singleton")
public class AppConfig {

    ConcurrentHashMap<String,JdbcTemplate> jdbcTemplateMap = new ConcurrentHashMap<String, JdbcTemplate>();
    ConcurrentHashMap<String,DbGetter> getterMap = new ConcurrentHashMap<String, DbGetter>();
    ConcurrentHashMap<String,SecureDbGetter> secGetterMap = new ConcurrentHashMap<String, SecureDbGetter>();
    ConcurrentHashMap<String,JdbConnector> connectorMap = new ConcurrentHashMap<String, JdbConnector>();

    RedissonClient redClient = null;

    public AppConfig() {
        super();
        subJdbConnector();
        subGetter();
        subSecGetter();
    }

    private RedissonClient getRedClient() {
        if (redClient == null) {
            Config config = new Config();
            config.useSingleServer()
                    .setAddress("redis://192.168.1.70:6379");
            redClient = Redisson.create(config);
        }
        return redClient;
    }

    public JdbcTemplate getJdbcTemplate(String endpoint) {
        return jdbcTemplateMap.get(endpoint);
    }

    public JdbConnector getJdbConnector(String endpoint) {
        return connectorMap.get(endpoint);
    }

    public DbGetter getDbGetter(String endpoint, String method){
        return getterMap.get(endpoint+method);
    }

    public SecureDbGetter getSecureDbGetter(String endpoint, String method){
        return secGetterMap.get(endpoint+method);
    }


    // PUB:
    public void pubLogins(Login login){
        RTopic topic = getRedClient().getTopic("logins");
        long clientsReceivedMessage = topic.publish(login);
    }


    // SUBSCRIBERS:

    private void subJdbConnector(){
        System.out.println("sub #1: JdbConnector is ready");
        RTopic topic = getRedClient().getTopic("jdb.connector");

        topic.addListener(JdbConnector.class, new MessageListener<JdbConnector>() {
            @Override
            public void onMessage(CharSequence charSequence, JdbConnector conn) {
                System.out.println("TAKE connector");
                System.out.println(conn);
                JdbcTemplate t = JdbcTemplateFactory.getJdbcTemplate(conn);
                jdbcTemplateMap.put(conn.getEndpointId(), t);
                connectorMap.put(conn.getEndpointId(), conn);
                System.out.println("Total: "+jdbcTemplateMap.size());
            }
        });
    }

    private void subGetter(){
        System.out.println("sub #2: DbGetter is ready");
        RTopic topic = getRedClient().getTopic("getter");

        topic.addListener(DbGetter.class, new MessageListener<DbGetter>() {
            @Override
            public void onMessage(CharSequence charSequence, DbGetter getter) {
                System.out.println("TAKE getter");
                System.out.println(getter);
                getterMap.put(getter.getEndpointId()+getter.getMethod(), getter);
                System.out.println("Total: "+getterMap.size());
            }
        });
    }

    private void subSecGetter(){
        System.out.println("sub #3: SecureDbGetter is ready");
        RTopic topic = getRedClient().getTopic("secure.getter");

        topic.addListener(SecureDbGetter.class, new MessageListener<SecureDbGetter>() {
            @Override
            public void onMessage(CharSequence charSequence, SecureDbGetter getter) {
                System.out.println("TAKE sec getter");
                System.out.println(getter);
                secGetterMap.put(getter.getEndpointId()+getter.getMethod(), getter);
                System.out.println("Total: "+secGetterMap.size());
            }
        });
    }
}
