package hello.security.pubsub;

import hello.config.AppConfig;
import hello.model.getter.DbGetter;
import hello.security.JwtAuthMgt;
import hello.security.enums.AuthenticationModeEnum;
import hello.security.model.Login;
import hello.security.model.ProtoLogin;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class JwtPubSub {

    public static void setup(){
        //login:
        sub_addLogin();
        sub_syncLogin();
        sub_removeLoginById();

        //token:
        sub_addToken();
        sub_removeToken();

        //roles:
        sub_addLoginRoles();
        sub_addGetterRoles();

        sub_removeLoginRoles();
        sub_removeGetterRoles();

        //authorization mode
        sub_setAuthorizationMode();

        //authentication mode
        sub_setAuthenticationMode();

        //default expire
        sub_setDefaultExpire();
    }

    public static ConcurrentHashMap<String, Login> loginByUsername = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, Login> loginById = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Login> loginByToken = new ConcurrentHashMap<>();
    private static HashSet<String> activeTokens = new HashSet<>(); // token_id


    private static RedissonClient getClient() {
        return AppConfig.getRedClient();
    }

    //#################
    //Login functions:
    //#################

    // get
    public static Login getLoginByUsername(String username){
        return loginByUsername.get(username);
    }

    public static Login getLoginById(Integer id){
        return loginById.get(id);
    }

    public static Login getLoginByToken(String token){
        return loginByToken.get(token);
    }


    // add
    private static void sub_addLogin() {
        RTopic topic = getClient().getTopic("admin.add.protoLogin");
        topic.addListener(ProtoLogin.class, new MessageListener<ProtoLogin>() {
            @Override
            public void onMessage(CharSequence charSequence, ProtoLogin protoLogin) {
                System.out.println("admin.add.protoLogin: "+protoLogin);
                addLogin(protoLogin);
                showStatus();
            }
        });
    }

    public static void preload_addLogin(){
        System.out.println("");
        System.out.println("====================");
        System.out.println("proto-logins by ids:");
        System.out.println("====================");
        RMap<Integer, ProtoLogin> map = getClient().getMap("protoLoginById");
        for(Map.Entry<Integer,ProtoLogin> element : map.entrySet()){
            System.out.println(element.getValue());
            addLogin(element.getValue());
        }
    }

    private static void addLogin(ProtoLogin protoLogin){
        if(protoLogin.getId() == null || protoLogin.getName() == null || protoLogin.getPassword() == null) {
            //TODO throw err
            return;
        }
        Login login = JwtAuthMgt.addNewLogin(protoLogin);
        loginById.put(login.getId(), login);
        loginByUsername.put(login.getName(), login);
        if (JwtAuthMgt.AUTH_MODE.equals(AuthenticationModeEnum.JWT)) {
            safetyAddToken(login);
        }
    }



    // pub sync
    public static void pub_syncLogin(Login login){
        loginById.put(login.getId(), login);
        loginByUsername.put(login.getName(), login);
        safetyRemoveOldToken(login);
        safetyAddToken(login);
        RTopic topic = getClient().getTopic("admin.sync.login");
        topic.publish(login);
    }

    // sub sync
    private static void sub_syncLogin() {
        RTopic topic = getClient().getTopic("admin.add.login");
        topic.addListener(Login.class, new MessageListener<Login>() {
            @Override
            public void onMessage(CharSequence charSequence, Login login) {
                System.out.println("admin.add.login: "+login);
                loginById.put(login.getId(), login);
                loginByUsername.put(login.getName(), login);
                if (JwtAuthMgt.AUTH_MODE.equals(AuthenticationModeEnum.JWT)) {
                    safetyAddToken(login);
                }
                showStatus();
            }
        });
    }

    // remove
    private static void sub_removeLoginById() {
        RTopic topic = getClient().getTopic("admin.remove.login");
        topic.addListener(Integer.class, new MessageListener<Integer>() {
            @Override
            public void onMessage(CharSequence charSequence, Integer loginId) {
                System.out.println("admin.remove.login: "+loginId);
                Login login = getLoginById(loginId);
                if (login == null) {
                    //TODO throw err
                    System.out.println("login is null");
                    return;
                }
                loginById.remove(loginId);
                loginByUsername.remove(login.getName());
                if (JwtAuthMgt.AUTH_MODE.equals(AuthenticationModeEnum.JWT)) {
                    safetyRemoveToken(login);
                }
                showStatus();
            }
        });
    }



    //#################
    //Token functions:
    //#################

    // add or replace
    private static void sub_addToken() {
        RTopic topic = getClient().getTopic("admin.add.token");
        topic.addListener(HashMap.class, new MessageListener<HashMap<Integer, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<Integer, String> tokenByLoginId) {
                System.out.println("admin.add.token: "+tokenByLoginId);
                for (Map.Entry<Integer, String> entry : tokenByLoginId.entrySet()) {
                    addToken(entry.getKey(), entry.getValue());
                    System.out.println("admin.add.token: "+entry.getValue());
                }
                showStatus();
            }
        });
    }

    public static void preload_addToken() {
        System.out.println("");
        System.out.println("====================");
        System.out.println("tokens by login ids:");
        System.out.println("====================");
        RMap<Integer, String> map = getClient().getMap("tokenByLoginId");
        for(Map.Entry<Integer,String> element : map.entrySet()){
            System.out.println(element.getKey()+":"+element.getValue());
            addToken(element.getKey(), element.getValue());
        }
    }

    private static void addToken(Integer loginId, String token){
        Login login = getLoginById(loginId);
        if (login == null) {
            // TODO throw error
            System.out.println("admin.add.token: Login is null");
            return;
        }
        safetyReplaceToken(login, token);
    }

    // clear
    private static void sub_removeToken() {
        RTopic topic = getClient().getTopic("admin.clear.token");
        topic.addListener(List.class, new MessageListener<ArrayList<Integer>>() {
            @Override
            public void onMessage(CharSequence charSequence, ArrayList<Integer> loginIds) {
                System.out.println("admin.clear.token: "+loginIds);
                loginIds.forEach( loginId -> {
                    Login login = getLoginById(loginId);
                    if (login == null) {
                        // TODO throw error
                        System.out.println("admin.add.token: Login is null");
                    } else {
                        safetyRemoveToken(login);
                        System.out.println("admin.clear.token:"+ loginId);
                    }
                });
                showStatus();
            }
        });
    }


    public static void preload_defaultExpire() {
        System.out.println("");
        System.out.println("================");
        System.out.println("expires in days:");
        System.out.println("================");
        RMap<Integer, Integer> map = getClient().getMap("expireInDays");
        for(Map.Entry<Integer,Integer> element : map.entrySet()){
            System.out.println(element.getKey()+":"+element.getValue());
            JwtAuthMgt.EXPIRE = element.getValue();
        }
    }

    // safety remove token
    private static void safetyRemoveToken(Login login){
        if (login.getToken() != null && !login.getToken().equals("")) {
            loginByToken.remove(login.getToken());
            activeTokens.remove(login.getToken());
            login.setToken(null);
        }
    }

    private static void safetyRemoveOldToken(Login login) {
        if (login.getOldToken() != null && !login.getOldToken().equals("")) {
            loginByToken.remove(login.getOldToken());
            activeTokens.remove(login.getOldToken());
        }
    }

    // safety add token
    private static void safetyAddToken(Login login){
        if (login.getToken() != null && !login.getToken().equals("")) {
            loginByToken.put(login.getToken(), login);
            activeTokens.add(login.getToken());
        }
    }

    // safety replace token
    private static void safetyReplaceToken(Login login, String newToken){
        safetyRemoveToken(login);
        login.setToken(newToken);
        loginByToken.put(newToken, login);
        activeTokens.add(newToken);
    }

    public static HashSet<String> getActiveTokens() {
        return activeTokens;
    }



    //#################
    //Authentication mode:
    //#################
    private static void sub_setAuthenticationMode(){
        RTopic topic = getClient().getTopic("admin.change.authentication_mode");
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String mode) {
                System.out.println("admin.change.authentication_mode: "+mode);
                JwtAuthMgt.AUTH_MODE = AuthenticationModeEnum.fromValue(mode);
            }
        });
    }

    public static void preload_authenticationMode() {
        System.out.println("");
        System.out.println("==============================");
        System.out.println("required: authentication mode:");
        System.out.println("==============================");
        RMap<Integer, String> map = getClient().getMap("authenticationMode");
        for(Map.Entry<Integer,String> element : map.entrySet()){
            System.out.println(element.getKey()+":"+element.getValue());
            JwtAuthMgt.AUTH_MODE = AuthenticationModeEnum.fromValue(element.getValue());
        }
    }

    //#################
    //Authorization mode:
    //#################
    private static void sub_setAuthorizationMode(){
        RTopic topic = getClient().getTopic("admin.change.authorization_mode");
        topic.addListener(Boolean.class, new MessageListener<Boolean>() {
            @Override
            public void onMessage(CharSequence charSequence, Boolean enable) {
                System.out.println("admin.change.authorization_mode: "+enable);
                JwtAuthMgt.enabledAuthorization = enable;
            }
        });
    }

    public static void preload_authorizationMode() {
        System.out.println("");
        System.out.println("=============================");
        System.out.println("required: authorization mode:");
        System.out.println("=============================");
        RMap<Integer, Boolean> map = getClient().getMap("authorizationMode");
        for(Map.Entry<Integer,Boolean> element : map.entrySet()){
            System.out.println(element.getKey()+":"+element.getValue());
            JwtAuthMgt.enabledAuthorization = element.getValue();
        }
    }

    //#################
    //Expire in days:
    //#################
    private static void sub_setDefaultExpire(){
        RTopic topic = getClient().getTopic("admin.change.default_expire_in_days");
        topic.addListener(Integer.class, new MessageListener<Integer>() {
            @Override
            public void onMessage(CharSequence charSequence, Integer expireInDays) {
                System.out.println("admin.change.default_expire_in_days: "+expireInDays);
                if(expireInDays <= 0) {
                    //TODO throw err
                    System.out.println("admin.change.default_expire_in_days: xpireInDays <= 0");
                    return;
                }
                JwtAuthMgt.EXPIRE = expireInDays;
            }
        });
    }

    //#################
    //Roles functions:
    //#################

    // add
    private static void sub_addLoginRoles() {
        RTopic topic = getClient().getTopic("admin.add.login.roles");
        topic.addListener(HashMap.class, new MessageListener<HashMap<Integer, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<Integer, String> roleByLoginId) {
                System.out.println("admin.add.login.roles: "+roleByLoginId);
                for (Map.Entry<Integer, String> element : roleByLoginId.entrySet()) {
                    if (!addLoginRoles(element.getKey(), element.getValue())) {
                        continue;
                    }
                    System.out.println("done!!! admin.add.login.roles: "+ element.getValue());
                }
            }
        });
    }

    public static void preload_addLoginRoles() {
        System.out.println("");
        System.out.println("================");
        System.out.println("add login roles:");
        System.out.println("================");
        RMap<Integer, String> map = getClient().getMap("addLoginRoles");
        for(Map.Entry<Integer,String> element : map.entrySet()){
            System.out.println(element.getKey()+":"+element.getValue());
            addLoginRoles(element.getKey(), element.getValue());
        }
    }

    private static Boolean addLoginRoles(Integer loginId, String role){
        Login login = getLoginById(loginId);
        if(login == null) {
            //TODO throw err
            System.out.println("admin.add.login.roles: Login is null");
            return false;
        }
        login.setRole(role);
        return true;
    }




    private static void sub_addGetterRoles() {
        RTopic topic = getClient().getTopic("admin.add.getter.roles");
        topic.addListener(HashMap.class, new MessageListener<HashMap<Integer, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<Integer, String> roleByGetterId) {
                System.out.println("admin.add.getter.roles: "+roleByGetterId);
                for (Map.Entry<Integer, String> element : roleByGetterId.entrySet()) {
                    addGetterRoles(element.getKey(), element.getValue());
                    System.out.println("done!!! admin.add.getter.roles: "+element.getValue());
                }
            }
        });
    }

    public static void preload_addGettersRoles() {
        System.out.println("");
        System.out.println("=================");
        System.out.println("add getter roles:");
        System.out.println("=================");
        RMap<Integer, String> map = getClient().getMap("addGetterRoles");
        for(Map.Entry<Integer,String> element : map.entrySet()){
            System.out.println(element.getKey()+":"+element.getValue());
            addGetterRoles(element.getKey(), element.getValue());
        }
    }

    private static Boolean addGetterRoles(Integer getterId, String role){
        DbGetter getter = AppConfig.getDbGetterById(getterId);
        if(getter == null) {
            //TODO throw err
            System.out.println("admin.add.getter.roles: Login is null");
            return false;
        }
        getter.setRole(role);
        return true;
    }

    //remove
    private static void sub_removeLoginRoles() {
        RTopic topic = getClient().getTopic("admin.REMOVE.login.roles");
        topic.addListener(HashMap.class, new MessageListener<HashMap<Integer, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<Integer, String> roleByLoginId) {
                System.out.println("admin.REMOVE.login.roles: "+roleByLoginId);
                for (Map.Entry<Integer, String> entry : roleByLoginId.entrySet()) {
                    Login login = getLoginById(entry.getKey());
                    if(login == null) {
                        //TODO throw err
                        //TODO throw err
                        System.out.println("admin.REMOVE.login.roles: Login is null");
                        continue;
                    }
                    login.getRoles().remove(entry.getValue());
                    System.out.println("done!!! admin.REMOVE.login.roles: "+login.getRoles());
                }
            }
        });
    }

    private static void sub_removeGetterRoles() {
        RTopic topic = getClient().getTopic("admin.remove.getter.roles");
        topic.addListener(HashMap.class, new MessageListener<HashMap<Integer, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<Integer, String> roleByGetterId) {
                System.out.println("admin.REMOVE.getter.roles: "+roleByGetterId);
                for (Map.Entry<Integer, String> entry : roleByGetterId.entrySet()) {
                    DbGetter getter = AppConfig.getDbGetterById(entry.getKey());
                    if(getter == null || getter.getRoles() == null) {
                        //TODO throw err
                        //TODO throw err
                        System.out.println("admin.REMOVE.getter.roles: Login or Roles is null");
                        continue;
                    }
                    getter.getRoles().remove(entry.getValue());
                    System.out.println("done!!! admin.REMOVE.getter.roles: "+getter.getRoles());
                }
            }
        });
    }



    private static void showStatus() {
        System.out.println("loginByUsername:"+loginByUsername.size());
        System.out.println("loginById:"+loginById.size());
        System.out.println("loginByToken:"+loginByToken.size());
        System.out.println("activeTokens:"+activeTokens.size());
    }
}
