package hello.security;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import hello.config.AppConfig;
import hello.security.enums.AuthenticationModeEnum;
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
    private
    AppConfig appConfig;

    private ConcurrentHashMap<String, Login> loginMap = new ConcurrentHashMap<>();

    private static final Random RANDOM = new SecureRandom();
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;

    private static final Integer EXPIRATION = 2; // in days

    private static final String SECRET = "ThisIsASecret";

    private static final String TOKEN_PREFIX = "Bearer";

    private static final String HEADER_STRING = "Authorization";


    private static AuthenticationModeEnum AUTH_MODE = AuthenticationModeEnum.BASIC;

    // Authorization Mode
    private static Boolean enabledAuthorization = false;



    // #########################
    // New user registration -
    // create new login with temporary password and generate temporary token:
    // #########################
    public Boolean addNewLogin(HttpServletRequest request, String username, String psw, int expireInDays) {
        String salt = getSalt();
        String encryptedPsw = generateSecurePassword(psw, salt);
        int expireDays = expireInDays == 0 ? EXPIRATION : expireInDays;
        Date expire = new DateTime(new Date()).plusDays(expireDays).toDate();
        Login login = new Login(username, encryptedPsw, salt, expire);
        saveLogin(request, login);
        this.loginMap.put(login.getLogin(), login);
        return true;
    }


    // #########################
    // Authentication Functions:
    // #########################

    // returns HTTP-status code if authentication == fail or
    // null if authentication == success
    public ResponseEntity<String> checkAuthentication(HttpServletRequest request) {
        switch (AUTH_MODE) {
            case NONE:
                return null;
            case JWT:
                return checkJWT(request);
            case BASIC:
                return checkBasic(request);
        }
        return null;
    }


    private ResponseEntity<String> checkBasic(HttpServletRequest request) {
        final String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.toLowerCase().startsWith("basic")) {
            String base64Credentials = authorization.substring("Basic".length()).trim();
            byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
            String credentials = new String(credDecoded, StandardCharsets.UTF_8);
            final String[] values = credentials.split(":", 2);
            System.out.println(values[0]+":"+values[1]);
        }
        return null;
    }

    private ResponseEntity<String> checkJWT(HttpServletRequest request) {
        ResponseEntity<String> res;
        JwtStatusEnum status = JwtStatusEnum.UnAuhorized;
        try {
            if (!AuthSessionListener.isAuthenticated(request.getSession().getId())) {
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

    private JwtStatusEnum doAuthentication(HttpServletRequest request) {
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


    private ResponseEntity<String> forward(ServletRequest request, JwtStatusEnum status) throws IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/plain;charset=utf-8");
        switch (status){
            case Authorized:
                AuthSessionListener.setAuthenticated(req.getSession().getId());
                break;
            case Expired:
                return new ResponseEntity<>("token has expired", responseHeaders, HttpStatus.UPGRADE_REQUIRED);
            case UnAuhorized:
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
    public ResponseEntity<String> checkAuthorization(HttpServletRequest request) {
        if (!enabledAuthorization || AuthenticationModeEnum.NONE.equals(AUTH_MODE)) return null;
        String endpoint = request.getParameter("endpointId");
        String method = request.getParameter("method");

        //TODO
        return null;
    }




    // #########################
    // Login Functions:
    // #########################

    public Login getLogin(String username){
        return this.loginMap.get(username);
    }


    private void pubLoginToRepository(Login login) {
        appConfig.pubLogins(login);
    }


    // #########################
    // Password Functions:
    // #########################

    public String tryChangePsw(HttpServletRequest request, Login login, String oldPassword, String newPassword) {
        String token = createToken(login, oldPassword);
        if (token == null) {
            return null;
        }
        String salt = getSalt();
        login.setSalt(salt);
        String encryptedPassword = generateSecurePassword(newPassword, salt);
        login.setPsw(encryptedPassword);
        saveLogin(request, login);
        return token;
    }

    public String tryRefreshToken(HttpServletRequest request, Login login, String password) {
        String token = createToken(login, password);
        if (token == null) {
            return null;
        }
        saveLogin(request, login);
        return token;
    }


    private String createToken(Login login, String password) {
        if (verifyUserPassword(password, login.getPsw(), login.getSalt())) {
            Date expire = new DateTime(new Date()).plusDays(EXPIRATION).toDate();
            login.setExpire(expire);
            return Jwts.builder().setSubject(login.getLogin())
                    .setExpiration(expire)
                    .signWith(SignatureAlgorithm.HS512, SECRET).compact();
        }
        return null;
    }

    private boolean verifyUserPassword(String providedPassword, String securedPassword, String salt) {
        String newSecurePassword = generateSecurePassword(providedPassword, salt);
        return newSecurePassword.equalsIgnoreCase(securedPassword);
    }

    private String getSalt() {
        StringBuilder returnValue = new StringBuilder(30);
        for (int i = 0; i < 30; i++) {
            returnValue.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return new String(returnValue);
    }

    private byte[] hash(char[] password, byte[] salt) {
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

    private String generateSecurePassword(String password, String salt) {
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
        return Base64.getEncoder().encodeToString(securePassword);
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


    private void saveLogin(HttpServletRequest request, Login login) {
        AuthSessionListener.setAuthenticated(request.getSession().getId());
        pubLoginToRepository(login);
        loginMap.put(login.getLogin(), login);
    }

}