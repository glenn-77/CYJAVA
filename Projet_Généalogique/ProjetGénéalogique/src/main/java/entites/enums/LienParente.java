package entites.enums;

/**
 * Enumeration of family relationship types between two individuals in the genealogical tree.
 * Used to define the direction and nature of links.
 */
public enum LienParente {
    /** Indicates the person is the father of another. */
    PERE,

    /** Indicates the person is the mother of another. */
    MERE,

    /** Indicates the person is the son of another. */
    FILS,

    /** Indicates the person is the daughter of another. */
    FILLE,

    /** Special link used when a person is being registered (e.g., in a request). */
    INSCRIPTION
}
