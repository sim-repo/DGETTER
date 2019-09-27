package hello.controller;

import hello.security.JwtAuthMgt;
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


@RestController
@RequestMapping(path = "/foo")
public class EmployeeController {

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
}