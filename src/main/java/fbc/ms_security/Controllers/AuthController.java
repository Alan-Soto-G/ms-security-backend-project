package fbc.ms_security.Controllers;

import fbc.ms_security.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");

        try {
            Map<String, Object> response = authService.loginWithGoogle(idToken);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Detecta si el mensaje menciona token vencido o inválido
            String message = e.getMessage().toLowerCase();
            if (message.contains("vencido") || message.contains("expirado")) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token de Google vencido"));
            } else if (message.contains("inválido")) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token de Google inválido"));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", e.getMessage()));
            }
        }
    }
}
