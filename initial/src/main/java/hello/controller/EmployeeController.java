package hello.controller;

import hello.security.AuthMgt;
import hello.security.AuthSessionListener;
import hello.security.enums.JwtStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RestController
@RequestMapping(path = "/foo")
public class EmployeeController {

    @Autowired
    AuthMgt authMgt;

    @GetMapping(path="/", produces = "application/json")
    public String getEmployees()
    {
        return "hello";
    }

    @GetMapping("/bar")
    @ResponseBody
    public String retrieveMaxSessionIncativeInterval(HttpSession session) {
        //session.setMaxInactiveInterval(1*20);
        return "Max Inactive Interval before Session expires: " + session.getMaxInactiveInterval();
    }

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);

    @GetMapping("/spam")
    public ResponseEntity<String> testSessionListner(HttpServletRequest request, HttpServletResponse response){
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/plain;charset=utf-8");
        ResponseEntity<String> res = checkAuth(request);
        if (res != null) {
            return res;
        }
        System.out.println("PROCESSING.....");
        return new ResponseEntity<String>("ok", responseHeaders, HttpStatus.OK);
    }


    private ResponseEntity<String> checkAuth(HttpServletRequest request) {
        ResponseEntity<String> res = null;
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Content-Type", "text/plain;charset=utf-8");
        JwtStatusEnum status = JwtStatusEnum.UnAuhorized;
        try {
            if (AuthSessionListener.isAuthenticated(request.getSession().getId()) == false) {
                try {
                    status = authMgt.doAuthentication(request);
                }catch (Exception e) {
                    status =  JwtStatusEnum.UnAuhorized;
                }
                res = authMgt.forward(request,status);
                if (res != null) {
                    return res;
                }
            }
        }catch (IOException e) {
        }
        return null;
    }
}