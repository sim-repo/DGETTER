package hello.helper;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;


public class MyLogger {

    public static void info(Class clazz, String inf) {
        Logger logger = LogManager.getLogger(clazz);
        logger.info(inf);
    }

    public static void warnStartBlock(Class clazz, String header) {
        Logger logger = LogManager.getLogger(clazz);
        logger.warn("");logger.warn("");logger.warn("");
        logger.warn("##################################################################");
        logger.warn(header);
        logger.warn("------------------------------------------------------------------");
    }

    public static void warnEndBlock(Class clazz, String header) {
        Logger logger = LogManager.getLogger(clazz);
        logger.warn("------------------------------------------------------------------");
        logger.warn(header);
        logger.warn("##################################################################");

    }

    public static void warnSingleHeader(Class clazz, String header) {
        Logger logger = LogManager.getLogger(clazz);
        logger.warn("");
        logger.warn("------------------------------------------------------------------");
        logger.warn(header);
        logger.warn("------------------------------------------------------------------");
    }

    public static void warn(Class clazz, String inf) {
        Logger logger = LogManager.getLogger(clazz);
        logger.warn(inf);
    }

    public static void debug(Class clazz, String inf) {
        Logger logger = LogManager.getLogger(clazz);
        logger.debug(inf);
    }

    public static void error(Class clazz, Exception e) {
        Logger logger = LogManager.getLogger(clazz);
        StackTraceElement[] stktrace = e.getStackTrace();
        StringBuilder builder = new StringBuilder();
        logger.error("");
        logger.error("");
        builder.append(stktrace[0].toString());
        builder.append(" : "+e.getLocalizedMessage());
        logger.error(builder.toString());
    }

    public static void error(Class clazz, String e) {
        Logger logger = LogManager.getLogger(clazz);
        StringBuilder builder = new StringBuilder();
        builder.append(clazz);
        builder.append(" : "+e);
        logger.error(builder.toString());
    }

    public static void logResponse(Class clazz, String url, ResponseEntity<String> res) {
        Logger logger = LogManager.getLogger(clazz);
        String bodySubstring = "null";

        if (res != null && res.getBody() != null) {
            if (res.getBody().length() > 50 ) {
                bodySubstring = res.getBody().substring(0, 49);
            } else
            if (res.getBody().length() > 1) {
                bodySubstring = res.getBody().substring(0, 1);
            }
        }
        logger.debug(String.format("SyncCtrl %s,  %s, thread id: %s , body: %s", System.currentTimeMillis(), url,  Thread.currentThread().getId(), bodySubstring));
    }
}
