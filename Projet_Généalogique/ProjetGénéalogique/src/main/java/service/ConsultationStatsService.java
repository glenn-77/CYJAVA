package service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour gérer les statistiques des consultations d'arbre généalogique.
 */
public class ConsultationStatsService {

    private static final String CSV_FILE = "Projet_Généalogique/ProjetGénéalogique/ressources/consultations.csv";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Ajoute une consultation d'arbre dans le fichier.
     *
     * @param nssArbre     NSS de l'arbre consulté
     * @param nssConsultant NSS de l'utilisateur consultant
     */
    public void ajouterConsultation(String nssArbre, String nssConsultant) {
        // Valider les paramètres NSS pour éviter les écritures incorrectes
        if (nssArbre == null || nssConsultant == null || nssArbre.isEmpty() || nssConsultant.isEmpty()) {
            System.err.println("❌ Les NSS fournis ne peuvent pas être nuls ou vides.");
            return;
        }

        // Préparer les données à écrire dans le fichier
        String dateConsultation = LocalDate.now().format(DATE_FORMAT);
        String ligneAÉcrire = String.join(",", nssArbre, nssConsultant, dateConsultation);

        // Écrire dans le fichier avec garantie d'une nouvelle ligne après chaque écriture
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_FILE), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(ligneAÉcrire);
            writer.newLine(); // Garantir que la ligne est correctement terminée par un saut de ligne
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'écriture dans le fichier consultations.csv : " + e.getMessage());
        }
    }

    /**
     * Récupère les fréquences de consultation (par mois ou par année).
     *
     * @param nssArbre NSS de l'arbre consulté
     * @param parMois  Si vrai, regroupe par mois, sinon par année
     * @return Une carte où la clé est le mois/année et la valeur est le nombre de consultations
     */
    public Map<String, Long> calculerFrequences(String nssArbre, boolean parMois) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(CSV_FILE))) {
            return reader.lines()
                    .map(line -> line.split(","))
                    .filter(values -> values.length == 3 && values[0].equals(nssArbre))
                    .map(values -> LocalDate.parse(values[2], DATE_FORMAT))
                    .collect(Collectors.groupingBy(date -> {
                        if (parMois) {
                            return date.getYear() + "-" + String.format("%02d", date.getMonthValue());
                        } else {
                            return String.valueOf(date.getYear());
                        }
                    }, Collectors.counting()));
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture des statistiques : " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * Récupère les utilisateurs ayant consulté un arbre et leur fréquence de consultation.
     *
     * @param nssArbre NSS de l'arbre concerné
     * @return Une carte où la clé est le NSS de l'utilisateur et la valeur est le nombre de consultations
     */
    public Map<String, Long> recupererConsultationsParUtilisateur(String nssArbre) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(CSV_FILE))) {
            return reader.lines()
                    .map(line -> line.split(","))
                    .filter(values -> values.length == 3 && values[0].equals(nssArbre))
                    .collect(Collectors.groupingBy(values -> values[1], Collectors.counting()));
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture des consultations : " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}