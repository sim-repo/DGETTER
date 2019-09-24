package hello.security.pubsub;

import hello.config.AppConfig;
import hello.security.model.Login;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
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
        sub_AddActiveToken();
        sub_RemoveActiveToken();
    }

    private HashSet<String> activeTokens = new HashSet<>();
    private ConcurrentHashMap<String, Login> loginMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Login> loginByToken = new ConcurrentHashMap<>();


    public Login getLogin(String username){
        return this.loginMap.get(username);
    }

    public Login getLoginByToken(String token){
        return this.loginByToken.get(token);
    }

    public HashSet<String> getActiveTokens() {
        return activeTokens;
    }


    private RedissonClient getClient() {
        return appConfig.getRedClient();
    }

    public void pub_AddActiveToken(String activeToken){
        RTopic topic = getClient().getTopic("add.active_token");
        topic.publish(activeToken);
    }


    public void remove_OldActiveToken(String activeToken){
        RTopic topic = getClient().getTopic("remove.active_token");
        topic.publish(activeToken);
        loginByToken.remove(activeToken);
    }

    public void pub_AddLogin(Login login){
        RTopic topic = getClient().getTopic("add.login");
        topic.publish(login);
        loginMap.put(login.getLogin(), login);
        if(login.getToken() != null && !login.getToken().equals("")) {
            loginByToken.put(login.getToken(), login);
        }
    }


    private void sub_AddActiveToken() {
        RTopic topic = getClient().getTopic("add.active_token");
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String newToken) {
                System.out.println("security.pubsub.PubSub: take new token: "+newToken);
                activeTokens.add(newToken);
                System.out.println("activeTokens.size(): "+activeTokens.size());
            }
        });
    }

    private void sub_RemoveActiveToken() {
        RTopic topic = getClient().getTopic("remove.active_token");
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence charSequence, String oldToken) {
                System.out.println("security.pubsub.PubSub: remove old token: "+oldToken);
                activeTokens.remove(oldToken);
                System.out.println("activeTokens.size(): "+activeTokens.size());
            }
        });
    }


    private void sub_AddLoginRoles() {
        RTopic topic = getClient().getTopic("add.login.roles");
        topic.addListener(HashMap.class, new MessageListener<HashMap<String, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<String, String> roleByUsername) {
                System.out.println("security.pubsub.PubSub: take new login.role: "+roleByUsername);
                for (Map.Entry<String, String> entry : roleByUsername.entrySet()) {
                    Login login = getLogin(entry.getKey());
                    login.setRole(entry.getValue());
                    System.out.println("login.roles: "+login.getRoles());
                }
            }
        });
    }

    private void sub_AddGetterRoles() {
        RTopic topic = getClient().getTopic("add.getter.roles");
        topic.addListener(HashMap.class, new MessageListener<HashMap<String, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<String, String> roleByUsername) {
                System.out.println("security.pubsub.PubSub: take new getter.role: "+roleByUsername);
                for (Map.Entry<String, String> entry : roleByUsername.entrySet()) {
                    Login login = getLogin(entry.getKey());
                    login.setRole(entry.getValue());

                    System.out.println("login.roles: "+login.getRoles());
                }
            }
        });
    }
}
