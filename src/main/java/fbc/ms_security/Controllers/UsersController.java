/*
 * Controlador para gestionar los usuarios del sistema.
 * Permite crear, leer, actualizar, eliminar y listar usuarios.
 *
 * Anotaciones principales:
 * @RestController: Define la clase como controlador REST.
 * @RequestMapping: URL base para los endpoints de usuarios.
 * @Autowired: Inyección automática de los repositorios necesarios.
 * @CrossOrigin: Permite peticiones desde otros dominios.
 */
package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Session;
import fbc.ms_security.Models.User;
import fbc.ms_security.Repositories.SessionRepository;
import fbc.ms_security.Repositories.UserRepository;
import fbc.ms_security.Services.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@CrossOrigin
@RestController // Trabajar con API REST
@RequestMapping("/api/users") // URL de la api donde se activa este controlador
public class UsersController {
    @Autowired // Inyección automática del repositorio de usuarios
    private UserRepository theUserRepository;

    @Autowired // Inyección automática del repositorio de sesiones
    private SessionRepository theSessionRepository;

    @Autowired
    private EncryptionService theEncryptionService;

    /**
     * Obtiene todos los usuarios registrados.
     * GET /api/users
     */
    @GetMapping("")
    public List<User> find() {
        return this.theUserRepository.findAll();
    }

    /**
     * Busca un usuario por su ID.
     * GET /api/users/{id}
     */
    @GetMapping("{id}")
    public User findById(@PathVariable String id) {
        User theUser = this.theUserRepository.findById(id).orElse(null);
        return theUser;
    }

    /**
     * Crea un nuevo usuario.
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity create(@RequestBody User newUser) {
        User checkUser = this.theUserRepository.getUserByEmail(newUser.getEmail());
        if (checkUser != null) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error", "Email already exists",
                            "code", HttpStatus.CONFLICT.value()
                    ));
        }else {
            newUser.setPassword(this.theEncryptionService.convertSHA256(newUser.getPassword()));
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(this.theUserRepository.save(newUser));
        }
    }

    /**
     * Actualiza un usuario existente por su ID.
     * PUT /api/users/{id}
     */
    @PutMapping("{id}")
    public User update(@PathVariable String id, @RequestBody User newUser) {
        User actualUser = this.theUserRepository.findById(id).orElse(null);
        if (actualUser != null) {
            // Actualiza los campos del usuario
            actualUser.setName(newUser.getName());
            actualUser.setEmail(newUser.getEmail());
            actualUser.setPassword(this.theEncryptionService.convertSHA256(newUser.getPassword()));
            this.theUserRepository.save(actualUser);
            return actualUser;
        } else {
            return null;
        }
    }

    /**
     * Elimina un usuario por su ID.
     * DELETE /api/users/{id}
     */
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        User theUser = this.theUserRepository.findById(id).orElse(null);
        if (theUser != null) {
            this.theUserRepository.delete(theUser);
        }
    }

    /**
     * Asocia una sesión a un usuario.
     * PUT /api/users/{idUser}/session/{sessionId}
     */
    @PutMapping("{idUser}/session/{sessionId}")
    public void matchSession(@PathVariable String idUser, @PathVariable String sessionId) {
        Session theSession = this.theSessionRepository.findById(sessionId).orElse(null);
        User  theUser = this.theUserRepository.findById(idUser).orElse(null);
        if (theSession != null &&  theUser != null) {
            theSession.setUser(theUser);
            this.theSessionRepository.save(theSession);
        }
    }
}