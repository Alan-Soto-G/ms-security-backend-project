package fbc.ms_security.Services;

import fbc.ms_security.Models.Entities.Permission;
import fbc.ms_security.Repositories.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    /**
     * Obtiene los permisos correspondientes a los índices especificados,
     * basándose en la lista de permisos ordenados por uso descendente.
     * Los índices fuera de rango son omitidos de la respuesta.
     *
     * @param indices Lista de índices de permisos a obtener
     * @return Lista de permisos correspondientes a los índices válidos
     */
    public List<Permission> getPermissionsByIndices(List<Integer> indices) {
        // Obtener todos los permisos ordenados por uso descendente
        List<Permission> allPermissionsByUsage = permissionRepository.findAllOrderByUsageDesc();

        List<Permission> result = new ArrayList<>();

        // Verificar que la lista de índices no sea nula o vacía
        if (indices == null || indices.isEmpty()) {
            return result;
        }

        // Iterar sobre los índices y obtener solo los permisos válidos
        for (Integer index : indices) {
            // Verificar que el índice sea válido (no nulo, no negativo y dentro del rango)
            if (index != null && index >= 0 && index < allPermissionsByUsage.size()) {
                result.add(allPermissionsByUsage.get(index));
            }
            // Los índices fuera de rango se omiten silenciosamente
        }

        return result;
    }

    /**
     * Obtiene todos los permisos ordenados por uso descendente
     *
     * @return Lista de todos los permisos ordenados por uso
     */
    public List<Permission> getAllPermissionsOrderedByUsage() {
        return permissionRepository.findAllOrderByUsageDesc();
    }
}
