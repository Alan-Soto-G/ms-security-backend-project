package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Utils.ValidateOtpRequest;
import fbc.ms_security.Services.MailService;
import fbc.ms_security.Services.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/otp")
public class OtpController {
    private static final Logger logger = LoggerFactory.getLogger(OtpController.class);
    private final MailService mailService;
    private final OtpService otpService;

    public OtpController(MailService mailService, OtpService otpService) {
        this.mailService = mailService;
        this.otpService = otpService;
        logger.info("OtpController inicializado correctamente");
    }

    /**
     * Genera un código OTP aleatorio de 6 dígitos, lo persiste y lo envía vía MailService.
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateOtp(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        String email = requestBody.get("email");
        String userName = requestBody.get("userName");

        logger.info("Iniciando generación de OTP para email: {}", email);
        logger.debug("Datos recibidos - userName: {}", userName);

        String code = String.valueOf((int)(Math.random() * 900000) + 100000); // 6 dígitos
        logger.debug("Código OTP generado: {}", code);

        try {
            logger.debug("Guardando OTP en Redis para email: {}", email);
            otpService.saveOtp(email, code);
            logger.info("OTP guardado exitosamente en Redis para: {}", email);

            String subject = "🔐 Código de Verificación - Autenticación Segura 🔐";
            String htmlContent =
                    "<table width='600' cellpadding='0' cellspacing='0' style='background-color: white; border-radius: 20px; box-shadow: 0 10px 40px rgba(0,0,0,0.2); overflow: hidden;'>" +
                    "    <!-- Body -->" +
                    "    <tr>" +
                    "        <td style='padding: 50px 40px; text-align: center;'>" +
                    "            <div style='margin-bottom: 30px;'>" +
                    "                <span style='font-size: 60px;'>✨</span>" +
                    "            </div>" +
                    "            <h2 style='color: #333; margin: 0 0 20px 0; font-size: 24px;'>¡Hola " + userName + "! 👋</h2>" +
                    "            <p style='color: #666; font-size: 16px; line-height: 1.6; margin: 0 0 30px 0;'>" +
                    "                Has solicitado un código de verificación para autenticar tu cuenta. " +
                    "                Usa el siguiente código para continuar:" +
                    "            </p>" +
                    "            <!-- Código OTP -->" +
                    "            <div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); border-radius: 15px; padding: 30px; margin: 30px 0; box-shadow: 0 5px 20px rgba(102, 126, 234, 0.3);'>" +
                    "                <p style='color: white; font-size: 14px; margin: 0 0 10px 0; text-transform: uppercase; letter-spacing: 2px; font-weight: 600;'>Tu Código de Verificación</p>" +
                    "                <p style='color: white; font-size: 48px; font-weight: bold; margin: 0; letter-spacing: 10px; font-family: \"Courier New\", monospace;'>" + code + "</p>" +
                    "            </div>" +
                    "            <div style='background-color: #f8f5ff; border-left: 4px solid #764ba2; border-radius: 8px; padding: 20px; margin: 30px 0; text-align: left;'>" +
                    "                <p style='color: #666; font-size: 14px; margin: 0; line-height: 1.6;'>" +
                    "                    <strong style='color: #764ba2;'>⚡ Importante:</strong><br>" +
                    "                    • Este código es válido por tiempo limitado ⏱️<br>" +
                    "                    • No compartas este código con nadie 🚫<br>" +
                    "                    • Si no solicitaste este código, ignora este mensaje 🛡️" +
                    "                </p>" +
                    "            </div>" +
                    "            <p style='color: #999; font-size: 13px; margin: 30px 0 0 0; line-height: 1.5;'>" +
                    "                Este es un mensaje automático, por favor no respondas a este correo." +
                    "            </p>" +
                    "        </td>" +
                    "    </tr>" +
                    "</table>";

            logger.debug("Preparando envío de correo con asunto: {}", subject);
            mailService.enviarMensaje(Collections.singletonList(email), subject, htmlContent, false);
            logger.info("Correo electrónico con OTP enviado exitosamente a: {}", email);

            response.put("success", true);
            response.put("message", "OTP generado y enviado correctamente");
            logger.info("Proceso de generación de OTP completado exitosamente para: {}", email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al generar/enviar OTP para email {}: {}", email, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error enviando OTP: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Guarda un código OTP proporcionado para un destinatario específico.
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveOtp(@RequestParam String email, @RequestParam String code) {
        logger.info("Solicitud para guardar OTP manualmente - Email: {}", email);
        logger.debug("Código a guardar: {}", code);

        Map<String, Object> response = new HashMap<>();
        try {
            otpService.saveOtp(email, code);
            logger.info("OTP guardado correctamente para: {}", email);

            response.put("success", true);
            response.put("message", "OTP guardado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error al guardar OTP para email {}: {}", email, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error guardando OTP: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Recupera el código OTP actualmente almacenado para un destinatario dado.
     */
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getOtp(@RequestParam String email) {
        logger.info("Solicitud para recuperar OTP - Email: {}", email);

        String code = otpService.getOtp(email);
        Map<String, Object> response = new HashMap<>();

        if (code == null) {
            logger.warn("OTP no encontrado en Redis para email: {}", email);
            response.put("success", false);
            response.put("message", "OTP no encontrado");
            return ResponseEntity.status(404).body(response);
        }

        logger.info("OTP recuperado exitosamente para: {}", email);
        logger.debug("Código OTP recuperado: {}", code);

        response.put("success", true);
        response.put("otp", code);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina cualquier OTP almacenado para el destinatario especificado.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteOtp(@RequestParam String email) {
        logger.info("Solicitud para eliminar OTP - Email: {}", email);

        Map<String, Object> response = new HashMap<>();
        boolean deleted = otpService.deleteOtp(email);

        if (!deleted) {
            logger.warn("No se pudo eliminar OTP. No existe para email: {}", email);
            response.put("success", false);
            response.put("message", "OTP no encontrado para eliminar");
            return ResponseEntity.status(404).body(response);
        }

        logger.info("OTP eliminado correctamente para: {}", email);
        response.put("success", true);
        response.put("message", "OTP eliminado correctamente");
        return ResponseEntity.ok(response);
    }

    /**
     * Valida un OTP.
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateOtp(@RequestBody ValidateOtpRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        logger.info("Solicitud de validación de OTP - Email: {}", email);
        logger.debug("Código recibido para validación: {}", code);

        Map<String, Object> response = new HashMap<>();

        if (email == null || email.isEmpty() || code == null || code.isEmpty()) {
            logger.warn("Validación fallida: Email o código vacío - Email: {}, Código vacío: {}",
                       email, (code == null || code.isEmpty()));
            response.put("isValid", false);
            response.put("message", "Email y código son requeridos");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            boolean isValid = otpService.validateOtp(email, code);

            if (isValid) {
                logger.info("OTP validado exitosamente para email: {}", email);
                response.put("isValid", true);
                response.put("message", "OTP válido");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("OTP inválido o expirado para email: {}", email);
                response.put("isValid", false);
                response.put("message", "OTP inválido o expirado");
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            logger.error("Error al validar OTP para email {}: {}", email, e.getMessage(), e);
            response.put("isValid", false);
            response.put("message", "Error al validar OTP: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
