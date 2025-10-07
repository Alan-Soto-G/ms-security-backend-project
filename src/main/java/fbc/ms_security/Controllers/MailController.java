package fbc.ms_security.Controllers;

import fbc.ms_security.Services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "${frontend.url}")
@RestController
@RequestMapping("/api/public/email")
public class MailController {

    @Autowired
    private MailService mailService;

    @PostMapping
    public String enviarCorreo(@RequestBody Map<String, Object> payload) {
        Object recipientsObj = payload.get("recipients");
        String subject = (String) payload.get("subject");
        String content = (String) payload.get("content");
        boolean is_Html = payload.get("isHtml") != null && (Boolean) payload.get("isHtml");

        if (recipientsObj == null || subject == null || content == null) {
            return "Error: faltan campos requeridos (recipients, subject, content)";
        }

        List<String> recipients = new ArrayList<>();

        // Manejar tanto String como List
        if (recipientsObj instanceof String) {
            // Si es un solo email como string
            recipients.add((String) recipientsObj);
        } else if (recipientsObj instanceof List) {
            // Si es una lista de emails
            recipients = (List<String>) recipientsObj;
        } else {
            return "Error: recipients debe ser un string o una lista de strings";
        }

        return mailService.enviarMensaje(recipients, subject, content, is_Html);
    }
}