package model;

/**
 * Authenticated user within the system, has role which must be one of the following:
 * <ul>
 *     <li>AdminStaff</li>
 *     <li>TeachingStaff</li>
 *     <li>Student</li>
 * </ul>
 */
public class AuthenticatedUser extends User {
    private final String email;
    private final String role;

    /**
     *
     * @param email the email of the user
     * @param role  the role of the user, must be "AdminStaff", "TeachingStaff" or "Student"
     */
    public AuthenticatedUser(String email, String role) {
        if (email == null) {
            throw new IllegalArgumentException("User email cannot be null!");
        }
        if (role == null || (!role.equals("AdminStaff") && !role.equals("TeachingStaff") && !role.equals("Student"))) {
            throw new IllegalArgumentException("Unsupported user role " + role);
        }
        this.email = email;
        this.role = role;
    }

    /**
     * @return return the email of the user
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return the role of the user
     */
    public String getRole() {
        return role;
    }
}
