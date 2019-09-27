package hello.config;

import hello.factory.JdbcTemplateFactory;
import hello.model.connectors.JdbConnector;
import hello.model.getter.DbGetter;

import hello.security.JwtAuthMgt;
import hello.security.enums.AuthenticationModeEnum;
import hello.security.enums.JwtStatusEnum;
import hello.security.pubsub.JwtPubSub;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.config.Config;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


//mvnw package && java -jar target/gs-spring-boot-docker-0.1.0.jar

@Service("appConfig")
@Scope("singleton")
public class AppConfig {

    private static ConcurrentHashMap<String,JdbcTemplate> jdbcTemplateMap = new ConcurrentHashMap<String, JdbcTemplate>();
    private static ConcurrentHashMap<String,DbGetter> getterMap = new ConcurrentHashMap<String, DbGetter>();
    private static ConcurrentHashMap<Integer,DbGetter> getterById = new ConcurrentHashMap<Integer, DbGetter>();
    private static ConcurrentHashMap<String,JdbConnector> connectorMap = new ConcurrentHashMap<String, JdbConnector>();

    private static RedissonClient redClient = null;

    public AppConfig() {
        super();
        JwtPubSub.setup();
        JwtPubSub.preload_authenticationMode();
        JwtPubSub.preload_authorizationMode();
        JwtPubSub.preload_addLogin();
        JwtPubSub.preload_addToken();
        JwtPubSub.preload_defaultExpire();
        JwtPubSub.preload_addLoginRoles();
        JwtPubSub.preload_addGettersRoles();
        JwtPubSub.preload_syncLogin();
        preload_JdbConnector();
        preload_Getter();

        subJdbConnector();
        subGetter();
    }

    public static RedissonClient getRedClient() {
        if (redClient == null) {
            Config config = new Config();
            config.useSingleServer()
                    .setAddress("redis://192.168.1.70:6379");
            redClient = Redisson.create(config);
        }
        return redClient;
    }

    public static JdbcTemplate getJdbcTemplate(String endpoint) {
        return jdbcTemplateMap.get(endpoint);
    }

    public static JdbConnector getJdbConnector(String endpoint) {
        return connectorMap.get(endpoint);
    }

    public static DbGetter getDbGetter(String endpoint, String method){
        return getterMap.get(endpoint+method);
    }

    public static DbGetter getDbGetterById(Integer getterId){
        return getterById.get(getterId);
    }

    // SUBSCRIBERS:

    private void subJdbConnector(){
        RTopic topic = getRedClient().getTopic("jdb.connector");

        topic.addListener(JdbConnector.class, new MessageListener<JdbConnector>() {
            @Override
            public void onMessage(CharSequence charSequence, JdbConnector conn) {
                System.out.println(conn);
                JdbcTemplate t = JdbcTemplateFactory.getJdbcTemplate(conn);
                jdbcTemplateMap.put(conn.getEndpointId(), t);
                connectorMap.put(conn.getEndpointId(), conn);
                System.out.println("Total: "+jdbcTemplateMap.size());
            }
        });
    }

    private void subGetter(){
        RTopic topic = getRedClient().getTopic("getter");

        topic.addListener(DbGetter.class, new MessageListener<DbGetter>() {
            @Override
            public void onMessage(CharSequence charSequence, DbGetter getter) {
                System.out.println(getter);
                getterMap.put(getter.getEndpointId()+getter.getMethod(), getter);
                getterById.put(getter.getId(), getter);
                System.out.println("Total: "+getterMap.size());
            }
        });
    }

    private static void preload_JdbConnector() {
        System.out.println("");
        System.out.println("===============");
        System.out.println("jdb connectors:");
        System.out.println("===============");
        RMap<Integer, JdbConnector> map = getRedClient().getMap("jdbcConnectors");
        for(Map.Entry<Integer,JdbConnector> element : map.entrySet()){
            JdbConnector conn = element.getValue();
            System.out.println(element.getKey()+":"+conn);
            JdbcTemplate t = JdbcTemplateFactory.getJdbcTemplate(conn);
            jdbcTemplateMap.put(conn.getEndpointId(), t);
            connectorMap.put(conn.getEndpointId(), conn);
        }
        System.out.println("Total: "+jdbcTemplateMap.size());
    }

    private static void preload_Getter() {
        System.out.println("");
        System.out.println("===========");
        System.out.println("jdb getter:");
        System.out.println("===========");
        RMap<Integer, DbGetter> map = getRedClient().getMap("jdbcGetter");
        for(Map.Entry<Integer,DbGetter> element : map.entrySet()){
            DbGetter getter = element.getValue();
            System.out.println(element.getKey()+":"+getter);
            getterMap.put(getter.getEndpointId()+getter.getMethod(), getter);
            getterById.put(getter.getId(), getter);
        }
        System.out.println("Total: "+getterMap.size());
    }

}
