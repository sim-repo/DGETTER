package hello.security;

import hello.config.AppConfig;
import hello.model.getter.DbGetter;
import hello.security.model.Login;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class RoleSub {


    // add
    static void sub_syncLoginRoles() {
        RTopic topic = CommonSub.getClient().getTopic("admin.sync.login.roles");
        topic.addListener(HashMap.class, new MessageListener<HashMap<Integer, HashSet<String>>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<Integer, HashSet<String>> rolesByLoginId) {
                System.out.println("admin.sync.login.roles: "+rolesByLoginId);
                for (Map.Entry<Integer, HashSet<String>> element : rolesByLoginId.entrySet()) {
                    rolesByLoginId.put(element.getKey(), element.getValue());
                }
               // showStatus();
            }
        });
    }



    // add
    static void sub_addLoginRoles() {
        RTopic topic = CommonSub.getClient().getTopic("admin.add.login.roles");
        topic.addListener(HashMap.class, new MessageListener<HashMap<Integer, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<Integer, String> roleByLoginId) {
                System.out.println("admin.add.login.roles: "+roleByLoginId);
                for (Map.Entry<Integer, String> element : roleByLoginId.entrySet()) {
                    if (!addLoginRoles(element.getKey(), element.getValue())) {
                        continue;
                    }
                    System.out.println("admin.add.login.roles: ["+element.getKey()+"]:["+ element.getValue()+"]");
                }
            }
        });
    }

    static void preload_addLoginRoles() {
        System.out.println("");
        System.out.println("================");
        System.out.println("add login roles:");
        System.out.println("================");
        RMap<Integer, String> map = CommonSub.getClient().getMap("addLoginRoles");
        for(Map.Entry<Integer,String> element : map.entrySet()){
            if(addLoginRoles(element.getKey(), element.getValue())){
                System.out.println("admin.add.login.roles: ["+element.getKey()+"]:["+ element.getValue()+"]");
            }
        }
    }

    private static Boolean addLoginRoles(Integer loginId, String role){
        Login login = CommonSub.getLoginById(loginId);
        if(login == null) {
            //TODO throw err
            System.out.println("admin.add.login.roles: Login is null");
            return false;
        }
        login.setRole(role);
        return true;
    }




    static void sub_addGetterRoles() {
        RTopic topic = CommonSub.getClient().getTopic("admin.add.getter.roles");
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

    static void preload_addGettersRoles() {
        System.out.println("");
        System.out.println("=================");
        System.out.println("add getter roles:");
        System.out.println("=================");
        RMap<Integer, String> map = CommonSub.getClient().getMap("addGetterRoles");
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
    static void sub_removeLoginRoles() {
        RTopic topic = CommonSub.getClient().getTopic("admin.REMOVE.login.roles");
        topic.addListener(HashMap.class, new MessageListener<HashMap<Integer, String>>() {
            @Override
            public void onMessage(CharSequence charSequence, HashMap<Integer, String> roleByLoginId) {
                System.out.println("admin.REMOVE.login.roles: "+roleByLoginId);
                for (Map.Entry<Integer, String> entry : roleByLoginId.entrySet()) {
                    Login login = CommonSub.getLoginById(entry.getKey());
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

    static void sub_removeGetterRoles() {
        RTopic topic = CommonSub.getClient().getTopic("admin.remove.getter.roles");
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
}
