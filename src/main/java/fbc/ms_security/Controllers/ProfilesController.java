/*
 * Controlador para gestionar los perfiles de usuario.
 * Permite crear, leer, actualizar, eliminar y listar perfiles.
 *
 * Anotaciones principales:
 * @RestController: Define la clase como controlador REST.
 * @RequestMapping: URL base para los endpoints de perfiles.
 * @Autowired: Inyección automática de los repositorios necesarios.
 * @CrossOrigin: Permite peticiones desde otros dominios.
 */
package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Profile;
import fbc.ms_security.Repositories.SessionRepository;
import fbc.ms_security.Repositories.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController // Trabajar con API REST
@RequestMapping("/api/profiles") // URL de la api donde se activa este controlador
public class ProfilesController {
    @Autowired // Inyección automática del repositorio de perfiles
    private ProfileRepository theProfileRepository;

    @Autowired // Inyección automática del repositorio de sesiones
    private SessionRepository theSessionRepository;

    /**
     * Obtiene todos los perfiles registrados.
     * GET /api/profiles
     */
    @GetMapping("")
    public List<Profile> find() {
        return this.theProfileRepository.findAll();
    }

    /**
     * Busca un perfil por su ID.
     * GET /api/profiles/{id}
     */
    @GetMapping("{id}")
    public Profile findById(@PathVariable String id) {
        Profile theProfile = this.theProfileRepository.findById(id).orElse(null);
        return theProfile;
    }

    /**
     * Crea un nuevo perfil.
     * POST /api/profiles
     */
    @PostMapping
    public Profile create(@RequestBody Profile newProfile) {
        return this.theProfileRepository.save(newProfile);
    }

    /**
     * Actualiza un perfil existente por su ID.
     * PUT /api/profiles/{id}
     */
    @PutMapping("{id}")
    public Profile update(@PathVariable String id, @RequestBody Profile newProfile) {
        Profile actualProfile = this.theProfileRepository.findById(id).orElse(null);
        if (actualProfile != null) {
            // Actualiza los campos del perfil
            actualProfile.setPhone(newProfile.getPhone());
            actualProfile.setPhoto(newProfile.getPhoto());
            this.theProfileRepository.save(actualProfile);
            return actualProfile;
        } else {
            return null;
        }
    }

    /**
     * Elimina un perfil por su ID.
     * DELETE /api/profiles/{id}
     */
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        Profile theProfile = this.theProfileRepository.findById(id).orElse(null);
        if (theProfile != null) {
            this.theProfileRepository.delete(theProfile);
        }
    }
}