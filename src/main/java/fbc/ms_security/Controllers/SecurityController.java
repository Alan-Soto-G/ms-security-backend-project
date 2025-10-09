package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Entities.Permission;
import fbc.ms_security.Models.Entities.User;
import fbc.ms_security.Repositories.UserRepository;
import fbc.ms_security.Services.EncryptionService;
import fbc.ms_security.Services.JwtService;
import fbc.ms_security.Services.UserService;
import fbc.ms_security.Services.ValidatorsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/security/")
public class SecurityController {
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private final UserRepository theUserRepository;
    private final EncryptionService theEncryptionService;
    private final JwtService theJwtService;
    private final ValidatorsService theValidatorsService;
    private final UserService userService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    public SecurityController(UserRepository theUserRepository,
                            EncryptionService theEncryptionService,
                            JwtService theJwtService,
                            ValidatorsService theValidatorsService,
                            UserService userService) {
        this.theUserRepository = theUserRepository;
        this.theEncryptionService = theEncryptionService;
        this.theJwtService = theJwtService;
        this.theValidatorsService = theValidatorsService;
        this.userService = userService;
        logger.info("SecurityController inicializado correctamente");
    }

    @PostMapping("/permissions-validation")
    public boolean permissionsValidation(final HttpServletRequest request,
                                         @RequestBody Permission thePermission) {
        logger.info("Validando permisos para URL: {} y método: {}", thePermission.getUrl(), thePermission.getMethod());
        boolean isValid = this.theValidatorsService.validationRolePermission(request, thePermission.getUrl(), thePermission.getMethod());
        logger.debug("Resultado de validación de permisos: {}", isValid);
        return isValid;
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
    @PostMapping("/verify-credentials")
    public ResponseEntity<Map<String, Object>> verifyCredentials(@RequestBody User credentials) {
        logger.info("Verificando credenciales para email: {}", credentials.getEmail());

        try {
            // Buscar usuario por email
            User theActualUser = this.theUserRepository.getUserByEmail(credentials.getEmail());

            // Validar que el usuario existe y la contraseña coincide
            if (theActualUser != null &&
                    theActualUser.getPassword().equals(theEncryptionService.convertSHA256(credentials.getPassword()))) {

                logger.info("Credenciales verificadas exitosamente para: {}", credentials.getEmail());
                // Limpiar la contraseña por seguridad
                theActualUser.setPassword("");

                // Retornar usuario válido
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "user", theActualUser
                ));
            } else {
                logger.warn("Credenciales inválidas para email: {}", credentials.getEmail());
                // Credenciales inválidas
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                                "valid", false,
                                "error", "Invalid email or password"
                        ));
            }
        } catch (Exception e) {
            logger.error("Error al verificar credenciales para email {}: {}", credentials.getEmail(), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "valid", false,
                            "error", "Error verifying credentials: " + e.getMessage()
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
        logger.info("Generando token para usuario ID: {}", user.get_id());

        try {
            // Buscar usuario completo por ID
            User theActualUser = this.theUserRepository.findById(user.get_id()).orElse(null);

            if (theActualUser == null) {
                logger.warn("Usuario no encontrado con ID: {}", user.get_id());
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "User not found"
                        ));
            }

            logger.debug("Usuario encontrado, generando token JWT");
            // Generar token JWT
            String token = this.theJwtService.generateToken(theActualUser);

            // Calcular fecha de expiración
            Date expirationDate = new Date(System.currentTimeMillis() + jwtExpiration);

            // Limpiar contraseña por seguridad
            theActualUser.setPassword("");

            logger.info("Token generado exitosamente para usuario: {}", theActualUser.getEmail());
            // Retornar respuesta con token, expiración y usuario
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "expiration", expirationDate,
                    "user", theActualUser
            ));
        } catch (Exception e) {
            logger.error("Error al generar token para usuario ID {}: {}", user.get_id(), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error generating token: " + e.getMessage()
                    ));
        }
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
    public HashMap<String, Object> login(@RequestBody User theNewUser,
                                        final HttpServletResponse response) throws IOException {
        logger.info("Intento de login para email: {}", theNewUser.getEmail());

        HashMap<String, Object> theResponse = new HashMap<>();
        try {
            User theActualUser = this.theUserRepository.getUserByEmail(theNewUser.getEmail());

            if (theActualUser != null &&
                    theActualUser.getPassword().equals(theEncryptionService.convertSHA256(theNewUser.getPassword()))) {

                logger.info("Login exitoso para: {}", theNewUser.getEmail());
                logger.debug("Generando token JWT para el usuario");

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

                logger.info("Token generado y login completado para: {}", theNewUser.getEmail());
                return theResponse;
            } else {
                logger.warn("Login fallido - Credenciales inválidas para: {}", theNewUser.getEmail());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return theResponse;
            }
        } catch (Exception e) {
            logger.error("Error durante login para email {}: {}", theNewUser.getEmail(), e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return theResponse;
        }
    }

    /**
     * GET /api/public/security/user/{email}
     * Verifica si un usuario existe por su email.
     * Retorna información sobre la existencia del usuario para facilitar el flujo de registro.
     *
     * @param email Email del usuario a verificar
     * @return ResponseEntity con JSON indicando si el usuario existe y sus datos (sin password)
     *
     * Respuestas:
     * - 200 OK con { "exists": true, "user": {...} } si el usuario existe
     * - 200 OK con { "exists": false } si el usuario no existe
     * - 500 Internal Server Error en caso de error
     */
    @GetMapping("/user/{email}")
    public ResponseEntity<Map<String, Object>> getOrCreateUserByEmail(@PathVariable String email) {
        logger.info("Verificando existencia de usuario con email: {}", email);

        try {
            boolean userExists = userService.emailExists(email);

            if (!userExists) {
                logger.info("Usuario no existe con email: {}", email);
                return ResponseEntity.ok(Map.of("exists", false));
            } else {
                User theUser = userService.findByEmail(email);

                if (theUser != null) {
                    // Limpiar la contraseña por seguridad
                    theUser.setPassword("");
                    logger.info("Usuario existe con email: {}", email);
                    return ResponseEntity.ok(Map.of(
                            "exists", true,
                            "user", theUser
                    ));
                } else {
                    // Caso edge: emailExists retornó true pero findByEmail retornó null
                    logger.warn("Inconsistencia: emailExists retornó true pero usuario no encontrado para: {}", email);
                    return ResponseEntity.ok(Map.of("exists", false));
                }
            }
        } catch (Exception e) {
            logger.error("Error al verificar existencia de usuario con email {}: {}", email, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "exists", false,
                            "error", "Error al verificar usuario: " + e.getMessage()
                    ));
        }
    }

    /**
     * POST /api/public/security/register
     * Endpoint público para registrar nuevos usuarios.
     * Usa UserService para validaciones, encriptación y creación.
     *
     * Request body: { "name": "Usuario", "email": "usuario@example.com", "password": "123456" }
     *
     * @param newUser Usuario con name, email y password
     * @return ResponseEntity con el usuario creado (sin password) o error
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User newUser) {
        logger.info("Intentando registrar nuevo usuario con email: {}", newUser.getEmail());
        logger.debug("Datos del usuario: nombre={}", newUser.getName());

        try {
            User createdUser = userService.create(newUser);
            // Limpiar la contraseña por seguridad
            createdUser.setPassword("");
            logger.info("Usuario registrado exitosamente: {}", createdUser.getEmail());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(Map.of(
                            "user", createdUser
                    ));
        } catch (RuntimeException e) {
            logger.error("Error al registrar usuario {}: {}", newUser.getEmail(), e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
