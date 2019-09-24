package hello.security.pubsub;

import hello.config.AppConfig;
import hello.model.getter.DbGetter;
import hello.security.AuthMgt;
import hello.security.enums.AuthenticationModeEnum;
import hello.security.model.Login;
import hello.security.model.ProtoLogin;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component("pubSub")
@Scope("singleton")
public class PubSub {

    @Autowired
    private
    AppConfig appConfig;

    public PubSub() {
        super();
        setup();
    }

    public RedissonClient getRedClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:6379");
        return Redisson.create(config);
    }

    public void setup(){
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

    private ConcurrentHashMap<String, Login> loginByUsername = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Login> loginById = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Login> loginByToken = new ConcurrentHashMap<>();
    private HashSet<String> activeTokens = new HashSet<>(); // token_id


    private RedissonClient getClient() {
        return getRedClient();
    }

    //#################
    //Login functions:
    //#################

    // get
    public Login getLoginByUsername(String username){
        return this.loginByUsername.get(username);
    }

    public Login getLoginById(Integer id){
        return this.loginById.get(id);
    }

    public Login getLoginByToken(String token){
        return this.loginByToken.get(token);
    }


    // add
    private void sub_addLogin() {
        RTopic topic = getClient().getTopic("admin.add.protoLogin");
        topic.addListener(ProtoLogin.class, new MessageListener<ProtoLogin>() {
            @Override
            public void onMessage(CharSequence charSequence, ProtoLogin protoLogin) {
                System.out.println("admin.add.protoLogin: "+protoLogin);
                if(protoLogin.getId() == null || protoLogin.getName() == null || protoLogin.getPassword() == null) {
                    //TODO throw err
                    return;
                }
                Login login = AuthMgt.addNewLogin(protoLogin);
                loginById.put(login.getId(), login);
                loginByUsername.put(login.getName(), login);
                safetyAddToken(login);

                showStatus();
            }
        });
    }

    // pub sync
    public void pub_syncLogin(Login login){
        loginById.put(login.getId(), login);
        loginByUsername.put(login.getName(), login);
        safetyRemoveOldToken(login);
        safetyAddToken(login);
        RTopic topic = getClient().getTopic("admin.sync.login");
        topic.publish(login);
    }

    // sub sync
    private void sub_syncLogin() {
        RTopic topic = getClient().getTopic("admin.add.login");
        topic.addListener(Login.class, new MessageListener<Login>() {
            @Override
            public void onMessage(CharSequence charSequence, Login login) {
                System.out.println("admin.add.login: "+login);
                loginById.put(login.getId(), login);
                loginByUsername.put(login.getName(), login);
                safetyAddToken(login);
                showStatus();
            }
        });
    }

    // remove
    private void sub_removeLoginById() {
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
                safetyRemoveToken(login);
                showStatus();
            }
        });
    }





    //#################
    //Token functions:
    //#################

    // add or replace
    private void sub_addToken() {
        RTopic topic = getClient().getTopic("admin.add.token");
        topic.addListener(HashMap.class, new MessageListener<HashMap<Integer, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<Integer, String> tokenByLoginId) {
                System.out.println("admin.add.token: "+tokenByLoginId);
                for (Map.Entry<Integer, String> entry : tokenByLoginId.entrySet()) {
                    Login login = getLoginById(entry.getKey());
                    if (login == null) {
                        // TODO throw error
                        System.out.println("admin.add.token: Login is null");
                        continue;
                    }
                    safetyReplaceToken(login, entry.getValue());
                    System.out.println("admin.add.token: "+login.getToken());
                }
                showStatus();
            }
        });
    }

    // clear
    private void sub_removeToken() {
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

    // safety remove token
    private void safetyRemoveToken(Login login){
        if (login.getToken() != null && !login.getToken().equals("")) {
            loginByToken.remove(login.getToken());
            activeTokens.remove(login.getToken());
            login.setToken(null);
        }
    }

    private void safetyRemoveOldToken(Login login) {
        if (login.getOldToken() != null && !login.getOldToken().equals("")) {
            loginByToken.remove(login.getOldToken());
            activeTokens.remove(login.getOldToken());
        }
    }

    // safety add token
    private void safetyAddToken(Login login){
        if (login.getToken() != null && !login.getToken().equals("")) {
            loginByToken.put(login.getToken(), login);
            activeTokens.add(login.getToken());
        }
    }

    // safety replace token
    private void safetyReplaceToken(Login login, String newToken){
        safetyRemoveToken(login);
        login.setToken(newToken);
        loginByToken.put(newToken, login);
        activeTokens.add(newToken);
    }

    public HashSet<String> getActiveTokens() {
        return activeTokens;
    }



    //#################
    //Authentication mode:
    //#################
    private void sub_setAuthenticationMode(){
        RTopic topic = getClient().getTopic("admin.change.authentication_mode");
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String mode) {
                System.out.println("admin.change.authentication_mode: "+mode);
                AuthMgt.AUTH_MODE = AuthenticationModeEnum.fromValue(mode);
            }
        });
    }


    //#################
    //Authorization mode:
    //#################
    private void sub_setAuthorizationMode(){
        RTopic topic = getClient().getTopic("admin.change.authorization_mode");
        topic.addListener(Boolean.class, new MessageListener<Boolean>() {
            @Override
            public void onMessage(CharSequence charSequence, Boolean enable) {
                System.out.println("admin.change.authorization_mode: "+enable);
                AuthMgt.enabledAuthorization = enable;
            }
        });
    }


    //#################
    //Expire in days:
    //#################
    private void sub_setDefaultExpire(){
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
                AuthMgt.EXPIRE = expireInDays;
            }
        });
    }

    //#################
    //Roles functions:
    //#################

    // add
    private void sub_addLoginRoles() {
        RTopic topic = getClient().getTopic("admin.add.login.roles");
        topic.addListener(HashMap.class, new MessageListener<HashMap<Integer, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<Integer, String> roleByLoginId) {
                System.out.println("admin.add.login.roles: "+roleByLoginId);
                for (Map.Entry<Integer, String> entry : roleByLoginId.entrySet()) {
                    Login login = getLoginById(entry.getKey());
                    if(login == null) {
                        //TODO throw err
                        System.out.println("admin.add.login.roles: Login is null");
                        continue;
                    }
                    login.setRole(entry.getValue());
                    System.out.println("done!!! admin.add.login.roles: "+login.getRoles());
                }
            }
        });
    }

    private void sub_addGetterRoles() {
        RTopic topic = getClient().getTopic("admin.add.getter.roles");
        topic.addListener(HashMap.class, new MessageListener<HashMap<Integer, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<Integer, String> roleByGetterId) {
                System.out.println("admin.add.getter.roles: "+roleByGetterId);
                for (Map.Entry<Integer, String> entry : roleByGetterId.entrySet()) {
                    DbGetter getter = appConfig.getDbGetterById(entry.getKey());
                    if(getter == null) {
                        //TODO throw err
                        System.out.println("admin.add.getter.roles: Login is null");
                        continue;
                    }
                    getter.setRole(entry.getValue());
                    System.out.println("done!!! admin.add.getter.roles: "+getter.getRoles());
                }
            }
        });
    }

    //remove
    private void sub_removeLoginRoles() {
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

    private void sub_removeGetterRoles() {
        RTopic topic = getClient().getTopic("admin.remove.getter.roles");
        topic.addListener(HashMap.class, new MessageListener<HashMap<Integer, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<Integer, String> roleByGetterId) {
                System.out.println("admin.REMOVE.getter.roles: "+roleByGetterId);
                for (Map.Entry<Integer, String> entry : roleByGetterId.entrySet()) {
                    DbGetter getter = appConfig.getDbGetterById(entry.getKey());
                    if(getter == null) {
                        //TODO throw err
                        //TODO throw err
                        System.out.println("admin.REMOVE.getter.roles: Login is null");
                        continue;
                    }
                    getter.getRoles().remove(entry.getValue());
                    System.out.println("done!!! admin.REMOVE.getter.roles: "+getter.getRoles());
                }
            }
        });
    }

    private void showStatus() {
        System.out.println("loginByUsername:"+loginByUsername.size());
        System.out.println("loginById:"+loginById.size());
        System.out.println("loginByToken:"+loginByToken.size());
        System.out.println("activeTokens:"+activeTokens.size());
    }
}
