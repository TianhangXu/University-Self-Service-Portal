package controller;

import external.AuthenticationService;
import external.EmailService;
import model.AuthenticatedUser;
import model.SharedContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import view.View;


/**
 * Controller for guests. Provides functionality for logging in.
 */
public class GuestController extends Controller {
    /**
     * Instantiates a new guest controller.
     *
     * @param sharedContext the shared context
     * @param view          the user interface view
     * @param auth          the authentication service
     * @param email         the email of the user
     */
    public GuestController(SharedContext sharedContext, View view, AuthenticationService auth, EmailService email) {
        super(sharedContext, view, auth, email);
    }

    /**
     * logs in the current user, converting them to {@link AuthenticatedUser} if successful
     */
    public void login() {
        String username = view.getInput("Enter your username: ");
        String password = view.getInput("Enter your password: ");
        String response = auth.login(username, password);

        JSONParser parser = new JSONParser();
        String email = null;
        String role = null;
        try {
            JSONObject result = (JSONObject) parser.parse(response);
            if (result.containsKey("error")) {
                String errorMessage = (String) result.get("error");
                view.displayError(errorMessage);
                return;
            }
            email = (String) result.get("email");
            role = (String) result.get("role");
        } catch (ParseException e) {
            view.displayException(e);
        }

        try {
            sharedContext.currentUser = new AuthenticatedUser(email, role);
        } catch (IllegalArgumentException e) {
            view.displayException(e);
        }

        view.displaySuccess("Logged in as " + username);
    }

}
