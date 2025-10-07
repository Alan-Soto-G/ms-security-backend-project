/*
 * Controlador para gestionar la relación entre usuarios y roles.
 * Permite asignar roles a usuarios, consultar relaciones y eliminarlas.
 *
 * Anotaciones principales:
 * @RestController: Define la clase como controlador REST.
 * @RequestMapping: URL base para los endpoints de relación usuario-rol.
 * @Autowired: Inyección automática de los repositorios necesarios.
 * @CrossOrigin: Permite peticiones desde otros dominios.
 */
package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Role;
import fbc.ms_security.Models.User;
import fbc.ms_security.Models.UserRole;
import fbc.ms_security.Repositories.RoleRepository;
import fbc.ms_security.Repositories.UserRepository;
import fbc.ms_security.Repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "${frontend.url}")
@RestController // Trabajar con API REST
@RequestMapping("/api/user-role") // URL de la api donde se activa este controlador
public class UserRoleController {
    @Autowired // Inyección automática del repositorio de relación usuario-rol
    private UserRoleRepository theUserRoleRepository;

    @Autowired // Inyección automática del repositorio de usuarios
    private UserRepository theUserRepository;

    @Autowired // Inyección automática del repositorio de roles
    private RoleRepository theRoleRepository;

    /**
     * Obtiene todas las relaciones usuario-rol.
     * GET /api/user-role
     */
    @GetMapping("")
    public List<UserRole> find() {
        return this.theUserRoleRepository.findAll();
    }

    /**
     * Busca una relación usuario-rol por su ID.
     * GET /api/user-role/{id}
     */
    @GetMapping("{id}")
    public UserRole findById(@PathVariable String id) {
        UserRole theUserRole = this.theUserRoleRepository.findById(id).orElse(null);
        return theUserRole;
    }

    /**
     * Obtiene todos los roles asignados a un usuario.
     * GET /api/user-role/user/{userId}
     */
    @GetMapping("user/{userId}")
    public List<UserRole> getRolesByUser(@PathVariable String userId) {
        return this.theUserRoleRepository.getRolesByUser(userId);
    }

    /**
     * Obtiene todos los usuarios que tienen un rol específico.
     * GET /api/user-role/role/{roleId}
     */
    @GetMapping("role/{roleId}")
    public List<UserRole> getUserByRole(@PathVariable String roleId) {
        return this.theUserRoleRepository.getUsersByRole(roleId);
    }

    /**
     * Asigna un rol a un usuario.
     * POST /api/user-role/user/{userId}/role/{roleId}
     */
    @PostMapping("user/{userId}/role/{roleId}")
    public UserRole create(@PathVariable String userId, @PathVariable String roleId) {
        User theUser = this.theUserRepository.findById(userId).orElse(null);
        Role theRole = this.theRoleRepository.findById(roleId).orElse(null);
        if (theUser != null && theRole != null) {
            UserRole newUserRole = new UserRole();
            newUserRole.setUser(theUser);
            newUserRole.setRole(theRole);
            return this.theUserRoleRepository.save(newUserRole);
        }else  {
            return null;
        }
    }

    /**
     * Elimina una relación usuario-rol por su ID.
     * DELETE /api/user-role/{id}
     */
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        this.theUserRoleRepository.deleteById(id);
    }

    /**
     * Elimina una relación usuario-rol específica.
     * DELETE /api/user-role/user/{userId}/role/{roleId}
     */
    @DeleteMapping("user/{userId}/role/{roleId}")
    public void deleteMatch(@PathVariable String userId, @PathVariable String roleId) {
        Optional<UserRole> theUserRole = this.theUserRoleRepository.findByUserIdAndRoleId(userId, roleId);
        String IdUserRole = theUserRole.get().get_id();
        this.theUserRoleRepository.deleteById(IdUserRole);
    }
}