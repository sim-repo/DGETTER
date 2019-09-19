package hello.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


public class SecurityConfig {

}
//@Configuration
//public class SecurityConfig extends WebSecurityConfigurerAdapter
//{
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.
//                .antMatcher("/foo/**")
//                .authorizeRequests()
//                .antMatchers("/foo/bar").hasRole("BAR")
//                .antMatchers("/foo/spam").hasRole("SPAM")
//                .anyRequest().authenticated()
//                .and()
//                .csrf().disable()
//                .authorizeRequests().anyRequest().authenticated()
//                .and()
//                .httpBasic();
//                //.and()
//                //.rememberMe().key("rem-me-key").tokenValiditySeconds(20);
//    }
//
//
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//
//        auth.inMemoryAuthentication()
//                .withUser("user").password("{noop}user").roles("BAR")
//                .and()
//                .withUser("admin").password("{noop}admin").roles("SPAM");
//    }
//}