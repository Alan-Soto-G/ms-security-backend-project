/*
 * Controlador para gestionar los permisos del sistema.
 * Proporciona endpoints para crear, leer, actualizar y listar permisos.
 *
 * Anotaciones principales:
 * @RestController: Indica que esta clase es un controlador REST, responde con JSON.
 * @RequestMapping: Define la URL base para los endpoints de este controlador.
 * @Autowired: Inyecta automáticamente las dependencias (repositorios) necesarias.
 * @CrossOrigin: Permite peticiones desde otros dominios (CORS).
 */
package fbc.ms_security.Controllers;

import fbc.ms_security.Models.Permission;
import fbc.ms_security.Repositories.SessionRepository;
import fbc.ms_security.Repositories.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController // Trabajar con API REST
@RequestMapping("/api/permissions") // URL de la api donde se activa este controlador
public class PermissionsController {
    @Autowired // Inyección automática del repositorio de permisos
    private PermissionRepository thePermissionRepository;

    @Autowired // Inyección automática del repositorio de sesiones
    private SessionRepository theSessionRepository;

    /**
     * Obtiene todos los permisos registrados.
     * Método GET /api/permissions
     */
    @GetMapping("")
    public List<Permission> find() {
        return this.thePermissionRepository.findAll();
    }

    /**
     * Busca un permiso por su ID.
     * Método GET /api/permissions/{id}
     */
    @GetMapping("{id}")
    public Permission findById(@PathVariable String id) {
        Permission thePermission = this.thePermissionRepository.findById(id).orElse(null);
        return thePermission;
    }

    /**
     * Crea un nuevo permiso.
     * Método POST /api/permissions
     */
    @PostMapping
    public ResponseEntity create(@RequestBody Permission newPermission) {
        Permission checkPermission = this.thePermissionRepository.getPermission(newPermission.getUrl(), newPermission.getMethod());
        if (checkPermission != null) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "error", "Permission already exists",
                            "status", HttpStatus.CONFLICT.value()
                    ));
        }else {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(this.thePermissionRepository.save(newPermission));
        }
    }

    /**
     * Actualiza un permiso existente por su ID.
     * Método PUT /api/permissions/{id}
     */
    @PutMapping("{id}")
    public Permission update(@PathVariable String id, @RequestBody Permission newPermission) {
        Permission actualPermission = this.thePermissionRepository.findById(id).orElse(null);
        if (actualPermission != null) {
            // Actualiza los campos del permiso
            actualPermission.setUrl(newPermission.getUrl());
            actualPermission.setMethod(newPermission.getMethod());
            actualPermission.setUrl(newPermission.getUrl());
            this.thePermissionRepository.save(actualPermission);
            return actualPermission;
        } else {
            return null;
        }
    }

    /**
     * Elimina un permiso por su ID.
     * Método DELETE /api/permissions/{id}
     */
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        Permission thePermission = this.thePermissionRepository.findById(id).orElse(null);
        if (thePermission != null) {
            this.thePermissionRepository.delete(thePermission);
        }
    }
}
