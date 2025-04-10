package controller;

import external.AuthenticationService;
import external.EmailService;
import model.SharedContext;
import view.View;

/**
 * Abstract base class for all controller types in the application.
 */
public abstract class Controller {
    protected final SharedContext sharedContext;
    protected final View view;
    protected final AuthenticationService auth;
    protected final EmailService email;

    /**
     * Instantiates a new controller.
     *
     * @param sharedContext the shared context
     * @param view          the user interface view
     * @param auth          the authentication service
     * @param email         the email of the user
     */
    protected Controller(SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        this.sharedContext = sharedContext;
        this.view = view;
        this.auth = auth;
        this.email = email;
    }

    /**
     * Displays the given options and prompts user to select one.
     *
     * @param options       the array of options that can be selected
     * @param exitOption    the label shown for the exit option
     * @param <T>           the type of the items in the options array
     * @return              the index of the selected option or {@code -1} to indicate exit
     */

    protected <T> int selectFromMenu(T[] options, String exitOption) {
        while (true) {
            view.displayDivider();
            int i = 0;
            for (T option : options) {
                view.displayInfo("[" + i + "] " + option);
                i++;
            }
            view.displayInfo("[-1] " + exitOption);
            view.displayDivider();
            String input = view.getInput("Please choose an option: ");
            try {
                int optionNo = Integer.parseInt(input);
                if (optionNo == -1) {
                    return -1;
                }
                if (optionNo >= 0 && optionNo < options.length) {
                    return optionNo;
                }
                view.displayError("Invalid option " + optionNo);
            } catch (NumberFormatException e) {
                view.displayError("Invalid option " + input);
            }
        }
    }
}
