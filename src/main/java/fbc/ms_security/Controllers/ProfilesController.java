package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Entities.Profile;
import fbc.ms_security.Repositories.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
public class ProfilesController {
    private static final Logger logger = LoggerFactory.getLogger(ProfilesController.class);
    private final ProfileRepository theProfileRepository;

    public ProfilesController(ProfileRepository theProfileRepository) {
        this.theProfileRepository = theProfileRepository;
        logger.info("ProfilesController inicializado correctamente");
    }

    /**
     * Obtiene todos los perfiles registrados.
     * GET /api/profiles
     */
    @GetMapping("")
    public ResponseEntity<List<Profile>> findAll() {
        logger.info("Solicitando lista de todos los perfiles");
        try {
            List<Profile> profiles = theProfileRepository.findAll();
            if (profiles.isEmpty()) {
                logger.warn("No se encontraron perfiles en el sistema");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Se encontraron {} perfiles", profiles.size());
            return ResponseEntity.ok(profiles);
        } catch (Exception e) {
            logger.error("Error al obtener lista de perfiles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca un perfil por su ID.
     * GET /api/profiles/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Profile> findById(@PathVariable String id) {
        logger.info("Buscando perfil con ID: {}", id);
        try {
            Profile theProfile = this.theProfileRepository.findById(id).orElse(null);
            if (theProfile == null) {
                logger.warn("Perfil no encontrado con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Perfil encontrado con ID: {}", id);
            logger.debug("Teléfono: {}", theProfile.getPhone());
            return ResponseEntity.ok(theProfile);
        } catch (Exception e) {
            logger.error("Error al buscar perfil con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crea un nuevo perfil.
     * POST /api/profiles
     */
    @PostMapping("")
    public ResponseEntity<Object> create(@RequestBody Profile newProfile) {
        logger.info("Intentando crear nuevo perfil");
        logger.debug("Datos del perfil - Teléfono: {}", newProfile.getPhone());

        try {
            if (newProfile == null) {
                logger.warn("Intento de crear perfil con datos nulos");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "error", "Los datos del perfil son requeridos",
                                "status", String.valueOf(HttpStatus.BAD_REQUEST.value())
                        ));
            }

            Profile createdProfile = this.theProfileRepository.save(newProfile);
            logger.info("Perfil creado exitosamente con ID: {}", createdProfile.get_id());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdProfile);
        } catch (Exception e) {
            logger.error("Error al crear perfil: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al crear perfil: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }

    /**
     * Actualiza un perfil existente por su ID.
     * PUT /api/profiles/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable String id, @RequestBody Profile newProfile) {
        logger.info("Intentando actualizar perfil con ID: {}", id);
        logger.debug("Nuevos datos - Teléfono: {}", newProfile.getPhone());

        try {
            Profile actualProfile = this.theProfileRepository.findById(id).orElse(null);
            if (actualProfile != null) {
                logger.debug("Perfil encontrado, actualizando datos");
                actualProfile.setPhone(newProfile.getPhone());
                actualProfile.setPhoto(newProfile.getPhoto());
                Profile updatedProfile = this.theProfileRepository.save(actualProfile);
                logger.info("Perfil actualizado exitosamente con ID: {}", id);
                return ResponseEntity.ok(updatedProfile);
            } else {
                logger.warn("Perfil no encontrado para actualizar con ID: {}", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Perfil no encontrado",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }
        } catch (Exception e) {
            logger.error("Error al actualizar perfil con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al actualizar perfil: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }

    /**
     * Elimina un perfil por su ID.
     * DELETE /api/profiles/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id) {
        logger.info("Intentando eliminar perfil con ID: {}", id);

        try {
            Profile theProfile = this.theProfileRepository.findById(id).orElse(null);
            if (theProfile != null) {
                logger.debug("Perfil encontrado, procediendo a eliminar");
                this.theProfileRepository.delete(theProfile);
                logger.info("Perfil eliminado exitosamente con ID: {}", id);
                return ResponseEntity.ok(Map.of("message", "Perfil eliminado correctamente"));
            } else {
                logger.warn("Perfil no encontrado para eliminar con ID: {}", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Perfil no encontrado",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }
        } catch (Exception e) {
            logger.error("Error al eliminar perfil con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al eliminar perfil: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }
}