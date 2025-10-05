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

    // Agregado 05/10/2025 - Endpoint específico para autenticación con Microsoft
    // Mantiene la misma estructura que Google para consistencia en la API
    @PostMapping("/microsoft")
    public ResponseEntity<?> loginWithMicrosoft(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");

        try {
            Map<String, Object> response = authService.loginWithMicrosoft(idToken);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("vencido") || message.contains("expirado")) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token de Microsoft vencido"));
            } else if (message.contains("inválido")) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token de Microsoft inválido"));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", e.getMessage()));
            }
        }
    }
}

// Agregado 05/10/2025 - Nuevo controlador para endpoints públicos de autenticación
// Permite autenticación con cualquier proveedor OAuth configurado sin necesidad de autenticación previa
@CrossOrigin
@RestController
@RequestMapping("/api/public")
class PublicAuthController {

    @Autowired
    private AuthService authService;

    // Agregado 05/10/2025 - Endpoint genérico que acepta múltiples proveedores OAuth
    // Resuelve el problema de CORS permitiendo autenticación desde frontend Angular
    @PostMapping("/auth")
    public ResponseEntity<?> loginWithProvider(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");
        String provider = request.get("provider");

        if (idToken == null || idToken.trim().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Token requerido"));
        }

        if (provider == null || provider.trim().isEmpty()) {
            provider = "google"; // Default para compatibilidad
        }

        try {
            Map<String, Object> response = authService.loginWithProvider(idToken, provider);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            String message = e.getMessage().toLowerCase();
            if (message.contains("vencido") || message.contains("expirado")) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token vencido"));
            } else if (message.contains("inválido")) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token inválido"));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", e.getMessage()));
            }
        }
    }
}
