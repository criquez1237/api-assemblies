package com.assembliestore.api.module.user.application.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.assembliestore.api.module.user.application.command.RegisterCommand;
import com.assembliestore.api.common.error.CredentialInvalidExeption;
import com.assembliestore.api.common.error.EmailAlreadyExistsException;
import com.assembliestore.api.common.error.InfrastructureException;
import com.assembliestore.api.module.user.application.command.AuthenticationCommand;
import com.assembliestore.api.module.user.application.dto.JwtTokenDto;
import com.assembliestore.api.module.user.application.port.AuthPort;
import com.assembliestore.api.module.user.domain.entities.Perfil;
import com.assembliestore.api.module.user.domain.entities.Token;
import com.assembliestore.api.module.user.domain.entities.User;
import com.assembliestore.api.module.user.domain.port.JwtPort;
import com.assembliestore.api.module.user.domain.repository.TokenRepository;
import com.assembliestore.api.module.user.domain.repository.UserRepository;
import com.assembliestore.api.module.user.application.services.OTPService;
import com.assembliestore.api.service.email.EmailService;
import com.assembliestore.api.service.email.dto.EmailRequest;

@Service
public class AuthService implements AuthPort {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final PasswordEncoder passwordEncoder;
    private final UserRepository _userRepository;
    private final JwtTokenService _tokenService;
    private final AuthenticationManager _authenticationManager;
    private final TokenRepository _tokenRepository;
    private final JwtPort _jwtPort;
    private final EmailService _emailService;
    private final OTPService _otpService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService tokenService,
            AuthenticationManager authenticationManager,
            JwtPort jwtPort,
            TokenRepository tokenRepository,
            EmailService emailService,
            OTPService otpService
            ) {

        this._userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this._tokenService = tokenService;
        this._authenticationManager = authenticationManager;
        this._jwtPort = jwtPort;
        this._tokenRepository = tokenRepository;
        this._emailService = emailService;
        this._otpService = otpService;
    }

    @Override
    public JwtTokenDto register(RegisterCommand command) {

        if (this._userRepository.findByEmail(command.email()).isPresent()) {
            throw new EmailAlreadyExistsException("El correo ya está registrado");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUserName("ekjricnriu");
        user.setEmail(command.email());
        user.setPassword(passwordEncoder.encode(command.password()));
        user.setRole(com.assembliestore.api.module.user.common.enums.Role.CLIENT); // Por defecto cliente
        Perfil perfil = new Perfil();
        perfil.setNames(command.names());
        perfil.setSurnames(command.surnames());
        perfil.setImagePerfil(command.imagePerfil());
        perfil.setPhone(command.phone());
        user.setPerfil(perfil);

        this._userRepository.create(user);

        // Generar y enviar OTP en lugar de tokens
        String userName = user.getPerfil().getNames() + " " + user.getPerfil().getSurnames();
        _otpService.generateAndSendOTP(user.getId(), user.getEmail(), userName);

        // No generar tokens aquí, se generarán después de la verificación OTP
        // return null para indicar que el registro requiere verificación OTP
        return null;
    }

    public JwtTokenDto registerWithOTP(RegisterCommand command) {
        if (this._userRepository.findByEmail(command.email()).isPresent()) {
            throw new EmailAlreadyExistsException("El correo ya está registrado");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUserName("ekjricnriu");
        user.setEmail(command.email());
        user.setPassword(passwordEncoder.encode(command.password()));
        user.setRole(com.assembliestore.api.module.user.common.enums.Role.CLIENT);
        user.setActived(false); // Usuario inactivo hasta verificar OTP
        
        Perfil perfil = new Perfil();
        perfil.setNames(command.names());
        perfil.setSurnames(command.surnames());
        perfil.setImagePerfil(command.imagePerfil());
        perfil.setPhone(command.phone());
        user.setPerfil(perfil);

        this._userRepository.create(user);

        // Generar y enviar OTP
        String userName = user.getPerfil().getNames() + " " + user.getPerfil().getSurnames();
        _otpService.generateAndSendOTP(user.getId(), user.getEmail(), userName);

        return null; // No se devuelven tokens hasta verificar OTP
    }

    public JwtTokenDto verifyOTPAndActivate(String email, String otpCode) {
        // Verificar el código OTP
        boolean isValidOTP = _otpService.verifyOTP(email, otpCode);
        if (!isValidOTP) {
            throw new RuntimeException("Código OTP inválido o expirado");
        }

        // Buscar el usuario por email
        Optional<User> userOpt = _userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        User user = userOpt.get();
        
        // Activar la cuenta
        user.setActived(true);
        _userRepository.update(user);

        // Generar tokens JWT
        JwtTokenDto tokens = _tokenService.generateToken(user);

        // Enviar email de bienvenida ahora que la cuenta está activada
        try {
            sendWelcomeEmail(user);
        } catch (Exception e) {
            logger.warn("No se pudo enviar el email de bienvenida para el usuario: {}. Error: {}", 
                       user.getEmail(), e.getMessage());
        }

        return tokens;
    }

    @Override
    public JwtTokenDto authentication(AuthenticationCommand command) {

        try {

            _authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            command.email(),
                            command.password()));

            Optional<User> response = _userRepository.findByEmail(command.email());

            /*
             * if (!response.isPresent()) {
             * throw new CredentialInvalidExeption(
             * "El usuario no existe o las credenciales son invalidas");
             * }
             */

            // mejorar codigo mas despues
            User user = response.get();
            /*
             * if (!passwordEncoder.matches(command.password(), user.getPassword())) {
             * throw new CredentialInvalidExeption(
             * "El usuario no existe o las credenciales son invalidas");
             * }
             */
            if (!user.isActived()) {
                throw new CredentialInvalidExeption(
                        "El usuario no existe o las credenciales son invalidas");
            }

            JwtTokenDto tokens = _tokenService.generateToken(user);

            return tokens;

        } catch (CredentialInvalidExeption error) {
            throw error;

        } catch (Exception error) {
            throw new InfrastructureException("A ocurrido un error interno");
        }
    }

    @Override
    public JwtTokenDto refreshToken(String refreshToken) {

        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {

            throw new CredentialInvalidExeption("El token de actualización es inválido");
        }

        final String token = refreshToken.substring(7);

        final String userName = _jwtPort.getUserNameFromToken(token);

        if (userName == null) {

            throw new CredentialInvalidExeption("El token de actualización es inválido");
        }

        Optional<User> user = _userRepository.findByEmail(userName);

        if (!user.isPresent()) {
            throw new CredentialInvalidExeption(
                    "El usuario no existe o las credenciales son invalidas");
        }

        if (!_jwtPort.validateToken(token, user.get())) {

            throw new CredentialInvalidExeption("El token de actualización es inválido");
        }

        final JwtTokenDto accessToken = _tokenService.generateToken(user.get());

        return accessToken;
    }

    @Override
    public JwtTokenDto logout(String token) {

        final Optional<Token> jwtTokenDto = _tokenRepository.findByToken(token);

        if (!jwtTokenDto.isPresent()) {
            throw new CredentialInvalidExeption("El token no existe");
        }

        jwtTokenDto.get().setExpired(true);
        jwtTokenDto.get().setRevoked(true);


        JwtTokenDto updatedToken = JwtTokenDto.builder()
                .id(jwtTokenDto.get().getId())
                .token(jwtTokenDto.get().getToken())
                .refreshToken(jwtTokenDto.get().getRefreshToken())
                .type(jwtTokenDto.get().getType())
                .revoked(jwtTokenDto.get().isRevoked())
                .expired(jwtTokenDto.get().isExpired())
                .userId(jwtTokenDto.get().getUserId())
                .updatedAt(jwtTokenDto.get().getUpdatedAt())
                .build();

        _tokenService.updateToken(updatedToken);

        return updatedToken;
    }

    private void sendWelcomeEmail(User user) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", user.getPerfil().getNames() + " " + user.getPerfil().getSurnames());

            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setTo(user.getEmail());
            emailRequest.setSubject("¡Bienvenido a Assemblies Store!");
            emailRequest.setTemplateName("welcome");
            emailRequest.setVariables(variables);

            _emailService.sendEmail(emailRequest);
            
            logger.info("Email de bienvenida enviado exitosamente a: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Error al enviar email de bienvenida a {}: {}", user.getEmail(), e.getMessage());
            throw e;
        }
    }
    
}
