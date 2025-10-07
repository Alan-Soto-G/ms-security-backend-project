package fbc.ms_security.Controllers;

import fbc.ms_security.Services.MailService;
import fbc.ms_security.Services.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador que gestiona las operaciones de Contraseña de Un Solo Uso (OTP), incluyendo:
 * - Generación de un nuevo OTP y envío por email/SMS.
 * - Guardado explícito de un código OTP.
 * - Recuperación del OTP actual.
 * - Eliminación de un OTP existente.
 * - Validación de un OTP proporcionado.
 *
 * Todos los endpoints están expuestos bajo la ruta '/otp'.
 */
@CrossOrigin(origins = "${frontend.url}")
@RestController
@RequestMapping("/api/public/otp")
public class OtpController {

    /**
     * Servicio responsable de enviar notificaciones de OTP por email o SMS.
     */
    @Autowired
    private MailService mailService;

    /**
     * Servicio que maneja la lógica de generación, almacenamiento, recuperación,
     * eliminación y validación de códigos OTP.
     */
    private final OtpService otpService;

    /**
     * Constructor del controlador con la dependencia del servicio OTP requerida.
     *
     * @param otpService el servicio que gestiona la persistencia y validación de OTP
     */
    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    /**
     * Genera un código OTP aleatorio de 6 dígitos, lo persiste y lo envía vía MailService.
     *
     * @param requestBody contiene email y userName del destinatario
     * @return el código OTP generado (devuelto para pruebas; eliminar o enmascarar en producción)
     */
    @PostMapping("/generate")
    public String generateOtp(@RequestBody Map<String, String> requestBody) {
        String code = String.valueOf((int)(Math.random() * 900000) + 100000); // 6 dígitos
        String email = requestBody.get("email");
        String userName = requestBody.get("userName");
        otpService.saveOtp(email, code);
        System.out.println("✅ OTP GENERADO para " + email + ": " + code);

        // Subject del correo
        String subject = "🔐 Código de Verificación - Autenticación Segura";

        // HTML Template bonito con colores blanco y morado
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

        this.mailService.enviarMensaje(email, subject, htmlContent, false);
        return code; // solo para pruebas
    }

    /**
     * Guarda un código OTP proporcionado para un destinatario específico.
     *
     * @param emailOrPhone dirección de email o número de teléfono del destinatario
     * @param code         el código OTP a guardar en el almacenamiento del backend
     * @return HTTP 200 OK tras el guardado exitoso
     */
    @PostMapping("/save")
    public ResponseEntity<Void> saveOtp(
            @RequestParam String emailOrPhone,
            @RequestParam String code) {
        otpService.saveOtp(emailOrPhone, code);
        return ResponseEntity.ok().build();
    }

    /**
     * Recupera el código OTP actualmente almacenado para un destinatario dado.
     *
     * @param emailOrPhone dirección de email o número de teléfono del destinatario
     * @return HTTP 200 con el código OTP si se encuentra; HTTP 404 Not Found en caso contrario
     */
    @GetMapping("/get")
    public ResponseEntity<String> getOtp(@RequestParam String emailOrPhone) {
        String code = otpService.getOtp(emailOrPhone);
        if (code == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(code);
    }

    /**
     * Elimina cualquier OTP almacenado para el destinatario especificado.
     *
     * @param emailOrPhone dirección de email o número de teléfono del destinatario
     * @return HTTP 204 No Content tras la eliminación exitosa
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteOtp(@RequestParam String emailOrPhone) {
        otpService.deleteOtp(emailOrPhone);
        return ResponseEntity.noContent().build();
    }

    /**
     * Valida un código OTP proporcionado contra el código almacenado.
     *
     * @param emailOrPhone dirección de email o número de teléfono del destinatario
     * @param code         el código OTP a validar
     * @return true si el código proporcionado coincide con el OTP almacenado y es válido; false en caso contrario
     */
    @PostMapping("/validate")
    public boolean validateOtp(@RequestParam String emailOrPhone, @RequestParam String code) {
        return otpService.validateOtp(emailOrPhone, code);
    }
}
