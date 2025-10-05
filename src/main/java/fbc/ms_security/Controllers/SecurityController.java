package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Permission;
import fbc.ms_security.Models.User;
import fbc.ms_security.Repositories.UserRepository;
import fbc.ms_security.Services.EncryptionService;
import fbc.ms_security.Services.JwtService;
import fbc.ms_security.Services.ValidatorsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/public/security")
public class SecurityController {
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private EncryptionService theEncryptionService;
    @Autowired
    private JwtService theJwtService;
    @Autowired
    private ValidatorsService theValidatorsService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @PostMapping("permissions-validation")
    public boolean permissionsValidation(final HttpServletRequest request,
                                         @RequestBody Permission thePermission) {
        return this.theValidatorsService.validationRolePermission(request,thePermission.getUrl(),thePermission.getMethod());
    }

    /**
     * POST /api/public/security/verify-credentials
     * Verifica las credenciales del usuario (email y contraseña).
     * Retorna el usuario si las credenciales son correctas, error 401 si no.
     *
     * Request body: { "email": "usuario@example.com", "password": "123456" }
     *
     * @param credentials Usuario con email y password
     * @return ResponseEntity con el usuario (sin password) si es válido
     */
    @PostMapping("verify-credentials")
    public ResponseEntity<Map<String, Object>> verifyCredentials(@RequestBody User credentials) {
        // Buscar usuario por email
        User theActualUser = this.theUserRepository.getUserByEmail(credentials.getEmail());

        // Validar que el usuario existe y la contraseña coincide
        if (theActualUser != null &&
                theActualUser.getPassword().equals(theEncryptionService.convertSHA256(credentials.getPassword()))) {

            // Limpiar la contraseña por seguridad
            theActualUser.setPassword("");

            // Retornar usuario válido
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "user", theActualUser
            ));
        } else {
            // Credenciales inválidas
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "valid", false,
                            "error", "Invalid email or password"
                    ));
        }
    }

    /**
     * POST /api/public/security/generate-token
     * Genera un token JWT para un usuario ya autenticado.
     * Retorna el token, fecha de expiración y datos del usuario.
     *
     * Request body: { "_id": "id_del_usuario" }
     *
     * @param user Usuario con el _id
     * @return ResponseEntity con token, expiración y usuario
     */
    @PostMapping("generate-token")
    public ResponseEntity<Map<String, Object>> generateToken(@RequestBody User user) {
        // Buscar usuario completo por ID
        User theActualUser = this.theUserRepository.findById(user.get_id()).orElse(null);

        if (theActualUser == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "User not found"
                    ));
        }

        // Generar token JWT
        String token = this.theJwtService.generateToken(theActualUser);

        // Calcular fecha de expiración
        Date expirationDate = new Date(System.currentTimeMillis() + jwtExpiration);

        // Limpiar contraseña por seguridad
        theActualUser.setPassword("");

        // Retornar respuesta con token, expiración y usuario
        return ResponseEntity.ok(Map.of(
                "token", token,
                "expiration", expirationDate,
                "user", theActualUser
        ));
    }

    /**
     * POST /api/public/security/login
     * Endpoint completo de login que verifica credenciales y genera token en un solo paso.
     *
     * Request body: { "email": "usuario@example.com", "password": "123456" }
     *
     * @param theNewUser Usuario con email y password
     * @param response Respuesta HTTP
     * @return HashMap con token, usuario y expiración
     */
    @PostMapping("login")
    public HashMap<String,Object> login(@RequestBody User theNewUser,
                                        final HttpServletResponse response) throws IOException {
        HashMap<String,Object> theResponse = new HashMap<>();
        User theActualUser = this.theUserRepository.getUserByEmail(theNewUser.getEmail());

        if(theActualUser != null &&
                theActualUser.getPassword().equals(theEncryptionService.convertSHA256(theNewUser.getPassword()))) {
            // Generar token JWT
            String token = theJwtService.generateToken(theActualUser);

            // Calcular fecha de expiración
            Date expirationDate = new Date(System.currentTimeMillis() + jwtExpiration);

            // Limpiar contraseña por seguridad
            theActualUser.setPassword("");

            // Construir respuesta
            theResponse.put("token", token);
            theResponse.put("user", theActualUser);
            theResponse.put("expiration", expirationDate);

            return theResponse;
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return theResponse;
        }
    }
}
