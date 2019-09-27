package hello.security;

import hello.security.model.Login;
import hello.security.pubsub.JwtPubSub;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class BasicAuthMgt {


    // #########################
    // lifecycle step #2: user puts real password and gets first token
    // result:
    // --new encrypted password
    // --new salt
    // --new expire
    // #########################
    public static Boolean tryChangePsw(Login login, String oldPassword, String newPassword) {
        if (JwtAuthMgt.verifyUserPassword(oldPassword, login.getEncryptedPassword(), login.getSalt())) {
            String salt = JwtAuthMgt.getSalt();
            login.setSalt(salt);
            String encryptedPassword = JwtAuthMgt.generateSecurePassword(newPassword, login.getSalt());
            login.setEncryptedPassword(encryptedPassword);
            JwtAuthMgt.syncLogin(login);
            return true;
        }
        return false;
    }


    public static ResponseEntity<String> checkBasic(HttpServletRequest request) {
        final String authorization = request.getHeader("Authorization");
        if(authorization == null) {
            return new ResponseEntity<>("",new HttpHeaders(), HttpStatus.UNAUTHORIZED);
        }
        if(!authorization.toLowerCase().startsWith("basic")) {
            return new ResponseEntity<>("Basic auth parameters must be included",new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        String base64Credentials = authorization.substring("Basic".length()).trim();
        byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(credDecoded, StandardCharsets.UTF_8);
        final String[] values = credentials.split(":", 2);
        System.out.println(values[0]+":"+values[1]);
        String loginId = values[0];
        String password = values[1];
        Login login = JwtPubSub.getLoginByUsername(loginId);
        if(login == null){
            return new ResponseEntity<>("No user found with loginId: "+loginId,new HttpHeaders(), HttpStatus.UNAUTHORIZED);
        }
        if (!JwtAuthMgt.verifyUserPassword(password, login.getEncryptedPassword(), login.getSalt())) {
            return new ResponseEntity<>("",new HttpHeaders(), HttpStatus.UNAUTHORIZED);
        }
        return null;
    }

}
