package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Entities.Session;
import fbc.ms_security.Repositories.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/sessions")
public class SessionsController {
    private static final Logger logger = LoggerFactory.getLogger(SessionsController.class);
    private final SessionRepository theSessionRepository;

    public SessionsController(SessionRepository theSessionRepository) {
        this.theSessionRepository = theSessionRepository;
        logger.info("SessionsController inicializado correctamente");
    }

    /**
     * Obtiene todas las sesiones registradas.
     * GET /api/sessions
     */
    @GetMapping("")
    public ResponseEntity<List<Session>> find() {
        logger.info("Solicitando lista de todas las sesiones");
        try {
            List<Session> sessions = this.theSessionRepository.findAll();
            logger.info("Se encontraron {} sesiones", sessions.size());
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            logger.error("Error al obtener lista de sesiones: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca una sesión por su ID.
     * GET /api/sessions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Session> findById(@PathVariable String id) {
        logger.info("Buscando sesión con ID: {}", id);
        try {
            Session theSession = this.theSessionRepository.findById(id).orElse(null);
            if (theSession == null) {
                logger.warn("Sesión no encontrada con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            logger.info("Sesión encontrada con ID: {}", id);
            logger.debug("Token de sesión: {}", theSession.getToken());
            return ResponseEntity.ok(theSession);
        } catch (Exception e) {
            logger.error("Error al buscar sesión con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crea una nueva sesión.
     * POST /api/sessions
     */
    @PostMapping("")
    public ResponseEntity<Session> create(@RequestBody Session newSession) {
        logger.info("Intentando crear nueva sesión");
        logger.debug("Token: {}, Expiración: {}", newSession.getToken(), newSession.getExpiration());

        try {
            Session savedSession = this.theSessionRepository.save(newSession);
            logger.info("Sesión creada exitosamente con ID: {}", savedSession.get_id());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSession);
        } catch (Exception e) {
            logger.error("Error al crear sesión: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Actualiza una sesión existente por su ID.
     * PUT /api/sessions/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Session> update(@PathVariable String id, @RequestBody Session newSession) {
        logger.info("Intentando actualizar sesión con ID: {}", id);
        logger.debug("Nuevos datos - Token: {}, Expiración: {}", newSession.getToken(), newSession.getExpiration());

        try {
            Session actualSession = this.theSessionRepository.findById(id).orElse(null);
            if (actualSession != null) {
                logger.debug("Sesión encontrada, actualizando datos");
                // Actualiza los campos de la sesión
                actualSession.setExpiration(newSession.getExpiration());
                actualSession.setToken(newSession.getToken());
                Session updatedSession = this.theSessionRepository.save(actualSession);
                logger.info("Sesión actualizada exitosamente con ID: {}", id);
                return ResponseEntity.ok(updatedSession);
            } else {
                logger.warn("Sesión no encontrada para actualizar con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            logger.error("Error al actualizar sesión con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Elimina una sesión por su ID.
     * DELETE /api/sessions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id) {
        logger.info("Intentando eliminar sesión con ID: {}", id);

        try {
            Session theSession = this.theSessionRepository.findById(id).orElse(null);
            if (theSession != null) {
                logger.debug("Sesión encontrada, procediendo a eliminar");
                this.theSessionRepository.delete(theSession);
                logger.info("Sesión eliminada exitosamente con ID: {}", id);
                return ResponseEntity.ok(Map.of("message", "Sesión eliminada correctamente"));
            } else {
                logger.warn("Sesión no encontrada para eliminar con ID: {}", id);
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "Sesión no encontrada",
                                "status", String.valueOf(HttpStatus.NOT_FOUND.value())
                        ));
            }
        } catch (Exception e) {
            logger.error("Error al eliminar sesión con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Error al eliminar sesión: " + e.getMessage(),
                            "status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    ));
        }
    }
}