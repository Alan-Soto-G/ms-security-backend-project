/*
 * Controlador para gestionar los roles del sistema.
 * Permite crear, leer, actualizar, eliminar y listar roles.
 *
 * Anotaciones principales:
 * @RestController: Define la clase como controlador REST.
 * @RequestMapping: URL base para los endpoints de roles.
 * @Autowired: Inyección automática de los repositorios necesarios.
 * @CrossOrigin: Permite peticiones desde otros dominios.
 */
package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Role;
import fbc.ms_security.Repositories.SessionRepository;
import fbc.ms_security.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController // Trabajar con API REST
@RequestMapping("/api/roles") // URL de la api donde se activa este controlador
public class RolesController {
    @Autowired // Inyección automática del repositorio de roles
    private RoleRepository theRoleRepository;

    @Autowired // Inyección automática del repositorio de sesiones
    private SessionRepository theSessionRepository;

    /**
     * Obtiene todos los roles registrados.
     * GET /api/roles
     */
    @GetMapping("")
    public List<Role> find() {
        return this.theRoleRepository.findAll();
    }

    /**
     * Busca un rol por su ID.
     * GET /api/roles/{id}
     */
    @GetMapping("{id}")
    public Role findById(@PathVariable String id) {
        Role theRole = this.theRoleRepository.findById(id).orElse(null);
        return theRole;
    }

    /**
     * Crea un nuevo rol.
     * POST /api/roles
     */
    @PostMapping
    public Role create(@RequestBody Role newRole) {
        return this.theRoleRepository.save(newRole);
    }

    /**
     * Actualiza un rol existente por su ID.
     * PUT /api/roles/{id}
     */
    @PutMapping("{id}")
    public Role update(@PathVariable String id, @RequestBody Role newRole) {
        Role actualRole = this.theRoleRepository.findById(id).orElse(null);
        if (actualRole != null) {
            // Actualiza los campos del rol
            actualRole.setName(newRole.getName());
            actualRole.setDescription(newRole.getDescription());
            this.theRoleRepository.save(actualRole);
            return actualRole;
        } else {
            return null;
        }
    }

    /**
     * Elimina un rol por su ID.
     * DELETE /api/roles/{id}
     */
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        Role theRole = this.theRoleRepository.findById(id).orElse(null);
        if (theRole != null) {
            this.theRoleRepository.delete(theRole);
        }
    }
}
