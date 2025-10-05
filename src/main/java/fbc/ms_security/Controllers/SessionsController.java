/*
 * Controlador para gestionar las sesiones de usuario.
 * Permite crear, leer, actualizar, eliminar y listar sesiones.
 *
 * Anotaciones principales:
 * @RestController: Define la clase como controlador REST.
 * @RequestMapping: URL base para los endpoints de sesiones.
 * @Autowired: Inyección automática del repositorio de sesiones.
 * @CrossOrigin: Permite peticiones desde otros dominios.
 */
package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Session;
import fbc.ms_security.Repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController // Trabajar con API REST
@RequestMapping("/api/sessions") // URL de la api donde se activa este controlador
public class SessionsController {
    @Autowired // Inyección automática del repositorio de sesiones
    private SessionRepository theSessionRepository;

    /**
     * Obtiene todas las sesiones registradas.
     * GET /api/sessions
     */
    @GetMapping("")
    public List<Session> find() {
        return this.theSessionRepository.findAll();
    }

    /**
     * Busca una sesión por su ID.
     * GET /api/sessions/{id}
     */
    @GetMapping("{id}")
    public Session findById(@PathVariable String id) {
        Session theSession = this.theSessionRepository.findById(id).orElse(null);
        return theSession;
    }

    /**
     * Crea una nueva sesión.
     * POST /api/sessions
     */
    @PostMapping
    public Session create(@RequestBody Session newSession) {
        return this.theSessionRepository.save(newSession);
    }

    /**
     * Actualiza una sesión existente por su ID.
     * PUT /api/sessions/{id}
     */
    @PutMapping("{id}")
    public Session update(@PathVariable String id, @RequestBody Session newSession) {
        Session actualSession = this.theSessionRepository.findById(id).orElse(null);
        if (actualSession != null) {
            // Actualiza los campos de la sesión
            actualSession.setExpiration(newSession.getExpiration());
            actualSession.setToken(newSession.getToken());
            this.theSessionRepository.save(actualSession);
            return actualSession;
        } else {
            return null;
        }
    }

    /**
     * Elimina una sesión por su ID.
     * DELETE /api/sessions/{id}
     */
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        Session theSession = this.theSessionRepository.findById(id).orElse(null);
        if (theSession != null) {
            this.theSessionRepository.delete(theSession);
        }
    }
}