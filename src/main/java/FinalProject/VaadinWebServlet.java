package FinalProject;

import com.vaadin.server.VaadinServlet;

import javax.servlet.ServletException;

public class VaadinWebServlet extends VaadinServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        Config.loadConfig();
    }

}
