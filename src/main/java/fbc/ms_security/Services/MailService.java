package fbc.ms_security.Services;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class MailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ms-notifications.api.url}")
    private String notificationsUrl;

    public String enviarMensaje(List<String> recipients, String subject, String content, boolean is_Html) {
        String endpoint = "/send-email"; // ✅ Endpoint unificado

        Map<String, Object> requestBody = new HashMap<>();

        // ✅ Normalizar: si es 1 destinatario enviar string, si son varios enviar lista
        if (recipients.size() == 1) {
            requestBody.put("recipients", recipients.get(0)); // string singular
        } else {
            requestBody.put("recipients", recipients); // lista
        }

        requestBody.put("subject", subject);
        requestBody.put("content", content);
        requestBody.put("is_html", is_Html); // ✅ Indicar que el contenido es HTML

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            String fullUrl = notificationsUrl + endpoint;
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("✅ Email enviado correctamente");
                return "Mensaje(s) enviado(s) correctamente: " + response.getBody();
            } else {
                System.err.println("⚠️ Error en envío: Código " + response.getStatusCode());
                return "Error en envío: Código " + response.getStatusCode();
            }
        } catch (Exception e) {
            System.err.println("❌ Error al conectar con Flask: " + e.getMessage());
            e.printStackTrace();
            return "Error en la conexión al servidor Flask: " + e.getMessage();
        }
    }

    // Método de conveniencia para un solo email
    public String enviarMensaje(String to, String subject, String content, boolean is_Html) {
        return enviarMensaje(Arrays.asList(to), subject, content, is_Html);
    }
}
