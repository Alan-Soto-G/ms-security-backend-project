package fbc.ms_security.Services;

import fbc.ms_security.Models.Entities.Profile;
import fbc.ms_security.Models.Entities.Session;
import fbc.ms_security.Models.Entities.User;
import fbc.ms_security.Repositories.ProfileRepository;
import fbc.ms_security.Repositories.SessionRepository;
import fbc.ms_security.Repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Servicio para la gestión de usuarios.
 * Contiene toda la lógica de negocio relacionada con usuarios.
 */
@Slf4j
@Transactional
@Service
public class UserService {
    private UserRepository theUserRepository;
    private SessionRepository theSessionRepository;
    private ProfileRepository theProfileRepository;
    private EncryptionService theEncryptionService;

    public UserService(UserRepository theUserRepository, SessionRepository theSessionRepository, ProfileRepository theProfileRepository, EncryptionService theEncryptionService) {
        this.theUserRepository = theUserRepository;
        this.theSessionRepository = theSessionRepository;
        this.theProfileRepository = theProfileRepository;
        this.theEncryptionService = theEncryptionService;
        log.info("UserService inicializado correctamente");
    }

    /**
     * Obtiene todos los usuarios del sistema.
     *
     * @return Lista de todos los usuarios
     */
    public List<User> findAll() {
        return this.theUserRepository.findAll();
    }

    /**
     * Busca un usuario por su ID.
     *
     * @param id ID del usuario
     * @return Usuario encontrado o null si no existe
     */
    public User findById(String id) {
        return this.theUserRepository.findById(id).orElse(null);
    }

    /**
     * Busca un usuario por su email.
     *
     * @param email Email del usuario
     * @return Usuario encontrado o null si no existe
     */
    public User findByEmail(String email) {
        return this.theUserRepository.getUserByEmail(email);
    }

    /**
     * Crea un nuevo usuario con validaciones.
     * - Verifica que el email no exista
     * - Encripta la contraseña
     * - Crea un perfil asociado automáticamente
     *
     * @param newUser Usuario a crear
     * @return Usuario creado (con password enmascarada)
     * @throws ResponseStatusException si el email ya existe
     */
    public User create(User newUser) {
        // Validar que el email no exista
        User existingUser = this.theUserRepository.getUserByEmail(newUser.getEmail());
        if (existingUser != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already exists"
            );
        }

        // Encriptar contraseña
        newUser.setPassword(this.theEncryptionService.convertSHA256(newUser.getPassword()));
        // Guardar usuario
        User savedUser = this.theUserRepository.save(newUser);

        // Crear perfil asociado
        Profile newProfile = new Profile();
        newProfile.setUser(savedUser);
        newProfile.setPhone("");
        newProfile.setPhoto(null);
        this.theProfileRepository.save(newProfile);

        // Enmascarar contraseña para la respuesta
        savedUser.setPassword("********");

        log.info("Usuario creado exitosamente: {}", savedUser.getEmail());
        return savedUser;
    }

    /**
     * Actualiza un usuario existente.
     * - Verifica que el usuario exista
     * - Actualiza name, email y password
     * - Encripta la nueva contraseña
     *
     * @param id ID del usuario a actualizar
     * @param newUser Datos nuevos del usuario
     * @return Usuario actualizado o null si no existe
     */
    public User update(String id, User newUser) {
        User actualUser = this.theUserRepository.findById(id).orElse(null);
        if (actualUser == null) {
            return null;
        }

        // Actualizar campos
        actualUser.setName(newUser.getName());
        actualUser.setEmail(newUser.getEmail());
        actualUser.setPassword(this.theEncryptionService.convertSHA256(newUser.getPassword()));

        // Guardar cambios
        this.theUserRepository.save(actualUser);

        // Enmascarar contraseña para la respuesta
        actualUser.setPassword("********");

        log.info("Usuario actualizado: {}", actualUser.getEmail());
        return actualUser;
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param id ID del usuario a eliminar
     * @return true si se eliminó, false si no existe
     */
    public boolean delete(String id) {
        User theUser = this.theUserRepository.findById(id).orElse(null);
        if (theUser == null) {
            return false;
        }

        this.theUserRepository.delete(theUser);
        log.info("Usuario eliminado: {}", theUser.getEmail());
        return true;
    }

    /**
     * Asocia una sesión a un usuario.
     *
     * @param userId ID del usuario
     * @param sessionId ID de la sesión
     * @return true si se asoció correctamente, false si no existe usuario o sesión
     */
    public boolean matchSession(String userId, String sessionId) {
        Session theSession = this.theSessionRepository.findById(sessionId).orElse(null);
        User theUser = this.theUserRepository.findById(userId).orElse(null);
        if (theSession == null || theUser == null) {
            return false;
        }

        theSession.setUser(theUser);
        this.theSessionRepository.save(theSession);

        log.info("Sesión {} asociada al usuario {}", sessionId, theUser.getEmail());
        return true;
    }

    /**
     * Verifica si un email ya está registrado.
     *
     * @param email Email a verificar
     * @return true si existe, false si no
     */
    public boolean emailExists(String email) {
        return this.theUserRepository.getUserByEmail(email) != null;
    }
}
