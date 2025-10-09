package fbc.ms_security.Services;

import fbc.ms_security.Models.Entities.Permission;
import fbc.ms_security.Models.Entities.Role;
import fbc.ms_security.Models.Entities.User;
import fbc.ms_security.Models.Relations.RolePermission;
import fbc.ms_security.Models.Relations.UserRole;
import fbc.ms_security.Repositories.PermissionRepository;
import fbc.ms_security.Repositories.RolePermissionRepository;
import fbc.ms_security.Repositories.UserRepository;
import fbc.ms_security.Repositories.UserRoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ValidatorsService {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private PermissionRepository thePermissionRepository;
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private RolePermissionRepository theRolePermissionRepository;

    @Autowired
    private UserRoleRepository theUserRoleRepository;

    private static final String BEARER_PREFIX = "Bearer ";
    public boolean validationRolePermission(HttpServletRequest request,
                                            String url,
                                            String method){
        boolean success=false;
        try {
            User theUser=this.getUser(request);
            System.out.println("Usuario "+theUser);
            if(theUser!=null){
                System.out.println("Antes URL "+url+" metodo "+method);
                url = url.replaceAll("[0-9a-fA-F]{24}|\\d+", "?");
                System.out.println("URL "+url+" metodo "+method);
                Permission thePermission=this.thePermissionRepository.getPermission(url,method);
                System.out.println("Permiso encontrado: " + (thePermission != null ? thePermission.get_id() : "null"));

                if(thePermission != null) {
                    List<UserRole> roles=this.theUserRoleRepository.getRolesByUser(theUser.get_id());
                    System.out.println("Roles encontrados: " + roles.size());

                    int i = 0;
                    while(i < roles.size() && success == false){
                        UserRole actual = roles.get(i);
                        Role theRole = actual.getRole();
                        if(theRole != null){
                            System.out.println("Rol "+theRole.get_id()+ " Permission "+thePermission.get_id());
                            RolePermission theRolePermission=this.theRolePermissionRepository.getRolePermission(theRole.get_id(),thePermission.get_id());
                            if (theRolePermission!=null){
                                success=true;
                            }
                        }
                        i+=1;
                    }
                } else {
                    System.out.println("No se encontró permiso para URL: " + url + " y método: " + method);
                }
            } else {
                System.out.println("Usuario no encontrado o token inválido");
            }
        } catch (Exception e) {
            System.err.println("Error en validationRolePermission: " + e.getMessage());
            e.printStackTrace();
            success = false;
        }
        return success;
    }
    public User getUser(final HttpServletRequest request) {
        User theUser=null;
        String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Header "+authorizationHeader);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String token = authorizationHeader.substring(BEARER_PREFIX.length());
            System.out.println("Bearer Token: " + token);
            User theUserFromToken=jwtService.getUserFromToken(token);
            System.out.println();
            if(theUserFromToken!=null) {
                theUser= this.theUserRepository.findById(theUserFromToken.get_id())
                        .orElse(null);

            }
        }
        return theUser;
    }
}
