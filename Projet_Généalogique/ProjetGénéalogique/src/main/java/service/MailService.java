package service;
import io.github.cdimascio.dotenv.Dotenv;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import java.io.IOException;

public class MailService {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("Projet_Généalogique/ProjetGénéalogique")
            .filename(".env")
            .load();

    ;  // charge le .env
    private static final String SENDGRID_API_KEY = dotenv.get("SENDGRID_API_KEY");

    public static void envoyerEmail(String destinataire, String sujet, String contenu) {
        Email from = new Email("diffoglenn007@gmail.com");
        Email to = new Email(destinataire);
        Content content = new Content("text/plain", contenu);
        Mail mail = new Mail(from, sujet, to, content);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("Statut: " + response.getStatusCode());
            System.out.println("Body: " + response.getBody());
            System.out.println("Headers: " + response.getHeaders());
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("❌ Erreur lors de l'envoi du mail via SendGrid");
        }
    }
}
