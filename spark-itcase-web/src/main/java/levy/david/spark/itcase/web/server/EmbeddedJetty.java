package levy.david.spark.itcase.web.server;

import java.io.File;
import java.io.IOException;

import javax.servlet.MultipartConfigElement;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class EmbeddedJetty {

    private static final int DEFAULT_PORT = 10080;
    private static final String CONTEXT_PATH = "/";
    private static final String CONFIG_LOCATION = "eu.kielczewski.example.config";
    private static final String MAPPING_URL = "/*";
    private static final String DEFAULT_PROFILE = "dev";

    public static void main(String[] args) throws Exception {
        new EmbeddedJetty().startJetty(getPortFromArgs(args));
    }

    private static int getPortFromArgs(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.valueOf(args[0]);
            } catch (NumberFormatException ignore) {
            }
        }
        return DEFAULT_PORT;
    }

    private void startJetty(int port) throws Exception {
        Server server = new Server(port);

        
        server.setHandler(getServletContextHandler(getContext()));
        server.start();
        server.join();
    }

    private static ServletContextHandler getServletContextHandler(WebApplicationContext context) throws IOException {
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setErrorHandler(null);
        contextHandler.setContextPath(CONTEXT_PATH);
        ServletHolder holder = new ServletHolder(new DispatcherServlet(context));
        
//        String uploadLocation = null;
//        //if (HAVE CONFIG PARAM) {
//        	uploadLocation = System.getProperty("user.home") + File.separator + ".itcase-temp";
//        	File uploadLocationFile = new File(uploadLocation);
//        	if (!uploadLocationFile.exists()) {
//        		uploadLocationFile.mkdir();
//        	} else if (!uploadLocationFile.isDirectory()){ 
//        		throw new RuntimeException("Unable to write to artifact temporary directory");
//        	}
//        //}
//        
//        MultipartConfigElement element = new MultipartConfigElement(uploadLocation);
//        holder.getRegistration().setMultipartConfig(element);
        contextHandler.addServlet(holder, MAPPING_URL);
        contextHandler.addEventListener(new ContextLoaderListener(context));
        contextHandler.setResourceBase(new ClassPathResource("/webapp").getURI().toString());
        return contextHandler;
    }

    private static WebApplicationContext getContext() {
    	XmlWebApplicationContext context = new XmlWebApplicationContext();
    	context.setConfigLocations("classpath:spark-itcase-servlet.xml");
    	//context.start();
    	return context;
    	
    	//        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
//        context.setConfigLocation(CONFIG_LOCATION);
//        context.getEnvironment().setDefaultProfiles(DEFAULT_PROFILE);
//        return context;
    }

}
