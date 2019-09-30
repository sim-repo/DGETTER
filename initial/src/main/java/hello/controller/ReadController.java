package hello.controller;

import hello.helper.ObjectConverter;
import hello.security.SecureCheck;
import hello.security.enums.JwtStatusEnum;
import hello.service.GetterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import org.apache.commons.codec.binary.Base64;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ReadController {


    @Autowired
    private GetterService getterService;

    private static ResponseEntity<String> getBadResponse(String absentParam){
        return new ResponseEntity<>("Error: required parameter is not specified: "+absentParam, HttpStatus.BAD_REQUEST);
    }

    private String validate(HttpServletRequest request) {
        String param = request.getParameter("method");
        if (param == null || param.equals("")) {
            return "method";
        }

        param = request.getParameter("endpointId");
        if (param == null || param.equals("")) {
            return "endpointId";
        }
        return null;
    }


    @RequestMapping(value = "/db", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> jsonDBUniGet(HttpServletRequest request){

        // 1. check authentication
        String invalidStr = SecureCheck.checkAuthentication(request);
        if (invalidStr != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("WWW-Authenticate", "");
            return new ResponseEntity<>(invalidStr, headers, HttpStatus.UNAUTHORIZED);
        }

        // 2. check request-format
        String absent = validate(request);
        if (absent != null) {
            String body = getBadResponse(absent).getBody();
            return new ResponseEntity<>(body, new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }
        if(request.getQueryString()==null) {
            return new ResponseEntity<>("Error: query string is null", new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        // 3. check authorization
        String endpoint = request.getParameter("endpointId");
        String method = request.getParameter("method");

        JwtStatusEnum status = SecureCheck.checkAuthorization(request);
        if (!status.equals(JwtStatusEnum.Authorized)) {
            return new ResponseEntity<>(status.toValue(), new HttpHeaders(), HttpStatus.FORBIDDEN);
        }

        try {
            String body = getterService.exec(endpoint, method, request.getQueryString());
            return new ResponseEntity<>(body, new HttpHeaders(), HttpStatus.OK);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }
    }


    //TODO add authorization by token
    @RequestMapping(value = "/db64", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public @ResponseBody String jsonDBUni64Get(HttpServletRequest request){
        String res;
        try {
            Enumeration enumeration = request.getParameterNames();
            Map<String, String> modelMap = new HashMap<>();
            while (enumeration.hasMoreElements()) {
                String parameterName = (String) enumeration.nextElement();
                String val = request.getParameter(parameterName);
                if (!ObjectConverter.isNumeric(val)) {
                    if (Base64.isBase64(val)) {
                        byte[] converted = Base64.decodeBase64(val.getBytes());
                        val = new String(converted, StandardCharsets.UTF_8);
                    }
                }
                modelMap.put(parameterName, val);
            }

            String absent = validate(request);
            if (absent != null) {
                return getBadResponse(absent).getBody();
            }
            String endpoint = request.getParameter("endpointId");
            String method = request.getParameter("method");
            res = getterService.exec(endpoint, method, modelMap);
        } catch (Exception ex) {
            res = ex.getMessage();
        }
        return res;
    }

}
