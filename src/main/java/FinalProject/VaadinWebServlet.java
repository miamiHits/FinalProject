package FinalProject;

import com.vaadin.server.VaadinServlet;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;

public class VaadinWebServlet extends VaadinServlet {

    private static final Logger logger = Logger.getLogger(VaadinWebServlet.class);
    @Override
    public void init() throws ServletException {
        logger.info("initializing servlet");
        super.init();
        Config.loadConfig();

    }

}
