package entites.enums;

/**
 * Represents the status of an administrative request.
 */
public enum Statut {
    /** Request is pending and has not yet been processed. */
    EN_ATTENTE,

    /** Request has been accepted by the administrator. */
    ACCEPTEE,

    /** Request has been rejected by the administrator. */
    REFUSEE
}
