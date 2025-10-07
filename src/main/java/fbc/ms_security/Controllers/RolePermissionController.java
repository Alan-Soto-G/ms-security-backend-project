/*
 * Controlador para gestionar la relación entre roles y permisos.
 * Permite asignar permisos a roles, consultar relaciones y eliminarlas.
 *
 * Anotaciones principales:
 * @RestController: Define la clase como controlador REST.
 * @RequestMapping: URL base para los endpoints de relación rol-permiso.
 * @Autowired: Inyección automática de los repositorios necesarios.
 * @CrossOrigin: Permite peticiones desde otros dominios.
 */
package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Permission;
import fbc.ms_security.Models.Role;
import fbc.ms_security.Models.RolePermission;
import fbc.ms_security.Repositories.RoleRepository;
import fbc.ms_security.Repositories.RolePermissionRepository;
import fbc.ms_security.Repositories.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "${frontend.url}")
@RestController // Trabajar con API REST
@RequestMapping("/api/role-permission") // URL de la api donde se activa este controlador
public class RolePermissionController {
    @Autowired // Inyección automática del repositorio de relación rol-permiso
    private RolePermissionRepository theRolePermissionRepository;

    @Autowired // Inyección automática del repositorio de permisos
    private PermissionRepository thePermissionRepository;

    @Autowired // Inyección automática del repositorio de roles
    private RoleRepository theRoleRepository;

    /**
     * Obtiene todas las relaciones rol-permiso.
     * GET /api/role-permission
     */
    @GetMapping("")
    public List<RolePermission> find() {
        return this.theRolePermissionRepository.findAll();
    }

    /**
     * Busca una relación rol-permiso por su ID.
     * GET /api/role-permission/{id}
     */
    @GetMapping("{id}")
    public RolePermission findById(@PathVariable String id) {
        RolePermission theRolePermission = this.theRolePermissionRepository.findById(id).orElse(null);
        return theRolePermission;
    }

    /**
     * Obtiene todas las relaciones por ID de permiso.
     * GET /api/role-permission/permission/{permissionId}
     */
    @GetMapping("permission/{permissionId}")
    public List<RolePermission> getRolesByPermission(@PathVariable String permissionId) {
        return this.theRolePermissionRepository.getRolesByPermission(permissionId);
    }

    /**
     * Obtiene todas las relaciones por ID de rol.
     * GET /api/role-permission/role/{roleId}
     */
    @GetMapping("role/{roleId}")
    public List<RolePermission> getPermissionByRole(@PathVariable String roleId) {
        return this.theRolePermissionRepository.getPermissionByRole(roleId);
    }

    /**
     * Asigna un permiso a un rol.
     * POST /api/role-permission/role/{roleId}/permission/{permissionId}
     */
    @PostMapping("role/{roleId}/permission/{permissionId}")
    public RolePermission create(@PathVariable String permissionId, @PathVariable String roleId) {
        Permission thePermission = this.thePermissionRepository.findById(permissionId).orElse(null);
        Role theRole = this.theRoleRepository.findById(roleId).orElse(null);
        if (thePermission != null && theRole != null) {
            RolePermission newRolePermission = new RolePermission();
            newRolePermission.setPermission(thePermission);
            newRolePermission.setRole(theRole);
            return this.theRolePermissionRepository.save(newRolePermission);
        }else  {
            return null;
        }
    }

    /**
     * Elimina una relación rol-permiso por su ID.
     * DELETE /api/role-permission/{id}
     */
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        this.theRolePermissionRepository.deleteById(id);
    }

    /**
     * Elimina una relación rol-permiso específica.
     * DELETE /api/role-permission/role/{roleId}/permission/{permissionId}
     */
    @DeleteMapping("role/{roleId}/permission/{permissionId}")
    public void deleteMatch(@PathVariable String roleId, @PathVariable String permissionId) {
        RolePermission theRolePermission = this.theRolePermissionRepository.getRolePermission(roleId, permissionId);

        String rolePermissionId = theRolePermission.get_id();
        this.theRolePermissionRepository.deleteById(rolePermissionId);
    }
}