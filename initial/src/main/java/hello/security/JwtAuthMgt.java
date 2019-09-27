package hello.security;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Sets;
import hello.config.AppConfig;
import hello.model.getter.IGetter;
import hello.security.enums.AuthenticationModeEnum;
import hello.security.enums.JwtStatusEnum;
import hello.security.model.Login;
import hello.security.model.ProtoLogin;
import hello.security.pubsub.JwtPubSub;
import io.jsonwebtoken.*;
import org.joda.time.DateTime;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class JwtAuthMgt {

    private static final Random RANDOM = new SecureRandom();
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    private static final String SECRET = "ThisIsASecret";

    private static final String TOKEN_PREFIX = "Bearer";

    private static final String HEADER_STRING = "Authorization";


    // Changed Dynamically States
    public static Integer EXPIRE = 2; // in days

    public static AuthenticationModeEnum AUTH_MODE = AuthenticationModeEnum.BASIC;

    public static Boolean enabledAuthorization = false;



    // #########################
    // lifecycle step #1: new user registration -
    // admin creates new login with temporary password
    // result:
    // --new login
    // --new temporary encrypted password
    // --new temporary salt
    // --new temporary expire
    // #########################

    // classic web-service version
    public static Boolean addNewLogin(String username, String psw, int expireInDays) {
        String salt = getSalt();
        String encryptedPsw = generateSecurePassword(psw, salt);
        int expireDays = expireInDays == 0 ? EXPIRE : expireInDays;
        Date expire = new DateTime(new Date()).plusDays(expireDays).toDate();
        Login login = new Login(username, encryptedPsw, salt, expire);
        //syncLogin(login);
        return true;
    }

    // docker-container version
    public static Login addNewLogin(ProtoLogin protoLogin) {
        String salt = getSalt();
        String encryptedPsw = generateSecurePassword(protoLogin.getPassword(), salt);
        int expireDays = protoLogin.getExpireInDays() == 0 ? EXPIRE : protoLogin.getExpireInDays();
        Date expire = new DateTime(new Date()).plusDays(expireDays).toDate();
        Login login = new Login(protoLogin.getId(), protoLogin.getName(), encryptedPsw, salt, expire);
        return login;
    }

    // #########################
    // lifecycle step #2: user puts real password and gets first token
    // result:
    // --new active token
    // --remove old token
    // --new encrypted password
    // --new salt
    // --new expire
    // #########################
    public static String tryChangePsw(Login login, String oldPassword, String newPassword) {
        String token = tryCreateToken(login, oldPassword);
        if (token == null) {
            // TODO throw err
            return null;
        }
        String salt = getSalt();
        login.setSalt(salt);
        String encryptedPassword = generateSecurePassword(newPassword, login.getSalt());
        login.setEncryptedPassword(encryptedPassword);
        syncLogin(login);
        return token;
    }

    // #########################
    // lifecycle step #3: token has expired  -
    // user puts password for getting new token
    // result:
    // --new active token
    // --remove old token
    // --new expire
    // #########################
    public static String tryRecreateToken(Login login, String password) {
        String token = tryCreateToken(login, password);
        if (token == null) {
            // TODO throw err
            return null;
        }
        syncLogin(login);
        return token;
    }



    // #########################
    // lifecycle step #4: regular user requests  -
    // check access to database connectors
    // returns HTTP-status code if authentication == fail or
    // null if authentication == success
    // #########################

    public static ResponseEntity<String> checkAuthentication(HttpServletRequest request) {
        switch (AUTH_MODE) {
            case NONE:
                return null;
            case JWT:
                return checkJWT(request);
            case BASIC:
                return BasicAuthMgt.checkBasic(request);
        }
        return null;
    }




    private static ResponseEntity<String> checkJWT(HttpServletRequest request) {
        ResponseEntity<String> res;
        JwtStatusEnum status = JwtStatusEnum.NotAuthorized;
        try {
            if (!isAuthenticated(request)) {
                try {
                    status = doAuthentication(request);
                }catch (Exception ignored) {
                }
                res = forward(request,status);
                if (res != null) {
                    return res;
                }
            }
        }catch (IOException e) {
            return new ResponseEntity<>("Something Wrong",new HttpHeaders(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return null;
    }

    private static Boolean isAuthenticated(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            return JwtPubSub.getActiveTokens().contains(token);
        }
        return false;
    }

    private static JwtStatusEnum doAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            String username = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody().getSubject();
            Login login = getLogin(username);
            if (login != null) {
                return isExpired(request, login.getExpire());
            }
            return JwtStatusEnum.NotAuthorized;
        }
        return JwtStatusEnum.NotAuthorized;
    }


    private static ResponseEntity<String> forward(ServletRequest request, JwtStatusEnum status) throws IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/plain;charset=utf-8");
        switch (status){
            case Authorized:
                break;
            case Expired:
                return new ResponseEntity<>("token has expired", responseHeaders, HttpStatus.UPGRADE_REQUIRED);
            case NotAuthorized:
                return new ResponseEntity<>("", responseHeaders, HttpStatus.UNAUTHORIZED);
            case InternalServerError:
                return new ResponseEntity<>("", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
            case RevokeToken:
                new ResponseEntity<>("Token has been revoked", responseHeaders, HttpStatus.LOCKED);
        }
        return null;
    }


    // #########################
    // Authorization Functions:
    // #########################
    public static JwtStatusEnum checkAuthorization(HttpServletRequest request) {
        if (!enabledAuthorization || AuthenticationModeEnum.NONE.equals(AUTH_MODE)) return JwtStatusEnum.Authorized;
        String endpoint = request.getParameter("endpointId");
        String method = request.getParameter("method");
        String token = request.getHeader(HEADER_STRING);
        Login login = JwtPubSub.getLoginByToken(token);
        if(login==null) {
            return JwtStatusEnum.NotAuthenticated;
        }
        IGetter getter = AppConfig.getDbGetter(endpoint, method);
        if(getter==null){
            return JwtStatusEnum.NoSettings;
        }
        if(login.getRoles()==null){
            return JwtStatusEnum.NotAuthorized;
        }
        if(getter.getRoles()==null){
            return JwtStatusEnum.NoSettings;
        }
        Set<String> intersection = Sets.intersection(login.getRoles(), getter.getRoles());
        if(intersection.size() > 0){
            return JwtStatusEnum.Authorized;
        }
        return JwtStatusEnum.NotAuthorized;
    }


    // #########################
    // Login Functions:
    // #########################
    public static Login getLogin(String username){
        return JwtPubSub.getLoginByUsername(username);
    }



    // #########################
    // Password Functions:
    // #########################


    // creates token for verified users only
    public static String tryCreateToken(Login login, String password) {
        if (verifyUserPassword(password, login.getEncryptedPassword(), login.getSalt())) {
            Date expire = new DateTime(new Date()).plusDays(EXPIRE).toDate();
            login.setExpire(expire);
            String newToken = Jwts.builder().setSubject(login.getName())
                    .setExpiration(expire)
                    .signWith(SignatureAlgorithm.HS512, SECRET).compact();
            login.setOldToken(login.getToken());
            login.setToken(newToken);
            return newToken;
        }
        return null;
    }

    public static boolean verifyUserPassword(String userPassword, String encryptedPasswordFromRepo, String salt) {
        String userEncryptedPassword = generateSecurePassword(userPassword, salt);
        return userEncryptedPassword.equalsIgnoreCase(encryptedPasswordFromRepo);
    }

    public static String getSalt() {
        StringBuilder returnValue = new StringBuilder(30);
        for (int i = 0; i < 30; i++) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return new String(returnValue);
    }

    private static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error while hashing a password: " + e.getMessage(), e);
        } finally {
            spec.clearPassword();
        }
    }

    public static String generateSecurePassword(String password, String salt) {
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
        return Base64.getEncoder().encodeToString(securePassword);
    }


    public static void changeExpire(Login login, int expireInDays) {
        login.setExpire(new DateTime(new Date()).plusDays(expireInDays).toDate());
    }

    private static JwtStatusEnum isExpired(HttpServletRequest request, Date untilDate) {
        if (untilDate == null) {
            return JwtStatusEnum.RevokeToken;
        }
        String token = request.getHeader(HEADER_STRING);
        Date expirationDate = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody().getExpiration();
        if (expirationDate.compareTo(untilDate) > 0) {
            return JwtStatusEnum.Expired;
        }
        return JwtStatusEnum.Authorized;
    }


    public static void syncLogin(Login login) {
        JwtPubSub.pub_syncLogin(login);
    }


}