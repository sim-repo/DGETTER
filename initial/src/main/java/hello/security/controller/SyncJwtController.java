package hello.security.controller;

import hello.security.model.Login;
import hello.security.AuthMgt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
public class SyncJwtController {

    @Autowired
    private AuthMgt authMgt;

    //TODO duplicate in redis amdin
    @RequestMapping(value = "/signup", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> signup(HttpServletRequest request, HttpServletResponse response) {

        String charset = "utf-8";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/plain;charset=" + charset);

        String username = request.getHeader("username");
        String password = request.getHeader("password");
        String expire = request.getHeader("expire");
        int expireInDays = Integer.parseInt(expire);

        if (username.equals("") || password.equals("") || expireInDays == 0) {
            return new ResponseEntity<>("user error: use username/password and expire with header request!", responseHeaders, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }
        Login login = authMgt.getLogin(username);
        if (login != null) {
            return new ResponseEntity<>("user error: username has already registered", responseHeaders, HttpStatus.CONFLICT);
        }
        Boolean done = authMgt.addNewLogin(username, password, expireInDays);
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

        String username = request.getHeader("username");
        String expire = request.getHeader("expire");
        int expireInDays = Integer.parseInt(expire);

        if (username.equals("") || expireInDays == 0) {
            return new ResponseEntity<String>("user error: use username/password and expire with header request!", responseHeaders, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }
        Login login = authMgt.getLogin(username);
        if (login == null) {
            return new ResponseEntity<String>("wrong login", responseHeaders, HttpStatus.UNAUTHORIZED);
        }
        authMgt.changeExpire(login, expireInDays);
        return new ResponseEntity<String>("done", responseHeaders, HttpStatus.OK);
    }


    //#####################
    // User's Controllers
    //#####################
    @RequestMapping(value = "/changePsw", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> changePsw(HttpServletRequest request, HttpServletResponse response) {

        String charset = "utf-8";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/plain;charset=" + charset);

        String username = request.getHeader("username");
        String oldPassword = request.getHeader("oldPassword");
        String newPassword = request.getHeader("newPassword");

        if (username.equals("") || oldPassword.equals("") || newPassword.equals("")) {
            return new ResponseEntity<>("use login/psw with header request!", responseHeaders, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }
        Login login = authMgt.getLogin(username);

        if (login == null) {
            return new ResponseEntity<>("wrong login", responseHeaders, HttpStatus.UNAUTHORIZED);
        }
        String token = authMgt.tryChangePsw(login, oldPassword, newPassword);
        if (token == null || token.equals("")) {
            return new ResponseEntity<>("wrong login or old password", responseHeaders, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(token, responseHeaders, HttpStatus.OK);
    }


    @RequestMapping(value = "/signin", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> signin(HttpServletRequest request, HttpServletResponse response) {

        String charset = "utf-8";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/plain;charset=" + charset);

        String username = request.getHeader("username");
        String password = request.getHeader("password");

        if (username.equals("") || password.equals("")) {
            return new ResponseEntity<>("use login/psw with header request!", responseHeaders, HttpStatus.NON_AUTHORITATIVE_INFORMATION);
        }
        Login login = authMgt.getLogin(username);

        if (login == null) {
            return new ResponseEntity<>("wrong login", responseHeaders, HttpStatus.UNAUTHORIZED);
        }
        String token = authMgt.tryRecreateToken(login, password);
        if (token == null || token.equals("")) {
            return new ResponseEntity<>("wrong login or old password", responseHeaders, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(token, responseHeaders, HttpStatus.OK);
    }
}

