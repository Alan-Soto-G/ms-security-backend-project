package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Entities.Permission;
import fbc.ms_security.Repositories.PermissionRepository;
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

    public PermissionsController(PermissionRepository thePermissionRepository) {
        this.thePermissionRepository = thePermissionRepository;
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
}
