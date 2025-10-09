package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Entities.Role;
import fbc.ms_security.Repositories.SessionRepository;
import fbc.ms_security.Repositories.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RolesController {
    private static final Logger logger = LoggerFactory.getLogger(RolesController.class);
    private final RoleRepository theRoleRepository;
    private final SessionRepository theSessionRepository;

    public RolesController(RoleRepository theRoleRepository, SessionRepository theSessionRepository) {
        this.theRoleRepository = theRoleRepository;
        this.theSessionRepository = theSessionRepository;
        logger.info("RolesController inicializado correctamente");
    }

    /**
     * Obtiene todos los roles registrados.
     * GET /api/roles
     */
    @GetMapping("")
    public ResponseEntity<List<Role>> find() {
        logger.info("Solicitando lista de todos los roles");
        try {
            List<Role> roles = this.theRoleRepository.findAll();
            logger.info("Se encontraron {} roles", roles.size());
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            logger.error("Error al obtener lista de roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca un rol por su ID.
     * GET /api/roles/{id}
     */
    @GetMapping("{id}")
    public ResponseEntity<Role> findById(@PathVariable String id) {
        logger.info("Buscando rol con ID: {}", id);
        try {
            Role theRole = this.theRoleRepository.findById(id).orElse(null);
            if (theRole == null) {
                logger.warn("Rol no encontrado con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Rol encontrado: {}", theRole.getName());
            return ResponseEntity.ok(theRole);
        } catch (Exception e) {
            logger.error("Error al buscar rol con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crea un nuevo rol.
     * POST /api/roles
     */
    @PostMapping
    public ResponseEntity<Object> create(@RequestBody Role newRole) {
        logger.info("Intentando crear nuevo rol: {}", newRole.getName());
        logger.debug("Datos del rol: {}", newRole);

        try {
            String normalizedName = newRole.getName().toLowerCase().trim();
            Role checkRole = this.theRoleRepository.getRoleByName(normalizedName);

            if (checkRole != null) {
                logger.warn("Intento de crear rol duplicado: {}", normalizedName);
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "message", "Role already exists",
                                "status", String.valueOf(HttpStatus.CONFLICT.value())
                        ));
            } else {
                newRole.setName(normalizedName);
                Role savedRole = this.theRoleRepository.save(newRole);
                logger.info("Rol creado exitosamente con ID: {}", savedRole.get_id());
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(savedRole);
            }
        } catch (Exception e) {
            logger.error("Error al crear rol {}: {}", newRole.getName(), e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error creating role: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }

    /**
     * Actualiza un rol existente por su ID.
     * PUT /api/roles/{id}
     */
    @PutMapping("{id}")
    public ResponseEntity<Role> update(@PathVariable String id, @RequestBody Role newRole) {
        logger.info("Intentando actualizar rol con ID: {}", id);
        logger.debug("Nuevos datos del rol: {}", newRole);

        try {
            Role actualRole = this.theRoleRepository.findById(id).orElse(null);
            if (actualRole != null) {
                logger.debug("Rol encontrado, actualizando datos");
                actualRole.setName(newRole.getName());
                actualRole.setDescription(newRole.getDescription());
                Role updatedRole = this.theRoleRepository.save(actualRole);
                logger.info("Rol actualizado exitosamente: {}", updatedRole.getName());
                return ResponseEntity.ok(updatedRole);
            } else {
                logger.warn("Rol no encontrado para actualizar con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            logger.error("Error al actualizar rol con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Elimina un rol por su ID.
     * DELETE /api/roles/{id}
     */
    @DeleteMapping("{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id) {
        logger.info("Intentando eliminar rol con ID: {}", id);

        try {
            Role theRole = this.theRoleRepository.findById(id).orElse(null);
            if (theRole != null) {
                logger.debug("Rol encontrado: {}, procediendo a eliminar", theRole.getName());
                this.theRoleRepository.delete(theRole);
                logger.info("Rol eliminado exitosamente con ID: {}", id);
                return ResponseEntity.ok(Map.of("message", "Rol eliminado correctamente"));
            } else {
                logger.warn("Rol no encontrado para eliminar con ID: {}", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Rol no encontrado",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }
        } catch (Exception e) {
            logger.error("Error al eliminar rol con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al eliminar rol: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }
}
