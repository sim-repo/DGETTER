package hello.security;

import hello.security.model.Login;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class JwtSub {


    static ConcurrentHashMap<String, Login> loginByToken = new ConcurrentHashMap<>();
    static HashSet<String> activeTokens = new HashSet<>(); // token_id


    // add or replace
    static void sub_addToken() {
        RTopic topic = CommonSub.getClient().getTopic("admin.add.token");
        topic.addListener(HashMap.class, new MessageListener<HashMap<Integer, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<Integer, String> tokenByLoginId) {
                System.out.println("admin.add.token: "+tokenByLoginId);
                for (Map.Entry<Integer, String> entry : tokenByLoginId.entrySet()) {
                    addToken(entry.getKey(), entry.getValue());
                    System.out.println("admin.add.token: "+entry.getValue());
                }
                CommonSub.showStatus();
            }
        });
    }

    static void preload_addToken() {
        System.out.println("");
        System.out.println("====================");
        System.out.println("tokens by login ids:");
        System.out.println("====================");
        RMap<Integer, String> map = CommonSub.getClient().getMap("tokenByLoginId");
        for(Map.Entry<Integer,String> element : map.entrySet()){
            System.out.println(element.getKey()+":"+element.getValue());
            addToken(element.getKey(), element.getValue());
        }
    }


    // clear
    static void sub_removeToken() {
        RTopic topic = CommonSub.getClient().getTopic("admin.clear.token");
        topic.addListener(List.class, new MessageListener<ArrayList<Integer>>() {
            @Override
            public void onMessage(CharSequence charSequence, ArrayList<Integer> loginIds) {
                System.out.println("admin.clear.token: "+loginIds);
                loginIds.forEach( loginId -> {
                    Login login = CommonSub.getLoginById(loginId);
                    if (login == null) {
                        // TODO throw error
                        System.out.println("admin.add.token: Login is null");
                    } else {
                        safetyRemoveToken(login);
                        System.out.println("admin.clear.token:"+ loginId);
                    }
                });
                CommonSub.showStatus();
            }
        });
    }




    private static void addToken(Integer loginId, String token){
        Login login = CommonSub.getLoginById(loginId);
        if (login == null) {
            // TODO throw error
            System.out.println("admin.add.token: Login is null");
            return;
        }
        safetyReplaceToken(login, token);
    }


    // safety remove token
    static void safetyRemoveToken(Login login){
        if (login.getToken() != null && !login.getToken().equals("")) {
            loginByToken.remove(login.getToken());
            activeTokens.remove(login.getToken());
            login.setToken(null);
        }
    }


    // safety add token
    static void safetyAddToken(Login login){
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

    static Login getLoginByToken(String token){
        return loginByToken.get(token);
    }

    static HashSet<String> getActiveTokens() {
        return activeTokens;
    }
}
