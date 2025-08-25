package com.assembliestore.api.module.user.infrastructure.adapter.in.api.controllers;

import com.assembliestore.api.module.user.infrastructure.adapter.dto.SignInRequest;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.SignUpRequest;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.VerifyOTPRequest;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.TokenResponse;
import com.assembliestore.api.module.user.infrastructure.adapter.mapper.SignInMapper;
import com.assembliestore.api.module.user.infrastructure.adapter.mapper.SignUpMapper;
import com.assembliestore.api.module.user.application.dto.JwtTokenDto;
import com.assembliestore.api.module.user.application.port.AuthPort;
import com.assembliestore.api.module.user.application.services.VerificationService;
import com.assembliestore.api.module.user.domain.repository.UserRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.assembliestore.api.service.storage.dto.FileUploadResponse;
import com.assembliestore.api.service.storage.service.CloudinaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.assembliestore.api.config.AppEnvConfig;
import com.assembliestore.api.common.response.ApiErrorResponse;
import com.assembliestore.api.common.response.ApiSuccessResponse;
import com.assembliestore.api.common.response.ErrorDetail;
import com.assembliestore.api.common.response.ResponseUtil;
import com.assembliestore.api.common.response.TechnicalDetails;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Endpoints de autenticación")
public class AuthController {

        private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthPort _authPort;
    private final CloudinaryService cloudinaryService;
    private final AppEnvConfig appEnvConfig;
    private final com.assembliestore.api.module.user.application.services.OTPService otpService;
    private final com.assembliestore.api.module.user.domain.repository.UserRepository userRepository;
        private final com.assembliestore.api.module.cart.application.service.CartService cartService;

        public AuthController(
                        UserRepository userRepository,
                        VerificationService verificationService,
                        PasswordEncoder passwordEncoder,
                        AuthPort authPort,
                        CloudinaryService cloudinaryService,
                        AppEnvConfig appEnvConfig,
                        com.assembliestore.api.module.user.application.services.OTPService otpService,
                        com.assembliestore.api.module.cart.application.service.CartService cartService) {

        this._authPort = authPort;
        this.cloudinaryService = cloudinaryService;
        this.appEnvConfig = appEnvConfig;
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.cartService = cartService;
    }

    @PostMapping("/signin")
    @Operation(summary = "Iniciar sesión", description = "Permite a un usuario iniciar sesión en la aplicación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<?> signin(@RequestBody final SignInRequest request, HttpServletRequest httpRequest) {
        long start = System.currentTimeMillis();
        try {
            JwtTokenDto jwtTokenDto = _authPort.authentication(SignInMapper.toSignAuthenticationCommand(request));
            TokenResponse tokenResponse = SignUpMapper.toTokenResponse(jwtTokenDto);

            // Buscar usuario y construir objeto user reducido
            var userOpt = userRepository.findByEmail(request.getEmail());
            com.assembliestore.api.module.user.infrastructure.adapter.dto.UserInfo userInfo = null;
            if (userOpt.isPresent()) {
                var user = userOpt.get();
                var perfil = user.getPerfil();
                var perfilDto = new com.assembliestore.api.module.user.infrastructure.adapter.dto.PerfilDto(
                        perfil.getImagePerfil(), perfil.getNames(), perfil.getSurnames(), perfil.getPhone());
                userInfo = new com.assembliestore.api.module.user.infrastructure.adapter.dto.UserInfo(user.getEmail(), user.getRole(), perfilDto);
            }

                        var payload = new com.assembliestore.api.module.user.infrastructure.adapter.dto.TokenWithUserResponse(tokenResponse, userInfo);
                        // populate additionalData.totalItemCart if possible
                        try {
                                if (userInfo != null) {
                                        // attempt to fetch cart count for user id
                                        if (userOpt.isPresent()) {
                                                var uid = userOpt.get().getId();
                                                                var cartResp = cartService.getCart(uid, 1, 1, null, null);
                                                if (cartResp != null && cartResp.getPagination() != null) {
                                                        var add = new com.assembliestore.api.module.user.infrastructure.adapter.dto.AdditionalDataDto(cartResp.getPagination().getTotalItems());
                                                        payload.setAdditionalData(add);
                                                }
                                        }
                                }
                        } catch (Exception ex) {
                                // don't block signin on cart errors
                        }

            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiSuccessResponse<com.assembliestore.api.module.user.infrastructure.adapter.dto.TokenWithUserResponse> resp = new ApiSuccessResponse<>("Inicio de sesión exitoso",
                    "AUTH_SIGNIN_SUCCESS", payload, tech);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiErrorResponse error = new ApiErrorResponse("Credenciales inválidas", "AUTH_SIGNIN_ERROR",
                    java.util.Arrays.asList(new ErrorDetail("authentication", e.getMessage())), tech);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/signup")
    @Operation(summary = "Registrarse", description = "Registra un nuevo usuario y envía código OTP para activación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registro exitoso - Se envió código OTP"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request, HttpServletRequest httpRequest) {
        long start = System.currentTimeMillis();
        try {
            _authPort.registerWithOTP(SignUpMapper.toRegisterCommand(request));

            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiSuccessResponse<java.util.Map<String, String>> resp = new ApiSuccessResponse<>(
                    "Usuario registrado con éxito. Por favor, verifica tu correo electrónico.",
                    "USER_REGISTERED_SUCCESS",
                    java.util.Map.of("email", request.getEmail()),
                    tech);
            return new ResponseEntity<>(resp, HttpStatus.CREATED);

        } catch (Exception e) {
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiErrorResponse error = new ApiErrorResponse("Error en el registro", "USER_REGISTER_ERROR",
                    java.util.Arrays.asList(new ErrorDetail("registration", e.getMessage())), tech);
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Registrarse (multipart)", description = "Registra un nuevo usuario con upload de imagen de perfil y envía código OTP para activación")
    public ResponseEntity<?> signupMultipart(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String names,
            @RequestParam String surnames,
            @RequestParam(required = false) String phone,
            @RequestPart(required = false) MultipartFile imagePerfil,
            HttpServletRequest httpRequest) {
        long start = System.currentTimeMillis();
        try {
            String imageUrl = null;

            if (imagePerfil != null && !imagePerfil.isEmpty()) {
                String prefix = email != null && email.contains("@") ? email.split("@")[0] : email;
                String folder = "user/" + (prefix != null ? prefix : "unknown");

                FileUploadResponse uploadResp = cloudinaryService.uploadImage(imagePerfil, folder, true);

                                if (uploadResp != null && uploadResp.isSuccess()) {
                                        // Defensive: secureUrl sometimes comes malformed (e.g. just "https"). Validate.
                                        String candidate = uploadResp.getSecureUrl();
                                        if (candidate == null || candidate.trim().isEmpty() || !candidate.startsWith("http")) {
                                                // Try fallback to non-secure url
                                                String fallback = uploadResp.getUrl();
                                                if (fallback != null && fallback.startsWith("http")) {
                                                        imageUrl = fallback;
                                                        logger.warn("secureUrl invalid, using fallback url for upload: {} -> {}", uploadResp.getSecureUrl(), fallback);
                                                } else {
                                                        // Both urls invalid
                                                        String msg = "Cloudinary returned invalid URLs: secureUrl=" + uploadResp.getSecureUrl() + ", url=" + uploadResp.getUrl();
                                                        logger.error(msg);
                                                        TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest,
                                                                        System.currentTimeMillis() - start, appEnvConfig);
                                                        ApiErrorResponse error = new ApiErrorResponse("Falló la subida de la imagen: " + msg,
                                                                        "UPLOAD_ERROR",
                                                                        java.util.Arrays.asList(new ErrorDetail("imagePerfil", msg)), tech);
                                                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                                                }
                                        } else {
                                                imageUrl = candidate;
                                        }
                                } else {
                                        String msg = uploadResp != null ? uploadResp.getMessage() : "Error al subir imagen";
                                        TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest,
                                                        System.currentTimeMillis() - start, appEnvConfig);
                                        ApiErrorResponse error = new ApiErrorResponse("Falló la subida de la imagen: " + msg,
                                                        "UPLOAD_ERROR",
                                                        java.util.Arrays.asList(new ErrorDetail("imagePerfil", msg)), tech);
                                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                                }
            }

            SignUpRequest request = new SignUpRequest();
            request.setEmail(email);
            request.setPassword(password);
            request.setNames(names);
            request.setSurnames(surnames);
            request.setPhone(phone);
            request.setImagePerfil(imageUrl);

            _authPort.registerWithOTP(SignUpMapper.toRegisterCommand(request));

            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiSuccessResponse<java.util.Map<String, String>> resp = new ApiSuccessResponse<>(
                    "Usuario registrado con éxito. Por favor, verifica tu correo electrónico.",
                    "USER_REGISTERED_SUCCESS",
                    java.util.Map.of("email", email),
                    tech);
            return new ResponseEntity<>(resp, HttpStatus.CREATED);

        } catch (Exception e) {
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiErrorResponse error = new ApiErrorResponse("Error en el registro multipart: " + e.getMessage(),
                    "USER_REGISTER_ERROR",
                    java.util.Arrays.asList(new ErrorDetail("registration", e.getMessage())), tech);
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verificar OTP", description = "Verifica el código OTP y activa la cuenta del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verificado - Cuenta activada"),
            @ApiResponse(responseCode = "400", description = "Código OTP inválido o expirado")
    })
    public ResponseEntity<?> verifyOTP(@RequestBody VerifyOTPRequest request, HttpServletRequest httpRequest) {
        long start = System.currentTimeMillis();
        try {
            JwtTokenDto jwtTokenDto = _authPort.verifyOTPAndActivate(request.getEmail(), request.getOtpCode());
            TokenResponse tokenResponse = SignUpMapper.toTokenResponse(jwtTokenDto);

            // Buscar usuario y construir objeto user reducido
            var userOpt = userRepository.findByEmail(request.getEmail());
            com.assembliestore.api.module.user.infrastructure.adapter.dto.UserInfo userInfo = null;
            if (userOpt.isPresent()) {
                var user = userOpt.get();
                var perfil = user.getPerfil();
                var perfilDto = new com.assembliestore.api.module.user.infrastructure.adapter.dto.PerfilDto(
                        perfil.getImagePerfil(), perfil.getNames(), perfil.getSurnames(), perfil.getPhone());
                userInfo = new com.assembliestore.api.module.user.infrastructure.adapter.dto.UserInfo(user.getEmail(), user.getRole(), perfilDto);
            }

                        var payload = new com.assembliestore.api.module.user.infrastructure.adapter.dto.TokenWithUserResponse(tokenResponse, userInfo);
                        try {
                                if (userInfo != null) {
                                        if (userOpt.isPresent()) {
                                                var uid = userOpt.get().getId();
                                                var cartResp = cartService.getCart(uid, 1, 1, null, null);
                                                if (cartResp != null && cartResp.getPagination() != null) {
                                                        var add = new com.assembliestore.api.module.user.infrastructure.adapter.dto.AdditionalDataDto(cartResp.getPagination().getTotalItems());
                                                        payload.setAdditionalData(add);
                                                }
                                        }
                                }
                        } catch (Exception ex) {
                        }

            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiSuccessResponse<com.assembliestore.api.module.user.infrastructure.adapter.dto.TokenWithUserResponse> resp = new ApiSuccessResponse<>("OTP verificado - Cuenta activada",
                    "OTP_VERIFIED", payload, tech);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiErrorResponse error = new ApiErrorResponse("Error al verificar OTP: " + e.getMessage(), "OTP_ERROR",
                    java.util.Arrays.asList(new ErrorDetail("otp", e.getMessage())), tech);
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Reenviar OTP", description = "Solicitar reenvío de código OTP si el usuario está registrado y no verificado")
    public ResponseEntity<?> resendOtp(
            @RequestBody com.assembliestore.api.module.user.infrastructure.adapter.dto.ResendOtpRequest request,
            HttpServletRequest httpRequest) {
        long start = System.currentTimeMillis();
        try {
            String email = request.getEmail();
            java.util.Optional<com.assembliestore.api.module.user.domain.entities.User> userOpt = userRepository
                    .findByEmail(email);

            if (!userOpt.isPresent()) {
                TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest,
                        System.currentTimeMillis() - start, appEnvConfig);
                ApiErrorResponse error = new ApiErrorResponse("El usuario no está registrado", "USER_NOT_FOUND",
                        java.util.Arrays.asList(new ErrorDetail("email", "Usuario no encontrado")), tech);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            com.assembliestore.api.module.user.domain.entities.User user = userOpt.get();

            if (user.isActived() && user.isVerified()) {
                TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest,
                        System.currentTimeMillis() - start, appEnvConfig);
                ApiErrorResponse error = new ApiErrorResponse("El usuario ya está verificado", "USER_ALREADY_VERIFIED",
                        java.util.Arrays.asList(new ErrorDetail("email", "Usuario ya verificado")), tech);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Generar y enviar nuevo OTP
            otpService.generateAndSendOTP(user.getId(), user.getEmail(),
                    user.getPerfil().getNames() + " " + user.getPerfil().getSurnames());

            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiSuccessResponse<java.util.Map<String, String>> resp = new ApiSuccessResponse<>(
                    "OTP reenviado correctamente", "OTP_RESENT", java.util.Map.of("email", email), tech);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiErrorResponse error = new ApiErrorResponse("Error al reenviar OTP: " + e.getMessage(),
                    "OTP_RESEND_ERROR", java.util.Arrays.asList(new ErrorDetail("resend", e.getMessage())), tech);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Actualizar token", description = "Permite a un usuario actualizar su token de acceso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token actualizado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token de actualización inválido")
    })
    public ResponseEntity<?> refreshToken(
            @RequestHeader("Authorization") String authorizationHeader,
            HttpServletRequest httpRequest) {
        long start = System.currentTimeMillis();
        try {
            JwtTokenDto jwtTokenDto = _authPort.refreshToken(authorizationHeader);
            TokenResponse tokenResponse = SignUpMapper.toTokenResponse(jwtTokenDto);
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiSuccessResponse<TokenResponse> resp = new ApiSuccessResponse<>("Token actualizado exitosamente",
                    "TOKEN_REFRESHED", tokenResponse, tech);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiErrorResponse error = new ApiErrorResponse("Token de actualización inválido", "TOKEN_REFRESH_ERROR",
                    java.util.Arrays.asList(new ErrorDetail("token", e.getMessage())), tech);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Permite a un usuario cerrar sesión en la aplicación")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cierre de sesión exitoso"),
            @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    public ResponseEntity<?> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            HttpServletRequest httpRequest) {
        long start = System.currentTimeMillis();
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Invalid Authorization header");
            }

            final String jwtToken = authHeader.substring(7);

            _authPort.logout(jwtToken);

            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiSuccessResponse<java.util.Map<String, String>> resp = new ApiSuccessResponse<>("Logout exitoso",
                    "AUTH_LOGOUT_SUCCESS", java.util.Map.of(), tech);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            TechnicalDetails tech = ResponseUtil.buildTechnicalDetails(httpRequest, System.currentTimeMillis() - start,
                    appEnvConfig);
            ApiErrorResponse error = new ApiErrorResponse("Error al cerrar sesión: " + e.getMessage(),
                    "AUTH_LOGOUT_ERROR",
                    java.util.Arrays.asList(new ErrorDetail("logout", e.getMessage())), tech);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

}
