package hello.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@WebListener("authSessionListener")
public class AuthSessionListener implements HttpSessionListener {

    private static final ConcurrentHashMap<String, Boolean> sessions = new ConcurrentHashMap<String, Boolean>();

    private static final Logger LOG= LoggerFactory.getLogger(AuthSessionListener.class);

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        System.out.println("NEW SESSION: "+event.getSession().getId());
        sessions.put(event.getSession().getId(), false);
      //  event.getSession().setMaxInactiveInterval(1*60);
        counter.incrementAndGet();
        updateSessionCounter(event);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        System.out.println("DEINIT SESSION: "+event.getSession().getId());
        sessions.remove(event.getSession().getId());
        counter.decrementAndGet();
        updateSessionCounter(event);
    }

    private void updateSessionCounter(HttpSessionEvent httpSessionEvent){
        httpSessionEvent.getSession().getServletContext()
                .setAttribute("activeSession", counter.get());
        System.out.println("Total active session are {} + "+counter.get());
        System.out.println("Total stored session are {} + "+sessions.size());
    }

    public static Boolean isAuthenticated(String sessionId) {
        if (sessions.containsKey(sessionId)) {
            return sessions.get(sessionId);
        }
        return false;
    }

    public static void setAuthenticated(String sessionId) {
        sessions.put(sessionId, true);
    }

}