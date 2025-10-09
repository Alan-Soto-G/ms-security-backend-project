package fbc.ms_security.Services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import fbc.ms_security.Models.Entities.User;
import fbc.ms_security.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    public Map<String, Object> loginWithGoogle(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();

            User user = userRepository.getUserByEmail(email);
            if (user == null) {
                user = new User();
                user.setName(name);
                user.setEmail(email);
                user.setPassword(null);
                userRepository.save(user);
            }

            String jwt = jwtService.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("jwt", jwt);
            response.put("user", user);
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error verificando token de Google: " + e.getMessage());
        }
    }

    // Agregado 05/10/2025 - Método para autenticación con Microsoft
    // Utiliza la misma lógica que Google pero específicamente para tokens de Microsoft OAuth
    public Map<String, Object> loginWithMicrosoft(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();

            User user = userRepository.getUserByEmail(email);
            if (user == null) {
                user = new User();
                user.setName(name);
                user.setEmail(email);
                user.setPassword(null);
                userRepository.save(user);
            }

            String jwt = jwtService.generateToken(user);

            Map<String, Object> response = new HashMap<>();
            response.put("jwt", jwt);
            response.put("user", user);
            return response;

        } catch (Exception e) {
            throw new RuntimeException("Error verificando token de Microsoft: " + e.getMessage());
        }
    }

    // Agregado 05/10/2025 - Método genérico para manejar múltiples proveedores OAuth
    // Permite extensibilidad para futuros proveedores sin duplicar código
    public Map<String, Object> loginWithProvider(String idToken, String provider) {
        switch (provider.toLowerCase()) {
            case "google":
                return loginWithGoogle(idToken);
            case "microsoft":
                return loginWithMicrosoft(idToken);
            default:
                throw new RuntimeException("Proveedor no soportado: " + provider);
        }
    }
}
