package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Entities.User;
import fbc.ms_security.Services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UsersController {
    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);
    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
        logger.info("UsersController inicializado correctamente");
    }

    /**
     * Obtiene todos los usuarios registrados.
     * GET /api/users
     */
    @GetMapping("")
    public ResponseEntity<List<User>> find() {
        logger.info("Solicitando lista de todos los usuarios");
        try {
            List<User> users = userService.findAll();
            logger.info("Se encontraron {} usuarios", users.size());
            users.forEach(user -> user.setPassword("********")); // Ocultar las contraseñas en la respuesta
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error al obtener lista de usuarios: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca un usuario por su ID.
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable String id) {
        logger.info("Buscando usuario con ID: {}", id);
        try {
            User theUser = userService.findById(id);
            if (theUser == null) {
                logger.warn("Usuario no encontrado con ID: {}", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .build();
            }
            logger.info("Usuario encontrado: {}", theUser.getEmail());
            theUser.setPassword("********"); // Ocultar la contraseña en la respuesta
            return ResponseEntity.ok(theUser);
        } catch (Exception e) {
            logger.error("Error al buscar usuario con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca un usuario por su EMAIL.
     * GET /api/users/email/{email}
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<User> findByEmail(@PathVariable String email) {
        logger.info("Buscando usuario con email: {}", email);
        try {
            User theUser = userService.findByEmail(email);
            if (theUser == null) {
                logger.warn("Usuario no encontrado con email: {}", email);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .build();
            }
            logger.info("Usuario encontrado con email: {}", email);
            theUser.setPassword("********"); // Ocultar la contraseña en la respuesta
            return ResponseEntity.ok(theUser);
        } catch (Exception e) {
            logger.error("Error al buscar usuario con email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/verify-email/{email}")
    public ResponseEntity<Map<String, Boolean>> verifyEmail(@PathVariable String email) {
        logger.info("Verificando existencia de email: {}", email);
        try {
            boolean exists = userService.emailExists(email);
            logger.info("Email {} - Existe: {}", email, exists);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            logger.error("Error al verificar email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crea un nuevo usuario.
     * POST /api/users
     */
    @PostMapping("")
    public ResponseEntity<Object> create(@RequestBody User newUser) {
        logger.info("Intentando crear nuevo usuario con email: {}", newUser.getEmail());
        logger.debug("Datos del usuario: nombre={}", newUser.getName());

        try {
            User savedUser = userService.create(newUser);
            logger.info("Usuario creado exitosamente con ID: {}", savedUser.get_id());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedUser);
        } catch (ResponseStatusException e) {
            logger.error("Error al crear usuario {}: {}", newUser.getEmail(), e.getReason(), e);
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(Map.of(
                            "error", e.getReason(),
                            "status", String.valueOf(e.getStatusCode().value())
                    ));
        } catch (Exception e) {
            logger.error("Error inesperado al crear usuario {}: {}", newUser.getEmail(), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al crear usuario: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }

    /**
     * Actualiza un usuario existente por su ID.
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable String id, @RequestBody User newUser) {
        logger.info("Intentando actualizar usuario con ID: {}", id);
        logger.debug("Nuevos datos: email={}, nombre={}", newUser.getEmail(), newUser.getName());

        try {
            User updatedUser = userService.update(id, newUser);
            if (updatedUser == null) {
                logger.warn("Usuario no encontrado para actualizar con ID: {}", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Usuario no encontrado",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }
            logger.info("Usuario actualizado exitosamente: {}", updatedUser.getEmail());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            logger.error("Error al actualizar usuario con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al actualizar usuario: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }

    /**
     * Elimina un usuario por su ID.
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id) {
        logger.info("Intentando eliminar usuario con ID: {}", id);

        try {
            boolean deleted = userService.delete(id);

            if (!deleted) {
                logger.warn("Usuario no encontrado para eliminar con ID: {}", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Usuario no encontrado",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }

            logger.info("Usuario eliminado exitosamente con ID: {}", id);
            return ResponseEntity.ok(Map.of("message", "Usuario eliminado correctamente"));
        } catch (Exception e) {
            logger.error("Error al eliminar usuario con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al eliminar usuario: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }

    /**
     * Asocia una sesión a un usuario.
     * PUT /api/users/{idUser}/session/{sessionId}
     */
    @PutMapping("/{idUser}/session/{sessionId}")
    public ResponseEntity<Map<String, String>> matchSession(@PathVariable String idUser, @PathVariable String sessionId) {
        logger.info("Intentando asociar sesión ID: {} al usuario ID: {}", sessionId, idUser);

        try {
            boolean matched = userService.matchSession(idUser, sessionId);
            if (!matched) {
                logger.warn("No se pudo asociar sesión. Usuario ID: {} o Sesión ID: {} no encontrados", idUser, sessionId);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Usuario o sesión no encontrados",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }
            logger.info("Sesión asociada exitosamente al usuario ID: {}", idUser);
            return ResponseEntity.ok(Map.of("message", "Sesión asociada correctamente"));
        } catch (Exception e) {
            logger.error("Error al asociar sesión ID {} al usuario ID {}: {}", sessionId, idUser, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al asociar sesión: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }
}