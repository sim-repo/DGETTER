package hello.config;

import hello.factory.JdbcTemplateFactory;
import hello.model.connectors.JdbConnector;
import hello.model.getter.DbGetter;

import hello.security.CommonSub;
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
//mvnw package && java -DredisURL="redis://127.0.0.1:6379" -jar target/gs-spring-boot-docker-0.1.0.jar

@Service("appConfig")
@Scope("singleton")
public class AppConfig {

    private static ConcurrentHashMap<String,JdbcTemplate> jdbcTemplateMap = new ConcurrentHashMap<String, JdbcTemplate>();
    private static ConcurrentHashMap<String,DbGetter> getterMap = new ConcurrentHashMap<String, DbGetter>();
    private static ConcurrentHashMap<Integer,DbGetter> getterById = new ConcurrentHashMap<Integer, DbGetter>();
    private static ConcurrentHashMap<String,JdbConnector> connectorMap = new ConcurrentHashMap<String, JdbConnector>();

    private static RedissonClient redClient = null;
    private static String RedisURL="";


    public void setup(String _redisURL) {
        RedisURL = _redisURL;
        Config config = new Config();
        config.useSingleServer().setAddress(RedisURL);
        redClient = Redisson.create(config);

        CommonSub.setup();
        preload_JdbConnector();
        preload_Getter();

        subJdbConnector();
        subGetter();
    }


    public static RedissonClient getRedClient() {
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
                addJdbcConnector(conn);
                System.out.println("-----");
                System.out.println("total: "+jdbcTemplateMap.size());
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
            addJdbcConnector(element.getValue());
        }
        System.out.println("-----");
        System.out.println("total: "+jdbcTemplateMap.size());
    }

    private static void addJdbcConnector(JdbConnector connector){
        JdbcTemplate t = JdbcTemplateFactory.getJdbcTemplate(connector);
        jdbcTemplateMap.put(connector.getEndpointId(), t);
        connectorMap.put(connector.getEndpointId(), connector);
        System.out.println("id:["+connector.getId()+"]");
    }


    private void subGetter(){
        RTopic topic = getRedClient().getTopic("getter");

        topic.addListener(DbGetter.class, new MessageListener<DbGetter>() {
            @Override
            public void onMessage(CharSequence charSequence, DbGetter getter) {
                addGetter(getter);
                System.out.println("-----");
                System.out.println("total: "+getterMap.size());
            }
        });
    }



    private static void preload_Getter() {
        System.out.println("");
        System.out.println("===========");
        System.out.println("jdb getter:");
        System.out.println("===========");
        RMap<Integer, DbGetter> map = getRedClient().getMap("jdbcGetter");
        for(Map.Entry<Integer,DbGetter> element : map.entrySet()){
            DbGetter getter = element.getValue();
            addGetter(getter);
        }
        System.out.println("-----");
        System.out.println("total: "+getterMap.size());
    }

    private static void addGetter(DbGetter getter) {
        getterMap.put(getter.getEndpointId()+getter.getMethod(), getter);
        getterById.put(getter.getId(), getter);
        System.out.println("id:["+getter.getId()+"]");
    }

}
