package learn.epam.com.config;

import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer implements WebApplicationInitializer {
    private static final String DISPATCHER = "dispatcher";
    private static final int FIRST = 1;
    private static final String SLASH = "/";
    private static final String ALL = "/*";
    private static final String HIDDEN_HTTP_METHOD_FILTER = "hiddenHttpMethodFilter";

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        FilterRegistration.Dynamic hidden = servletContext.addFilter(
                HIDDEN_HTTP_METHOD_FILTER, new HiddenHttpMethodFilter());

        hidden.addMappingForUrlPatterns(null, true, ALL);

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(ApplicationConfig.class, WebMvcConfig.class);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(context);

        ServletRegistration.Dynamic dispatcher = servletContext.addServlet(DISPATCHER, dispatcherServlet);
        dispatcher.setLoadOnStartup(FIRST);
        dispatcher.addMapping(SLASH);
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{ApplicationConfig.class}; // database, services, DAO, etc.
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[]{WebMvcConfig.class}; // controllers + Web MVC
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{SLASH};
    }
}
