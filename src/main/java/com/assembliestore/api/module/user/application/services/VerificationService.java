/**
 * 
 */
package com.assembliestore.api.module.user.application.services;

import org.springframework.stereotype.Service;
import com.assembliestore.api.module.user.domain.entities.User;

@Service
public class VerificationService {

    public Boolean validateToken(String token, String email) {
        // Implementar lógica de validación de token con Spring Security si es necesario
        return true;
    }
}
