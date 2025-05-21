package service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing statistics related to genealogical tree consultations.
 * Stores and analyzes consultation data in a CSV file.
 */
public class ConsultationStatsService {

    private static final String CSV_FILE = "Projet_Généalogique/ProjetGénéalogique/ressources/consultations.csv";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Records a tree consultation event by writing it to the CSV file.
     *
     * @param nssArbre      the NSS of the tree being consulted
     * @param nssConsultant the NSS of the user performing the consultation
     */
    public void ajouterConsultation(String nssArbre, String nssConsultant) {
        // Valider les paramètres NSS pour éviter les écritures incorrectes
        if (nssArbre == null || nssConsultant == null || nssArbre.isEmpty() || nssConsultant.isEmpty()) {
            System.err.println("❌ Les NSS fournis ne peuvent pas être nuls ou vides.");
            return;
        }

        // Préparer les données à écrire dans le fichier
        String dateConsultation = LocalDate.now().format(DATE_FORMAT);
        String ligneAecrire = String.join(",", nssArbre, nssConsultant, dateConsultation);

        // Écrire dans le fichier avec garantie d'une nouvelle ligne après chaque écriture
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_FILE), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(ligneAecrire);
            writer.newLine(); // Garantir que la ligne est correctement terminée par un saut de ligne
        } catch (IOException e) {
            System.err.println("❌ Erreur lors de l'écriture dans le fichier consultations.csv : " + e.getMessage());
        }
    }

    /**
     * Calculates consultation frequencies of a specific genealogical tree, grouped by month or year.
     *
     * @param nssArbre the NSS of the consulted tree
     * @param parMois  if true, groups by month; otherwise, groups by year
     * @return a map where the key is a month/year and the value is the count of consultations
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
     * Retrieves the users who consulted a tree and the number of times they did so.
     *
     * @param nssArbre the NSS of the consulted tree
     * @return a map where the key is the consultant's NSS and the value is the consultation count
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