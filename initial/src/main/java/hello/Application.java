package hello;

import hello.config.AppConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@ServletComponentScan
@SpringBootApplication
@RestController
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@PropertySource({ "classpath:redis.properties" })
public class Application extends SpringBootServletInitializer {

	@RequestMapping("/")
	public String home() {
		return "Hello Docker World";
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		ApplicationContext context
				= new AnnotationConfigApplicationContext(Application.class);
		String redisURL = System.getProperty("redisURL");
		AppConfig appConfig = context.getBean(AppConfig.class);
		Environment env = context.getBean(Environment.class);
		String url = env.getProperty("redisUrl");

		appConfig.setup(redisURL==null ? url: redisURL);
	}

}