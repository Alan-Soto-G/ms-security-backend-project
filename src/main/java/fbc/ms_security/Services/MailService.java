package fbc.ms_security.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);
    private static final String SEND_EMAIL_ENDPOINT = "/send-email";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ms-notifications.api.url}")
    private String notificationsUrl;

    public String enviarMensaje(List<String> recipients, String subject, String content, boolean is_Html) {
        Map<String, Object> requestBody = new HashMap<>();
        // Normalizar: si es 1 destinatario enviar string, si son varios enviar lista
        if (recipients.size() == 1) {
            requestBody.put("recipients", recipients.get(0));
        } else {
            requestBody.put("recipients", recipients);
        }
        requestBody.put("subject", subject);
        requestBody.put("content", content);
        requestBody.put("is_html", is_Html);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            String fullUrl = notificationsUrl + SEND_EMAIL_ENDPOINT;
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                logger.info("✅ Email enviado correctamente");
                return "Mensaje(s) enviado(s) correctamente: " + response.getBody();
            } else {
                logger.warn("⚠️ Error en envío: Código {}", response.getStatusCode());
                return "Error en envío: Código " + response.getStatusCode();
            }
        } catch (Exception e) {
            logger.error("❌ Error al conectar con Flask: {}", e.getMessage(), e);
            return "Error en la conexión al servidor Flask: " + e.getMessage();
        }
    }
}
