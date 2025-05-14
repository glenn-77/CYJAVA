/**
 * 
 */
/**
 * 
 */
module ProjetGénéalogique {
    requires javafx.controls; // Nécessaire pour les composants JavaFX
    requires  transitive javafx.graphics;
    requires jakarta.mail;
    requires java.desktop;// Nécessaire pour l'application JavaFX
    
    exports test; // Exporter le package test où se trouve Main
}

