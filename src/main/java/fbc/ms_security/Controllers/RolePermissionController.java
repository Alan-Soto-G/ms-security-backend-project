package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Entities.Permission;
import fbc.ms_security.Models.Entities.Role;
import fbc.ms_security.Models.Relations.RolePermission;
import fbc.ms_security.Repositories.RoleRepository;
import fbc.ms_security.Repositories.RolePermissionRepository;
import fbc.ms_security.Repositories.PermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role-permission")
public class RolePermissionController {
    private static final Logger logger = LoggerFactory.getLogger(RolePermissionController.class);
    private final RolePermissionRepository theRolePermissionRepository;
    private final PermissionRepository thePermissionRepository;
    private final RoleRepository theRoleRepository;

    public RolePermissionController(RolePermissionRepository theRolePermissionRepository,
                                   PermissionRepository thePermissionRepository,
                                   RoleRepository theRoleRepository) {
        this.theRolePermissionRepository = theRolePermissionRepository;
        this.thePermissionRepository = thePermissionRepository;
        this.theRoleRepository = theRoleRepository;
        logger.info("RolePermissionController inicializado correctamente");
    }

    /**
     * Obtiene todas las relaciones rol-permiso.
     * GET /api/role-permission
     */
    @GetMapping("")
    public ResponseEntity<List<RolePermission>> findAll() {
        logger.info("Solicitando lista de todas las relaciones rol-permiso");
        try {
            List<RolePermission> rolePermissions = theRolePermissionRepository.findAll();
            if (rolePermissions.isEmpty()) {
                logger.warn("No se encontraron relaciones rol-permiso en el sistema");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Se encontraron {} relaciones rol-permiso", rolePermissions.size());
            return ResponseEntity.ok(rolePermissions);
        } catch (Exception e) {
            logger.error("Error al obtener lista de relaciones rol-permiso: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca una relación rol-permiso por su ID.
     * GET /api/role-permission/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RolePermission> findById(@PathVariable String id) {
        logger.info("Buscando relación rol-permiso con ID: {}", id);
        try {
            RolePermission theRolePermission = this.theRolePermissionRepository.findById(id).orElse(null);
            if (theRolePermission == null) {
                logger.warn("Relación rol-permiso no encontrada con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Relación rol-permiso encontrada con ID: {}", id);
            return ResponseEntity.ok(theRolePermission);
        } catch (Exception e) {
            logger.error("Error al buscar relación rol-permiso con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todas las relaciones por ID de permiso.
     * GET /api/role-permission/permission/{permissionId}
     */
    @GetMapping("/permission/{permissionId}")
    public ResponseEntity<List<RolePermission>> getRolesByPermission(@PathVariable String permissionId) {
        logger.info("Obteniendo roles para permiso ID: {}", permissionId);
        try {
            List<RolePermission> rolePermissions = this.theRolePermissionRepository.getRolesByPermission(permissionId);
            if (rolePermissions.isEmpty()) {
                logger.warn("No se encontraron roles para el permiso ID: {}", permissionId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Se encontraron {} roles para el permiso ID: {}", rolePermissions.size(), permissionId);
            return ResponseEntity.ok(rolePermissions);
        } catch (Exception e) {
            logger.error("Error al obtener roles para permiso ID {}: {}", permissionId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todas las relaciones por ID de rol.
     * GET /api/role-permission/role/{roleId}
     */
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<RolePermission>> getPermissionByRole(@PathVariable String roleId) {
        logger.info("Obteniendo permisos para rol ID: {}", roleId);
        try {
            List<RolePermission> rolePermissions = this.theRolePermissionRepository.getPermissionByRole(roleId);
            if (rolePermissions.isEmpty()) {
                logger.warn("No se encontraron permisos para el rol ID: {}", roleId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Se encontraron {} permisos para el rol ID: {}", rolePermissions.size(), roleId);
            return ResponseEntity.ok(rolePermissions);
        } catch (Exception e) {
            logger.error("Error al obtener permisos para rol ID {}: {}", roleId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Asigna un permiso a un rol.
     * POST /api/role-permission/role/{roleId}/permission/{permissionId}
     */
    @PostMapping("/role/{roleId}/permission/{permissionId}")
    public ResponseEntity<Object> create(@PathVariable String permissionId, @PathVariable String roleId) {
        logger.info("Intentando asignar permiso ID: {} a rol ID: {}", permissionId, roleId);

        try {
            Permission thePermission = this.thePermissionRepository.findById(permissionId).orElse(null);
            Role theRole = this.theRoleRepository.findById(roleId).orElse(null);

            if (thePermission == null) {
                logger.warn("Permiso no encontrado con ID: {}", permissionId);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Permiso no encontrado",
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

            logger.debug("Permiso y rol encontrados, creando relación");
            RolePermission newRolePermission = new RolePermission();
            newRolePermission.setPermission(thePermission);
            newRolePermission.setRole(theRole);
            RolePermission savedRolePermission = this.theRolePermissionRepository.save(newRolePermission);
            logger.info("Permiso asignado exitosamente. Relación ID: {}", savedRolePermission.get_id());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedRolePermission);
        } catch (Exception e) {
            logger.error("Error al asignar permiso ID {} a rol ID {}: {}", permissionId, roleId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al asignar permiso: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }

    /**
     * Elimina una relación rol-permiso por su ID.
     * DELETE /api/role-permission/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id) {
        logger.info("Intentando eliminar relación rol-permiso con ID: {}", id);

        try {
            RolePermission theRolePermission = this.theRolePermissionRepository.findById(id).orElse(null);
            if (theRolePermission != null) {
                logger.debug("Relación encontrada, procediendo a eliminar");
                this.theRolePermissionRepository.delete(theRolePermission);
                logger.info("Relación rol-permiso eliminada exitosamente con ID: {}", id);
                return ResponseEntity.ok(Map.of("message", "Relación rol-permiso eliminada correctamente"));
            } else {
                logger.warn("Relación rol-permiso no encontrada para eliminar con ID: {}", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Relación rol-permiso no encontrada",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }
        } catch (Exception e) {
            logger.error("Error al eliminar relación rol-permiso con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al eliminar relación: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }

    /**
     * Elimina una relación rol-permiso específica.
     * DELETE /api/role-permission/role/{roleId}/permission/{permissionId}
     */
    @DeleteMapping("/role/{roleId}/permission/{permissionId}")
    public ResponseEntity<Map<String, String>> deleteMatch(@PathVariable String roleId, @PathVariable String permissionId) {
        logger.info("Intentando eliminar relación rol ID: {} - permiso ID: {}", roleId, permissionId);

        try {
            RolePermission theRolePermission = this.theRolePermissionRepository.getRolePermission(roleId, permissionId);
            if (theRolePermission == null) {
                logger.warn("Relación rol-permiso no encontrada para rol ID: {} y permiso ID: {}", roleId, permissionId);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Relación rol-permiso no encontrada",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }

            String rolePermissionId = theRolePermission.get_id();
            logger.debug("Relación encontrada con ID: {}, procediendo a eliminar", rolePermissionId);
            this.theRolePermissionRepository.deleteById(rolePermissionId);
            logger.info("Relación rol-permiso eliminada exitosamente");
            return ResponseEntity.ok(Map.of("message", "Relación rol-permiso eliminada correctamente"));
        } catch (Exception e) {
            logger.error("Error al eliminar relación rol ID {} - permiso ID {}: {}", roleId, permissionId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al eliminar relación: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }
}