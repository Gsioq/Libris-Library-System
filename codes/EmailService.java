package application;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailService {

    private static final String EMAIL = "librisdlms@gmail.com";
    private static final String PASSWORD = "rwfo milc rich spqy";

    public static boolean sendOTP(String recipientEmail, String otp) {
        try {
        	Properties props = new Properties();

        	props.put("mail.smtp.auth", "true");
        	props.put("mail.smtp.starttls.enable", "true");
        	props.put("mail.smtp.host", "smtp.gmail.com");
        	props.put("mail.smtp.port", "587");

        	// trust Gmail's cert
        	props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        	// fail faster instead of hanging forever
        	props.put("mail.smtp.connectiontimeout", "10000");
        	props.put("mail.smtp.timeout", "10000");
        	props.put("mail.smtp.writetimeout", "10000");

            Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(EMAIL, PASSWORD);
                    }
                }
            );

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(recipientEmail)
            );

            message.setSubject("LIBRIS Email Verification OTP");

            message.setText(
                "Your LIBRIS verification code is: " + otp
            );

            Transport.send(message);

            return true;

        } catch (Exception e) {
        	 e.printStackTrace();
            return false;
        }
    }
    
    public static void sendRoleInviteEmail(
            String recipientEmail,
            String role,
            String code
    ) {
        try {
        	Properties props = new Properties();

        	props.put("mail.smtp.auth", "true");
        	props.put("mail.smtp.starttls.enable", "true");
        	props.put("mail.smtp.host", "smtp.gmail.com");
        	props.put("mail.smtp.port", "587");

        	// trust Gmail certificate
        	props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        	// timeout settings
        	props.put("mail.smtp.connectiontimeout", "10000");
        	props.put("mail.smtp.timeout", "10000");
        	props.put("mail.smtp.writetimeout", "10000");

            Session session = Session.getInstance(
                props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                EMAIL,
                                PASSWORD
                        );
                    }
                }
            );

            Message message = new MimeMessage(session);

            message.setFrom(
                new InternetAddress(EMAIL)
            );

            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(recipientEmail)
            );

            message.setSubject(
                "LIBRIS Role Invitation"
            );

            message.setText(
                "Hello,\n\n" +
                "You have been invited to register as: " + role + "\n\n" +
                "Your invite code is: " + code + "\n\n" +
                "Use this code during registration.\n\n" +
                "Welcome to LIBRIS!"
            );

            Transport.send(message);

            System.out.println(
                "Role invitation email sent successfully."
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}