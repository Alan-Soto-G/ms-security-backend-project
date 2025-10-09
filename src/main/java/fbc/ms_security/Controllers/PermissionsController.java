package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Entities.Permission;
import fbc.ms_security.Models.Utils.MostUsedPermissionsRequest;
import fbc.ms_security.Repositories.PermissionRepository;
import fbc.ms_security.Services.PermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
public class PermissionsController {
    private static final Logger logger = LoggerFactory.getLogger(PermissionsController.class);
    private final PermissionRepository thePermissionRepository;
    private final PermissionService permissionService;

    public PermissionsController(PermissionRepository thePermissionRepository, PermissionService permissionService) {
        this.thePermissionRepository = thePermissionRepository;
        this.permissionService = permissionService;
    }

    /**
     * Obtiene todos los permisos registrados.
     */
    @GetMapping("")
    public ResponseEntity<List<Permission>> findAll() {
        List<Permission> permissions = this.thePermissionRepository.findAll();
        if (permissions.isEmpty()) {
            logger.warn("No se encontraron permisos registrados.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        logger.info("Se encontraron {} permisos.", permissions.size());
        return ResponseEntity.ok(permissions);
    }

    /**
     * Busca un permiso por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Permission> findById(@PathVariable String id) {
        return this.thePermissionRepository.findById(id)
                .map(permission -> {
                    logger.info("Permiso encontrado con ID: {}", id);
                    return ResponseEntity.ok(permission);
                })
                .orElseGet(() -> {
                    logger.warn("Permiso no encontrado con ID: {}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                });
    }

    /**
     * Crea un nuevo permiso.
     */
    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody Permission newPermission) {
        if (newPermission.getUrl() == null || newPermission.getMethod() == null) {
            logger.error("Datos incompletos para crear un permiso.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Datos incompletos", "status", HttpStatus.BAD_REQUEST.value()));
        }
        Permission existingPermission = this.thePermissionRepository.getPermission(newPermission.getUrl(), newPermission.getMethod());
        if (existingPermission != null) {
            logger.warn("El permiso ya existe: URL={}, Method={}", newPermission.getUrl(), newPermission.getMethod());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Permission already exists", "status", HttpStatus.CONFLICT.value()));
        }
        Permission savedPermission = this.thePermissionRepository.save(newPermission);
        logger.info("Permiso creado con éxito: {}", savedPermission);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPermission);
    }

    /**
     * Actualiza un permiso existente por su ID.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Permission newPermission) {
        return this.thePermissionRepository.findById(id)
                .map(existingPermission -> {
                    if (newPermission.getUrl() == null || newPermission.getMethod() == null) {
                        logger.error("Datos incompletos para actualizar el permiso con ID: {}", id);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Datos incompletos", "status", HttpStatus.BAD_REQUEST.value()));
                    }
                    existingPermission.setUrl(newPermission.getUrl());
                    existingPermission.setMethod(newPermission.getMethod());
                    Permission updatedPermission = this.thePermissionRepository.save(existingPermission);
                    logger.info("Permiso actualizado con éxito: {}", updatedPermission);
                    return ResponseEntity.ok(updatedPermission);
                })
                .orElseGet(() -> {
                    logger.warn("Permiso no encontrado para actualizar con ID: {}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                });
    }

    /**
     * Elimina un permiso por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return this.thePermissionRepository.findById(id)
                .map(permission -> {
                    this.thePermissionRepository.delete(permission);
                    logger.info("Permiso eliminado con éxito: ID={}", id);
                    return ResponseEntity.ok(Map.of("message", "Permiso eliminado con éxito"));
                })
                .orElseGet(() -> {
                    logger.warn("Permiso no encontrado para eliminar con ID: {}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(Map.of("error", "Permiso no encontrado", "status", String.valueOf(HttpStatus.NOT_FOUND.value())));
                });
    }

    /**
     * Obtiene los permisos más usados basándose en los índices proporcionados.
     */
    @PostMapping("/most-used")
    public ResponseEntity<?> getMostUsedPermissions(@RequestBody MostUsedPermissionsRequest request) {
        try {
            // Validar que el request no sea nulo
            if (request == null) {
                logger.error("Request nulo recibido en /most-used");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Request no puede ser nulo", "status", HttpStatus.BAD_REQUEST.value()));
            }

            // Validar que la lista de índices no sea nula
            if (request.getIndices() == null) {
                logger.error("Lista de índices nula recibida en /most-used");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "La lista de índices no puede ser nula", "status", HttpStatus.BAD_REQUEST.value()));
            }

            // Obtener los permisos correspondientes a los índices
            List<Permission> permissions = this.permissionService.getPermissionsByIndices(request.getIndices());

            // Verificar si se encontraron permisos
            if (permissions.isEmpty()) {
                logger.warn("No se encontraron permisos para los índices proporcionados: {}", request.getIndices());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No se encontraron permisos para los índices proporcionados", "status", HttpStatus.NOT_FOUND.value()));
            }

            logger.info("Se encontraron {} permisos más usados para los índices: {}", permissions.size(), request.getIndices());
            return ResponseEntity.ok(permissions);

        } catch (Exception e) {
            logger.error("Error interno al obtener permisos más usados: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor", "status", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}
