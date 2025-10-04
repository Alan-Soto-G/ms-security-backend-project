package fbc.ms_security.Services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import fbc.ms_security.Models.User;
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
}
