package hello.security.controller;

import hello.security.BasicAuthMgt;
import hello.security.enums.AuthenticationModeEnum;
import hello.security.model.Login;
import hello.security.JwtAuthMgt;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
public class SecurityController {


    @RequestMapping(value = "/signup", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> signup(HttpServletRequest request, HttpServletResponse response) {

        String charset = "utf-8";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/plain;charset=" + charset);

        if (JwtAuthMgt.AUTH_MODE.equals(AuthenticationModeEnum.NONE)) {
            return new ResponseEntity<>("Change Auth Mode to JWT/BASIC", responseHeaders, HttpStatus.METHOD_NOT_ALLOWED);
        }

        String username = request.getHeader("username");
        String password = request.getHeader("password");
        String expire = request.getHeader("expire");
        int expireInDays = Integer.parseInt(expire);

        if (username.equals("") || password.equals("") || expireInDays == 0) {
            return new ResponseEntity<>("user error: use username/password and expire with header request!", responseHeaders, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }
        Login login = JwtAuthMgt.getLogin(username);
        if (login != null) {
            return new ResponseEntity<>("user error: username has already registered", responseHeaders, HttpStatus.CONFLICT);
        }
        Boolean done = JwtAuthMgt.addNewLogin(username, password, expireInDays);
        if (!done) {
            return new ResponseEntity<>("error has occurred: can't generate new token", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(responseHeaders, HttpStatus.OK);
    }



    //TODO duplicate in redis amdin
    @RequestMapping(value = "/changeExpire", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> changeExpire(HttpServletRequest request, HttpServletResponse response) {

        String charset = "utf-8";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/plain;charset=" + charset);

        if (JwtAuthMgt.AUTH_MODE.equals(AuthenticationModeEnum.NONE)) {
            return new ResponseEntity<>("Change Auth Mode to JWT/BASIC", responseHeaders, HttpStatus.METHOD_NOT_ALLOWED);
        }

        String username = request.getHeader("username");
        String expire = request.getHeader("expire");
        int expireInDays = Integer.parseInt(expire);

        if (username.equals("") || expireInDays == 0) {
            return new ResponseEntity<>("user error: use username/password and expire with header request!", responseHeaders, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }
        Login login = JwtAuthMgt.getLogin(username);
        if (login == null) {
            return new ResponseEntity<>("wrong login", responseHeaders, HttpStatus.UNAUTHORIZED);
        }
        JwtAuthMgt.changeExpire(login, expireInDays);
        return new ResponseEntity<>("done", responseHeaders, HttpStatus.OK);
    }




    //#####################
    // User's Controllers
    //#####################
    @RequestMapping(value = "/changePsw", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> changePsw(HttpServletRequest request, HttpServletResponse response) {

        String charset = "utf-8";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/plain;charset=" + charset);


        if (JwtAuthMgt.AUTH_MODE.equals(AuthenticationModeEnum.NONE)) {
            return new ResponseEntity<>("Change Auth Mode to JWT/BASIC", responseHeaders, HttpStatus.METHOD_NOT_ALLOWED);
        }


        String username = request.getHeader("username");
        String oldPassword = request.getHeader("oldPassword");
        String newPassword = request.getHeader("newPassword");

        if (username.equals("") || oldPassword.equals("") || newPassword.equals("")) {
            return new ResponseEntity<>("use login/psw with header request!", responseHeaders, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }
        Login login = JwtAuthMgt.getLogin(username);

        if (login == null) {
            return new ResponseEntity<>("wrong login", responseHeaders, HttpStatus.UNAUTHORIZED);
        }

        if (JwtAuthMgt.AUTH_MODE.equals(AuthenticationModeEnum.JWT)) {
            String token = JwtAuthMgt.tryChangePsw(login, oldPassword, newPassword);
            if (token == null || token.equals("")) {
                return new ResponseEntity<>("wrong login or old password", responseHeaders, HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(token, responseHeaders, HttpStatus.OK);
        }

        if (JwtAuthMgt.AUTH_MODE.equals(AuthenticationModeEnum.BASIC)) {
            Boolean done = BasicAuthMgt.tryChangePsw(login, oldPassword, newPassword);
            if (!done) {
                return new ResponseEntity<>("wrong login or old password", responseHeaders, HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>("done", responseHeaders, HttpStatus.OK);
        }
        return new ResponseEntity<>("bad", responseHeaders, HttpStatus.BAD_REQUEST);
    }

}

