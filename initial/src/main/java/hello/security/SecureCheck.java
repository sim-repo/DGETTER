package hello.security;

import java.util.*;
import javax.servlet.http.HttpServletRequest;
import com.google.common.collect.Sets;
import hello.config.AppConfig;
import hello.model.getter.IGetter;
import hello.security.enums.AuthenticationModeEnum;
import hello.security.enums.JwtStatusEnum;
import hello.security.model.Login;


public class SecureCheck {

    private static final String HEADER_AUTH_NAME = "Authorization";

    static AuthenticationModeEnum AUTH_MODE = AuthenticationModeEnum.BASIC;
    static Boolean enabledAuthorization = false;


    public static String checkAuthentication(HttpServletRequest request) {
        switch (AUTH_MODE) {
            case NONE:
                return null;
            case JWT:
                String token = request.getHeader(HEADER_AUTH_NAME);
                return JwtSub.getActiveTokens().contains(token) ? null : "";
            case BASIC:
                String encodedPassword = request.getHeader(HEADER_AUTH_NAME);
                if(encodedPassword == null) {
                    return "Error: no header parameter - "+ HEADER_AUTH_NAME;
                }
                if (!BasicMgt.getActiveEncodedPasswords().contains(encodedPassword)) {
                    return "Error: login or password incorrect";
                }
                return null;
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

        IGetter getter = AppConfig.getDbGetter(endpoint, method);
        if(getter==null){
            return JwtStatusEnum.NoSettings;
        }

        if(getter.getRoles()==null || getter.getRoles().size() == 0){
            return JwtStatusEnum.Authorized;
        }


        String token = request.getHeader(HEADER_AUTH_NAME);

        Login login = null;
        if (SecureCheck.AUTH_MODE.equals(AuthenticationModeEnum.JWT)) {
            login = JwtSub.getLoginByToken(token);
            if(login==null) {
                return JwtStatusEnum.NotAuthenticated;
            }
        }

        if (SecureCheck.AUTH_MODE.equals(AuthenticationModeEnum.BASIC)) {
            String encodedPassword = getBasicEncodedPassword(request);
            login = BasicMgt.getLoginByEncodedPassword(encodedPassword);
        }

        if(login == null || login.getRoles()==null){
            return JwtStatusEnum.NotAuthorized;
        }

        Set<String> intersection = Sets.intersection(login.getRoles(), getter.getRoles());
        if(intersection.size() > 0){
            return JwtStatusEnum.Authorized;
        }
        return JwtStatusEnum.NotAuthorized;
    }

    private static String getBasicEncodedPassword(HttpServletRequest request){
        return request.getHeader(SecureCheck.HEADER_AUTH_NAME);
    }
}