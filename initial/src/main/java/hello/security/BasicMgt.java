package hello.security;

import hello.security.model.Login;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

class BasicMgt {

    private static ConcurrentHashMap<String, Login> loginByEncodedPassword = new ConcurrentHashMap<>();
    private static HashSet<String> activeEncodedPasswords = new HashSet<>(); // token_id

    static void safetyAddBasicPassword(Login login){
        if (login.getEncodedPassword() != null && !login.getEncodedPassword().equals("")) {
            loginByEncodedPassword.put(login.getEncodedPassword(), login);
            activeEncodedPasswords.add(login.getEncodedPassword());
        }
    }

    static void safetyRemoveBasicPassword(Login login){
        if (login.getEncodedPassword() != null && !login.getEncodedPassword().equals("")) {
            loginByEncodedPassword.remove(login.getEncodedPassword());
            activeEncodedPasswords.add(login.getEncodedPassword());
        }
    }


    static HashSet<String> getActiveEncodedPasswords() {
        return activeEncodedPasswords;
    }

    static Login getLoginByEncodedPassword(String encodedPassword) {
        return loginByEncodedPassword.get(encodedPassword);
    }
}
