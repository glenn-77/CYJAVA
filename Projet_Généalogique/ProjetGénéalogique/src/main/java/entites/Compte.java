package entites;

/**
 * Represents a user account with login credentials and contact information.
 * Each account is assigned a unique, auto-incremented number.
 */
public class Compte {
    private String login;
    private String motDePasse;
    private final String numero;
    private String email;
    private String telephone;
    private String adresse;
    private boolean isPremiereConnexion;
    private static int compteur = 17;

    /**
     * Constructs a new Compte (account) with the specified parameters.
     * The account number is automatically generated.
     *
     * @param login      the login username
     * @param motDePasse the password
     * @param email      the email address
     * @param telephone  the phone number
     * @param adresse    the postal address
     */
    public Compte(String login, String motDePasse, String email, String telephone, String adresse) {
        this.login = login;
        this.motDePasse = motDePasse;
        this.numero = String.format("%03d", compteur);
        compteur++;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.isPremiereConnexion = true;
    }

    /**
     * Gets the current counter used for generating account numbers.
     *
     * @return the current counter value
     */
    public static int getCompteur() {
        return compteur;
    }

    /**
     * Sets a new value for the account number counter.
     *
     * @param nvcompteur the new counter value
     */
    public static void setCompteur(int nvcompteur) {
        Compte.compteur = nvcompteur;
    }

    /**
     * Gets the login username of the account.
     *
     * @return the login username
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets the login username of the account.
     *
     * @param login the new login username
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Gets the password of the account.
     *
     * @return the password
     */
    public String getMotDePasse() {
        return motDePasse;
    }

    /**
     * Sets the password of the account.
     *
     * @param motDePasse the new password
     */
    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    /**
     * Gets the unique number of the account.
     *
     * @return the account number
     */
    public String getNumero() {
        return numero;
    }

    /**
     * Gets the email address associated with the account.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the account.
     *
     * @param email the new email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the telephone number associated with the account.
     *
     * @return the telephone number
     */
    public String getTelephone() {
        return telephone;
    }

    /**
     * Sets the telephone number of the account.
     *
     * @param telephone the new telephone number
     */
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    /**
     * Gets the postal address of the account.
     *
     * @return the postal address
     */
    public String getAdresse() {
        return adresse;
    }

    /**
     * Sets the postal address of the account.
     *
     * @param adresse the new postal address
     */
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    /**
     * Checks if this is the user's first login.
     *
     * @return true if it is the first login, false otherwise
     */
    public boolean isPremiereConnexion() {
        return this.isPremiereConnexion;
    }

    /**
     * Sets whether this is the user's first login.
     *
     * @param premiereConnexion true if first login, false otherwise
     */
    public void setPremiereConnexion(boolean premiereConnexion) {
        isPremiereConnexion = premiereConnexion;
    }
}
