package hello.security;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hello.config.AppConfig;
import hello.helper.MyLogger;
import hello.security.enums.JwtStatusEnum;
import hello.security.model.Login;
import io.jsonwebtoken.*;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component("authMgt")
@Scope("singleton")
public class AuthMgt {

    @Autowired
    AppConfig appConfig;

    ConcurrentHashMap<String, Login> loginHashMap= new ConcurrentHashMap<String, Login>();

    private static final Random RANDOM = new SecureRandom();
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    static final Integer EXPIRATIONDAY = 2; // in days

    static final String SECRET = "ThisIsASecret";

    static final String TOKEN_PREFIX = "Bearer";

    static final String HEADER_STRING = "Authorization";

    // Login Functions:
    public void cleanupLogins(){
        this.loginHashMap.clear();
    }

    public void setLoginHashMap(String key, Login login){
        this.loginHashMap.put(key, login);
    }

    public Login getLogin(String username){
        return this.loginHashMap.get(username);
    }

    public List<String> getLogins(){
        List<String> res = new ArrayList<String>();
        for (Map.Entry<String, Login> entry : loginHashMap.entrySet()) {
            res.add(entry.getKey());
        }
        return res;
    }

    public void pubLoginToRepository(Login login) {
        appConfig.pubLogins(login);
    }


    // Password Functions:
    public String getSalt(int length) {
        StringBuilder returnValue = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return new String(returnValue);
    }

    public byte[] hash(char[] password, byte[] salt) {
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

    public String generateSecurePassword(String password, String salt) {
        String returnValue = null;
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
        returnValue = Base64.getEncoder().encodeToString(securePassword);

        return returnValue;
    }

    public boolean verifyUserPassword(String providedPassword, String securedPassword, String salt) {
        boolean returnValue = false;

        String newSecurePassword = generateSecurePassword(providedPassword, salt);

        returnValue = newSecurePassword.equalsIgnoreCase(securedPassword);

        return returnValue;
    }


    public void changeExpire(Login login, int expireInDays) {
        login.setExpire(new DateTime(new Date()).plusDays(expireInDays).toDate());
    }

    private JwtStatusEnum isExpired(HttpServletRequest request, Date untilDate) {
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


    public JwtStatusEnum doAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HEADER_STRING);
        if (token != null) {
            String username = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody().getSubject();
            Login login = getLogin(username);
            if (login != null) {
                return isExpired(request, login.getExpire());
            }
            return JwtStatusEnum.UnAuhorized;
        }
        return JwtStatusEnum.UnAuhorized;
    }


    public ResponseEntity<String> forward(ServletRequest request, JwtStatusEnum status) throws IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/plain;charset=utf-8");
        switch (status){
            case Authorized:
                AuthSessionListener.setAuthenticated(req.getSession().getId());
                break;
            case Expired:
                return new ResponseEntity<String>("token has expired", responseHeaders, HttpStatus.UPGRADE_REQUIRED);
            case UnAuhorized:
                return new ResponseEntity<String>("", responseHeaders, HttpStatus.UNAUTHORIZED);
            case InternalServerError:
                return new ResponseEntity<String>("", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
            case RevokeToken:
                new ResponseEntity<String>("Token has been revoked", responseHeaders, HttpStatus.LOCKED);
        }
        return null;
    }


    // CREATE PASSWORD:
    public String getFirstToken(HttpServletRequest request, String username, String psw, int expireInDays) {
        String salt = getSalt(30);
        String encryptedPsw = generateSecurePassword(psw, salt);
        Integer expireDays = expireInDays == 0 ? EXPIRATIONDAY : expireInDays;
        Date expire = new DateTime(new Date()).plusDays(expireDays).toDate();
        Login login = new Login(username, encryptedPsw, salt, expire);

        try {
            saveLogin(request, login);
            setLoginHashMap(login.getLogin(), login);
            return  Jwts.builder().setSubject(username)
                    .setExpiration(expire)
                    .signWith(SignatureAlgorithm.HS512, SECRET).compact();
        } catch (Exception e) {
            StackTraceElement[] stktrace = e.getStackTrace();
            StringBuilder builder = new StringBuilder();
            builder.append(stktrace[0].toString());
            builder.append(" : "+e.getLocalizedMessage());
            MyLogger.error(AuthMgt.class, builder.toString());
            System.out.println(builder.toString());
        }
        return "";
    }



    // CHANGE PASSWORD:
    public String tryChangePsw(HttpServletRequest request, Login login, String oldPassword, String newPassword) {
        if (verifyUserPassword(oldPassword, login.getPsw(), login.getSalt()) == true) {
            return doChangePsw(request, login, newPassword);
        }
        return "";
    }

    private String doChangePsw(HttpServletRequest request, Login login, String newPassword) {
        String salt = getSalt(30);
        String encryptedPassword = generateSecurePassword(newPassword, salt);
        Date expire = new DateTime(new Date()).plusDays(EXPIRATIONDAY).toDate();
        login.setSalt(salt);
        login.setPsw(encryptedPassword);
        login.setExpire(expire);

        try {
            saveLogin(request, login);
            return  Jwts.builder().setSubject(login.getLogin())
                    .setExpiration(expire)
                    .signWith(SignatureAlgorithm.HS512, SECRET).compact();
        } catch (Exception e) {
            MyLogger.error(AuthMgt.class, e);
        }
        return "";
    }

    private void saveLogin(HttpServletRequest request, Login login) {
        AuthSessionListener.setAuthenticated(request.getSession().getId());
        pubLoginToRepository(login);
        loginHashMap.put(login.getLogin(), login);
    }
}