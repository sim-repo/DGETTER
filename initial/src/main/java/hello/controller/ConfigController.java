package hello.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


import javax.servlet.http.HttpServletRequest;

@Controller
public class ConfigController {

    @RequestMapping(value = "/conf/red", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = "text/plain;charset=Windows-1251")
    public ResponseEntity<String> confRed(HttpServletRequest request, @RequestBody String url) {
        HttpHeaders headers = new HttpHeaders();
        try {
            System.out.println(url);
            return new ResponseEntity<String>("", headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<String>(e.getCause().toString(), headers, HttpStatus.BAD_REQUEST);
        }
    }
}
