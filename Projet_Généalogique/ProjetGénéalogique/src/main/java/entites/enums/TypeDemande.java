package entites.enums;

/**
 * Enumeration representing the types of administrative requests
 * that a user can submit for tree management.
 */
public enum TypeDemande {

    /** Request to add a new relationship (e.g., parent, child). */
    AJOUT_LIEN,

    /** Request to remove an existing relationship. */
    SUPPRESSION_LIEN,

    /** Request to modify personal information (e.g., nationality). */
    MODIFICATION_INFO,

    /** Request to add a new person to the genealogical tree. */
    AJOUT_PERSONNE,

    /** Request to remove a person from the genealogical tree. */
    SUPPRESSION_PERSONNE
}
