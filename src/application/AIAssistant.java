package application;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AIAssistant {
	private static final String API_KEY = "AIzaSyBF_Y9r9KCq3TGQkAnlORRJGZuC14UGO2s";

    public static void askAI(String question) {
        try {
            String endpoint =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
                + API_KEY;

            URL url = new URL(endpoint);
            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );

            conn.setDoOutput(true);

            String jsonInput = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "%s"
                    }
                  ]
                }
              ]
            }
            """.formatted(question);

            OutputStream os = conn.getOutputStream();
            os.write(jsonInput.getBytes());
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            String line;
            StringBuilder response =
                    new StringBuilder();

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            br.close();

            System.out.println("AI Response:");
            System.out.println(response.toString());

        } catch (Exception e) {
            System.out.println("AI assistant failed.");
            e.printStackTrace();
        }
    }

    // JAVAFX AI CALL (Returns response as String for UI display)
    public static String askAIForUI(String question) {
        try {
            String endpoint =
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
                + API_KEY;

            URL url = new URL(endpoint);
            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty(
                    "Content-Type",
                    "application/json"
            );

            conn.setDoOutput(true);

            String jsonInput = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "%s"
                    }
                  ]
                }
              ]
            }
            """.formatted(question.replace("\"", "\\\""));

            OutputStream os = conn.getOutputStream();
            os.write(jsonInput.getBytes());
            os.flush();
            os.close();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            String line;
            StringBuilder response = new StringBuilder();

            while ((line = br.readLine()) != null) {
                response.append(line);
            }

            br.close();

            // Extract text from JSON response
            String raw = response.toString();
            int start = raw.indexOf("\"text\": \"") + 9;
            int end = raw.indexOf("\"", start);
            if (start > 8 && end > start) {
                return raw.substring(start, end)
                          .replace("\\n", "\n")
                          .replace("\\\"", "\"");
            }

            return raw;

        } catch (Exception e) {
            return "AI assistant failed: " + e.getMessage();
        }
    }
}