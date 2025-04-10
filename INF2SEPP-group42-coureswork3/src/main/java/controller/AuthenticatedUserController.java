package controller;

import external.AuthenticationService;
import external.EmailService;
import model.Guest;
import model.SharedContext;
import view.View;

/**
 * Controller for authenticated users. Provides functionality for logging out.
 */
public class AuthenticatedUserController extends Controller {
    /**
     * Instantiates a new authenticated user controller.
     *
     * @param sharedContext the shared context
     * @param view          the user interface view
     * @param auth          the authentication service
     * @param email         the email of the user
     */
    public AuthenticatedUserController(SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        super(sharedContext, view, auth, email);
    }

    /**
     * Logs the current user out, converting them to {@link Guest}  if successful.
     */
    public void logout() {
        if (sharedContext.currentUser instanceof Guest) {
            view.displayError("Guest users cannot logout!");
            return;
        }
        sharedContext.currentUser = new Guest();
        view.displaySuccess("Logged out!");
    }
}
