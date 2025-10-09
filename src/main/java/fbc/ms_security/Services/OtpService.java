package fbc.ms_security.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final long EXPIRATION_TIME = 5; // Minutos

    // Guardar OTP en Redis con expiración
    public void saveOtp(String identifier, String otpCode) {
        redisTemplate.opsForValue()
                .set("otp:" + identifier, otpCode, EXPIRATION_TIME, TimeUnit.MINUTES);
    }

    // Obtener OTP
    public String getOtp(String identifier) {
        return redisTemplate.opsForValue().get("otp:" + identifier);
    }

    // Eliminar OTP
    public boolean deleteOtp(String identifier) {
        return Boolean.TRUE.equals(redisTemplate.delete("otp:" + identifier));
    }

    // Validar OTP
    public boolean validateOtp(String identifier, String inputCode) {
        String savedCode = getOtp(identifier);
        if (savedCode != null && savedCode.equals(inputCode)) {
            deleteOtp(identifier); // ⚡ OTP de un solo uso
            return true;
        }
        return false;
    }
}
