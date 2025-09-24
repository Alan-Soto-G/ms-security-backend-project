package fbc.ms_security.Controllers;

import fbc.ms_security.Services.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/mail")
public class MailController {

    @Autowired
    private MailService mailService;

    /**
     * Endpoint para enviar un correo.
     * Recibe JSON con "to", "subject" y "mensaje".
     * Ejemplo de JSON:
     * {
     *   "to": "destino@example.com",
     *   "subject": "Asunto",
     *   "mensaje": "Contenido del correo"
     * }
     * Método POST /api/mail
     */
    @PostMapping
    public String enviarCorreo(@RequestBody Map<String, String> payload) {
        String to = payload.get("to");
        String subject = payload.get("subject");
        String mensaje = payload.get("mensaje");

        if (to == null || subject == null || mensaje == null) {
            return "Error: faltan campos requeridos (to, subject, mensaje)";
        }

        return mailService.enviarMensaje(to, subject, mensaje);
    }
}
