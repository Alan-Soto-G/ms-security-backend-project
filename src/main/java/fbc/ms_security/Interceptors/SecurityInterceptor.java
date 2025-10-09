package fbc.ms_security.Interceptors;

import fbc.ms_security.Models.Entities.Permission;
import fbc.ms_security.Repositories.PermissionRepository;
import fbc.ms_security.Services.ValidatorsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class SecurityInterceptor implements HandlerInterceptor {
    private ValidatorsService validatorService;
    private PermissionRepository permissionRepository;

    public SecurityInterceptor(ValidatorsService validatorService, PermissionRepository permissionRepository) {
        this.validatorService = validatorService;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean success = this.validatorService.validationRolePermission(request, request.getRequestURI(), request.getMethod());

        // Si la validación es exitosa, incrementar el contador de uso del permiso
        if (success) {
            try {
                // Buscar el permiso correspondiente a la URL y método
                Permission permission = this.permissionRepository.getPermission(request.getRequestURI(), request.getMethod());

                // Si el permiso existe, incrementar su uso y guardarlo
                if (permission != null) {
                    permission.incrementUsage();
                    this.permissionRepository.save(permission);
                }
            } catch (Exception e) {
                // Log del error pero no interrumpir el flujo normal
                // El incremento del contador es una funcionalidad adicional, no crítica
                System.err.println("Error al incrementar el contador de uso del permiso: " + e.getMessage());
            }
        }

        return success;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // Lógica a ejecutar después de que se haya manejado la solicitud por el controlador
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) throws Exception {
        // Lógica a ejecutar después de completar la solicitud, incluso después de la renderización de la vista
    }
}
