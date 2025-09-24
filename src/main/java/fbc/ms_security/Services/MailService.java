package fbc.ms_security.Services;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class MailService {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String flaskUrl = "http://127.0.0.1:5000/enviarCorreo";

    public String enviarMensaje(String to, String subject, String mensaje) {
        // Crear el cuerpo JSON
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("to", to);
        requestBody.put("subject", subject);
        requestBody.put("mensaje", mensaje);

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Crear request
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            // Enviar POST
            ResponseEntity<String> response = restTemplate.postForEntity(flaskUrl, request, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return "Mensaje enviado correctamente: " + response.getBody();
            } else {
                return "Error en envío: Código " + response.getStatusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error en la conexión al servidor Flask: " + e.getMessage();
        }
    }
}
