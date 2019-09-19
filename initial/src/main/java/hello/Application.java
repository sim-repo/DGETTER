package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;


@ServletComponentScan
@SpringBootApplication
@RestController
public class Application extends SpringBootServletInitializer {

	@RequestMapping("/")
	public String home() {
		return "Hello Docker World";
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}



	public static class ApplicationInitializer extends AbstractAnnotationConfigDispatcherServletInitializer  {

//		@Override
//		public void onStartup(ServletContext servletContext) throws ServletException {
//
//			servletContext
//					.addFilter("permitFilter", new DelegatingFilterProxy("permitAll"))
//					.addMappingForUrlPatterns(null, false, "secure/security/signup")
//			;
//			servletContext
//					.addFilter("authFiler", new DelegatingFilterProxy("springSecurityFilterChain"))
//					.addMappingForUrlPatterns(null, false, "/expired")
//					;
//
//			super.onStartup(servletContext);
//		}

//		@Override
//		protected javax.servlet.Filter[] getServletFilters() {
//			DelegatingFilterProxy delegateFilterProxy = new DelegatingFilterProxy();
//			delegateFilterProxy.setTargetBeanName("authFiler");
//			return new Filter[] { delegateFilterProxy };
//		}

		@Override
		protected Class<?>[] getRootConfigClasses() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected Class<?>[] getServletConfigClasses() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected String[] getServletMappings() {
			// TODO Auto-generated method stub
			return null;
		}
	}



}