package FinalProject;

import com.vaadin.server.*;
import com.vaadin.ui.UI;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VaadinWebServlet extends VaadinServlet implements SessionInitListener, SessionDestroyListener{

    public static final String IS_SESSION_VALID = "IS_SESSION_VALID";

    private static Set<VaadinSession> allValidSessions = new HashSet<>();
    private static VaadinSession currentValidSession = null;

    private static final Logger logger = Logger.getLogger(VaadinWebServlet.class);

    @Override
    public void init() throws ServletException {
        logger.info("initializing servlet");
        super.init();
        Config.loadConfig();
    }

    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().addSessionInitListener(this);
        getService().addSessionDestroyListener(this);
    }

    @Override
    public void sessionDestroy(SessionDestroyEvent event) {
        allValidSessions.remove(event.getSession());
        if (currentValidSession.equals(event.getSession()))
        {
            currentValidSession = null;
        }
    }

    @Override
    public void sessionInit(SessionInitEvent event) throws ServiceException {
        allValidSessions.add(event.getSession());
        if (currentValidSession == null)
        {
            currentValidSession = event.getSession();
            currentValidSession.setAttribute(IS_SESSION_VALID, true);
        }
        else if (!currentValidSession.equals(event.getSession()))
        {
            event.getSession().setAttribute(IS_SESSION_VALID, false);
        }
    }

    public static void clearAllOtherSessionButOne(VaadinSession remainingSession)
    {
        currentValidSession = remainingSession;
        remainingSession.setAttribute(IS_SESSION_VALID, true);
        allValidSessions.remove(remainingSession);
        for (VaadinSession session : allValidSessions)
        {//detach all UIs
            Collection<UI> sessionUIs = session.getUIs();
            for (UI ui : sessionUIs)
            {
                ui.access(() -> {
                    ui.close();
                });
            }
        }

        try {
            Thread.sleep(500);//wait for the UIs to detach before closing all sessions
        } catch (InterruptedException e) {
            logger.warn("failed sleeping when waiting for ui to be detached");
        }

        for (VaadinSession session : allValidSessions)
        {//close sessions
            session.close();
        }

        allValidSessions.add(remainingSession);
    }
}
