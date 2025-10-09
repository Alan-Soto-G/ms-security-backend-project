package fbc.ms_security.Controllers;

import fbc.ms_security.Services.MailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/public/email")
public class MailController {
    private final MailService mailService;

    public MailController(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> enviarCorreo(@RequestBody Map<String, Object> payload) {
        Object recipientsObj = payload.get("recipients");
        String subject = (String) payload.get("subject");
        String content = (String) payload.get("content");
        boolean is_Html = payload.get("isHtml") != null && (Boolean) payload.get("isHtml");

        if (recipientsObj == null || subject == null || content == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Error: faltan campos requeridos"));
        }

        List<String> recipients = new ArrayList<>();
        if (recipientsObj instanceof String) {
            recipients.add((String) recipientsObj);
        } else if (recipientsObj instanceof List) {
            recipients = (List<String>) recipientsObj;
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Error: recipients debe ser un string o una lista"));
        }

        String result = mailService.enviarMensaje(recipients, subject, content, is_Html);
        return ResponseEntity.ok(Map.of("message", result));
    }
}