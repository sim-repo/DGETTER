package hello.security;

import hello.config.AppConfig;
import hello.security.enums.AuthenticationModeEnum;
import hello.security.model.Login;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class CommonSub {

    public static void setup(){
        //login:
        sub_SyncLogin();
        sub_RemoveLoginById();

        //token:
        JwtSub.preload_addToken();
        JwtSub.sub_addToken();
        JwtSub.sub_removeToken();

        //roles:
        RoleSub.preload_addLoginRoles();
        RoleSub.preload_addGettersRoles();
        RoleSub.sub_addLoginRoles();
        RoleSub.sub_addGetterRoles();
        RoleSub.sub_syncLoginRoles();
        RoleSub.sub_removeLoginRoles();
        RoleSub.sub_removeGetterRoles();

        //authorization mode
        preload_SetAuthenticationMode();
        sub_SetAuthenticationMode();

        //authorization mode
        preload_SetAuthorizationMode();
        sub_SetAuthorizationMode();

        preload_SyncLogin();
    }

    private static ConcurrentHashMap<Integer, Login> loginById = new ConcurrentHashMap<>();


    static RedissonClient getClient() {
        return AppConfig.getRedClient();
    }

    //#################
    //Login functions:
    //#################

    // sub sync
    private static void sub_SyncLogin() {
        RTopic topic = getClient().getTopic("admin.sync.login");
        topic.addListener(Login.class, new MessageListener<Login>() {
            @Override
            public void onMessage(CharSequence charSequence, Login login) {
                System.out.println("admin.sync.login: "+login);
                loginById.put(login.getId(), login);
                JwtSub.safetyAddToken(login);
                BasicMgt.safetyAddBasicPassword(login);
                showStatus();
            }
        });
    }


    private static void preload_SyncLogin(){
        System.out.println("");
        System.out.println("==============");
        System.out.println("logins by ids:");
        System.out.println("==============");
        RMap<Integer, Login> map = getClient().getMap("logins");
        for(Map.Entry<Integer,Login> element : map.entrySet()){
            System.out.println(element.getValue());
            Login login = element.getValue();
            loginById.put(login.getId(), login);
            JwtSub.safetyAddToken(login);
            BasicMgt.safetyAddBasicPassword(login);
        }
    }


    // remove
    private static void sub_RemoveLoginById() {
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
                JwtSub.safetyRemoveToken(login);
                BasicMgt.safetyRemoveBasicPassword(login);
                showStatus();
            }
        });
    }



    //#################
    //Authentication mode:
    //#################

    private static void sub_SetAuthenticationMode(){
        RTopic topic = getClient().getTopic("admin.change.authentication_mode");
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String mode) {
                System.out.println("admin.change.authentication_mode: "+mode);
                SecureCheck.AUTH_MODE = AuthenticationModeEnum.fromValue(mode);
            }
        });
    }

    private static void preload_SetAuthenticationMode() {
        System.out.println("");
        System.out.println("==============================");
        System.out.println("required: authentication mode:");
        System.out.println("==============================");
        RMap<Integer, String> map = getClient().getMap("authenticationMode");
        for(Map.Entry<Integer,String> element : map.entrySet()){
            System.out.println(element.getKey()+":"+element.getValue());
            SecureCheck.AUTH_MODE = AuthenticationModeEnum.fromValue(element.getValue());
        }
    }

    //#################
    //Authorization mode:
    //#################
    private static void sub_SetAuthorizationMode(){
        RTopic topic = getClient().getTopic("admin.change.authorization_mode");
        topic.addListener(Boolean.class, new MessageListener<Boolean>() {
            @Override
            public void onMessage(CharSequence charSequence, Boolean enable) {
                System.out.println("admin.change.authorization_mode: "+enable);
                SecureCheck.enabledAuthorization = enable;
            }
        });
    }

    private static void preload_SetAuthorizationMode() {
        System.out.println("");
        System.out.println("=============================");
        System.out.println("required: authorization mode:");
        System.out.println("=============================");
        RMap<Integer, Boolean> map = getClient().getMap("authorizationMode");
        for(Map.Entry<Integer,Boolean> element : map.entrySet()){
            System.out.println(element.getKey()+":"+element.getValue());
            SecureCheck.enabledAuthorization = element.getValue();
        }
    }

    static Login getLoginById(Integer id){
        return loginById.get(id);
    }




    static void showStatus() {
        System.out.println("");
        System.out.println("");
        System.out.println("=================");
        System.out.println("loginById:"+loginById.size());
        System.out.println("loginByToken:"+JwtSub.loginByToken.size());
        System.out.println("activeTokens:"+JwtSub.activeTokens.size());
        System.out.println("=================");
    }


}
