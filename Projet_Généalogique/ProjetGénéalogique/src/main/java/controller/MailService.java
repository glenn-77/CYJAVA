package controller;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class MailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String EMAIL_SENDER = "glenndiffo8@gmail.com"; // ← ton email ici
    private static final String EMAIL_PASSWORD = "Bigboss001+"; // ← mot de passe d'application

    public static void envoyerEmail(String destinataire, String sujet, String corps) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));

        Session session = Session.getInstance(props,
                new jakarta.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL_SENDER, EMAIL_PASSWORD);
                    }
                }
        );

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_SENDER));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(destinataire)
            );
            message.setSubject(sujet);
            message.setText(corps);

            Transport.send(message);
            System.out.println("✅ Email envoyé à " + destinataire);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de l'envoi de l'email");
        }
    }
}

