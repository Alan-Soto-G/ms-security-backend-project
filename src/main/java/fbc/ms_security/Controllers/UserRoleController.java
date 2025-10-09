package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Entities.Role;
import fbc.ms_security.Models.Entities.User;
import fbc.ms_security.Models.Relations.UserRole;
import fbc.ms_security.Repositories.RoleRepository;
import fbc.ms_security.Repositories.UserRepository;
import fbc.ms_security.Repositories.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user-role")
public class UserRoleController {
    private static final Logger logger = LoggerFactory.getLogger(UserRoleController.class);
    private final UserRoleRepository theUserRoleRepository;
    private final UserRepository theUserRepository;
    private final RoleRepository theRoleRepository;

    public UserRoleController(UserRoleRepository theUserRoleRepository,
                            UserRepository theUserRepository,
                            RoleRepository theRoleRepository) {
        this.theUserRoleRepository = theUserRoleRepository;
        this.theUserRepository = theUserRepository;
        this.theRoleRepository = theRoleRepository;
        logger.info("UserRoleController inicializado correctamente");
    }

    /**
     * Obtiene todas las relaciones usuario-rol.
     * GET /api/user-role
     */
    @GetMapping("")
    public ResponseEntity<List<UserRole>> find() {
        logger.info("Solicitando lista de todas las relaciones usuario-rol");
        try {
            List<UserRole> userRoles = this.theUserRoleRepository.findAll();
            logger.info("Se encontraron {} relaciones usuario-rol", userRoles.size());
            return ResponseEntity.ok(userRoles);
        } catch (Exception e) {
            logger.error("Error al obtener lista de relaciones usuario-rol: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca una relación usuario-rol por su ID.
     * GET /api/user-role/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserRole> findById(@PathVariable String id) {
        logger.info("Buscando relación usuario-rol con ID: {}", id);
        try {
            UserRole theUserRole = this.theUserRoleRepository.findById(id).orElse(null);
            if (theUserRole == null) {
                logger.warn("Relación usuario-rol no encontrada con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Relación usuario-rol encontrada con ID: {}", id);
            return ResponseEntity.ok(theUserRole);
        } catch (Exception e) {
            logger.error("Error al buscar relación usuario-rol con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todos los roles asignados a un usuario.
     * GET /api/user-role/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserRole>> getRolesByUser(@PathVariable String userId) {
        logger.info("Obteniendo roles para usuario ID: {}", userId);
        try {
            List<UserRole> userRoles = this.theUserRoleRepository.getRolesByUser(userId);
            logger.info("Se encontraron {} roles para el usuario ID: {}", userRoles.size(), userId);
            return ResponseEntity.ok(userRoles);
        } catch (Exception e) {
            logger.error("Error al obtener roles para usuario ID {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todos los usuarios que tienen un rol específico.
     * GET /api/user-role/role/{roleId}
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<UserRole>> getUserByRole(@PathVariable String roleId) {
        logger.info("Obteniendo usuarios con rol ID: {}", roleId);
        try {
            List<UserRole> userRoles = this.theUserRoleRepository.getUsersByRole(roleId);
            logger.info("Se encontraron {} usuarios con el rol ID: {}", userRoles.size(), roleId);
            return ResponseEntity.ok(userRoles);
        } catch (Exception e) {
            logger.error("Error al obtener usuarios con rol ID {}: {}", roleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Asigna un rol a un usuario.
     * POST /api/user-role/user/{userId}/role/{roleId}
     */
    @PostMapping("/user/{userId}/role/{roleId}")
    public ResponseEntity<Object> create(@PathVariable String userId, @PathVariable String roleId) {
        logger.info("Intentando asignar rol ID: {} a usuario ID: {}", roleId, userId);

        try {
            User theUser = this.theUserRepository.findById(userId).orElse(null);
            Role theRole = this.theRoleRepository.findById(roleId).orElse(null);

            if (theUser == null) {
                logger.warn("Usuario no encontrado con ID: {}", userId);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Usuario no encontrado",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }

            if (theRole == null) {
                logger.warn("Rol no encontrado con ID: {}", roleId);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Rol no encontrado",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }

            logger.debug("Usuario y rol encontrados, creando relación");
            UserRole newUserRole = new UserRole();
            newUserRole.setUser(theUser);
            newUserRole.setRole(theRole);
            UserRole savedUserRole = this.theUserRoleRepository.save(newUserRole);
            logger.info("Rol asignado exitosamente. Relación ID: {}", savedUserRole.get_id());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUserRole);
        } catch (Exception e) {
            logger.error("Error al asignar rol ID {} a usuario ID {}: {}", roleId, userId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al asignar rol: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }

    /**
     * Elimina una relación usuario-rol por su ID.
     * DELETE /api/user-role/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id) {
        logger.info("Intentando eliminar relación usuario-rol con ID: {}", id);

        try {
            if (this.theUserRoleRepository.existsById(id)) {
                this.theUserRoleRepository.deleteById(id);
                logger.info("Relación usuario-rol eliminada exitosamente con ID: {}", id);
                return ResponseEntity.ok(Map.of("message", "Relación usuario-rol eliminada correctamente"));
            } else {
                logger.warn("Relación usuario-rol no encontrada para eliminar con ID: {}", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Relación usuario-rol no encontrada",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }
        } catch (Exception e) {
            logger.error("Error al eliminar relación usuario-rol con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al eliminar relación: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }

    /**
     * Elimina una relación usuario-rol específica.
     * DELETE /api/user-role/user/{userId}/role/{roleId}
     */
    @DeleteMapping("/user/{userId}/role/{roleId}")
    public ResponseEntity<Map<String, String>> deleteMatch(@PathVariable String userId, @PathVariable String roleId) {
        logger.info("Intentando eliminar relación usuario ID: {} - rol ID: {}", userId, roleId);

        try {
            Optional<UserRole> theUserRole = this.theUserRoleRepository.findByUserIdAndRoleId(userId, roleId);

            if (theUserRole.isPresent()) {
                String idUserRole = theUserRole.get().get_id();
                logger.debug("Relación encontrada con ID: {}, procediendo a eliminar", idUserRole);
                this.theUserRoleRepository.deleteById(idUserRole);
                logger.info("Relación usuario-rol eliminada exitosamente");
                return ResponseEntity.ok(Map.of("message", "Relación usuario-rol eliminada correctamente"));
            } else {
                logger.warn("Relación usuario-rol no encontrada para usuario ID: {} y rol ID: {}", userId, roleId);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Relación usuario-rol no encontrada",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }
        } catch (Exception e) {
            logger.error("Error al eliminar relación usuario ID {} - rol ID {}: {}", userId, roleId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al eliminar relación: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }
}