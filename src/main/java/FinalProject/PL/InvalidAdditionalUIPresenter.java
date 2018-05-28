package FinalProject.PL;

import FinalProject.VaadinWebServlet;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;

import java.util.Collection;

public class InvalidAdditionalUIPresenter extends Panel implements View {

    private final Navigator navigator;

    public InvalidAdditionalUIPresenter(Navigator navigator)
    {
        this.navigator = navigator;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        VerticalLayout mainLayout = new VerticalLayout();

        Label errorMessageLbl = new Label(
                "You cannot Have more than one experiment running!<br>" +
                        "Please close any open tab browsing to SHAS.<br>" +
                        "Either manually or by clicking the below button", ContentMode.HTML);
        Button closeAllUIs = new Button("Close Any Other UI");
        closeAllUIs.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                UiHandler.service.stopExperiment();
                UiHandler.currentRunningPresenter = null;
                VaadinWebServlet.clearAllOtherSessionButOne(getSession());
                Collection<UI> UIs = getSession().getUIs();
                for (UI ui: UIs ) {
                    ui.access(() -> {
                        ui.close();
                    });
                }
                Page.getCurrent().reload();
            }
        });
        mainLayout.addComponent(errorMessageLbl);
        mainLayout.setComponentAlignment(errorMessageLbl, Alignment.MIDDLE_CENTER);
        mainLayout.addComponent(closeAllUIs);
        mainLayout.setComponentAlignment(closeAllUIs, Alignment.MIDDLE_CENTER);

        setContent(mainLayout);
        setSizeFull();
    }
}
