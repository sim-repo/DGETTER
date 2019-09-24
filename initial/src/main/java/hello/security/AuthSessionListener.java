package hello.security;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;

@WebListener("authSessionListener")
public class AuthSessionListener implements HttpSessionListener {

    private final AtomicInteger counter = new AtomicInteger();

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        System.out.println("NEW SESSION: " + event.getSession().getId());
        //  event.getSession().setMaxInactiveInterval(1*60);
        counter.incrementAndGet();
        updateSessionCounter(event);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        System.out.println("DEINIT SESSION: " + event.getSession().getId());
        counter.decrementAndGet();
        updateSessionCounter(event);
    }

    private void updateSessionCounter(HttpSessionEvent httpSessionEvent) {
        httpSessionEvent.getSession().getServletContext()
                .setAttribute("activeSession", counter.get());
        System.out.println("Total active session are {} + " + counter.get());
    }
}
