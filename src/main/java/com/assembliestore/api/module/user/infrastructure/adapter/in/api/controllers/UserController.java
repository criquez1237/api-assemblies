package com.assembliestore.api.module.user.infrastructure.adapter.in.api.controllers;

import java.util.Collections;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.assembliestore.api.common.interfaces.SuccessfulResponse;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.SignInRequest;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.SignInResponse;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.SignUpRequest;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.UpdatePasswordRequest;
import com.assembliestore.api.module.user.infrastructure.adapter.dto.UpdateUserRequest;
import com.assembliestore.api.module.user.application.services.UserService;
import com.assembliestore.api.module.user.domain.entities.User;
import com.assembliestore.api.module.user.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController()
@RequestMapping("/users")
public class UserController {

	private final com.assembliestore.api.module.user.application.services.UserService _userService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserController(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {

		this._userService = userService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@GetMapping("/{id}")
	public ResponseEntity<User> getUserById(@PathVariable String id) {

		Optional<User> user = Optional.ofNullable(_userService.getById(id));

		return user.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PostMapping("signup")
	public ResponseEntity<SuccessfulResponse> signup(@RequestBody SignUpRequest request) {

		/*_userService.signup(request);

		SuccessfulResponse response = new SuccessfulResponse("Usuario registrado correctamente");*/

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody UpdateUserRequest request) {
		User updatedUser = _userService.updateUser(id, request);
		return ResponseEntity.ok(updatedUser);
	}

	@GetMapping("/me")
	public ResponseEntity<?> getCurrentUser(Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated()) {
			return ResponseEntity.ok("Usuario autenticado: " + authentication.getName());
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
		}
	}

	@PostMapping("/signin")
	public ResponseEntity<?> signin(@RequestBody SignInRequest request) {
		Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
		if (userOpt.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		}
		User user = userOpt.get();
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		}
		if (!user.isActived()) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Account is inactive");
		}
		// Autenticar en el contexto de Spring Security
		UsernamePasswordAuthenticationToken authToken =
			new UsernamePasswordAuthenticationToken(user.getEmail(), null, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(authToken);
		SignInResponse response = new SignInResponse();
		response.setUserId(user.getId());
		response.setUserName(user.getUserName());
		response.setEmail(user.getEmail());
		response.setRole(user.getRole() != null ? user.getRole().name() : null);
		if (user.getPerfil() != null) {
			response.setNames(user.getPerfil().getNames());
			response.setSurnames(user.getPerfil().getSurnames());
			response.setImagePerfil(user.getPerfil().getImagePerfil());
		}
		return ResponseEntity.ok(response);
	}

	/*@PutMapping("/password")
	public ResponseEntity<?> updatePassword(
			@AuthenticationPrincipal org.springframework.security.core.userdetails.User authUser,
			@RequestBody UpdatePasswordRequest request) {
		if (authUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
		}
		_userService.updatePassword(authUser.getUsername(), request);
		return ResponseEntity.ok("Contraseña actualizada correctamente");
	}*/

	@GetMapping
	public ResponseEntity<Iterable<User>> getAllUsers() {
		Iterable<User> users = _userService.getAllUsers();
		return ResponseEntity.ok(users);
	}

	@DeleteMapping("/all")
	public ResponseEntity<?> deleteAllUsers(@RequestParam(required = false) String confirm) {
		// Solo ejecuta si se pasa ?confirm=YES para evitar borrados accidentales
		if (!"YES".equals(confirm)) {
			return ResponseEntity.status(400).body("Debes confirmar con ?confirm=YES");
		}
		_userService.deleteAllUsers();
		return ResponseEntity.ok("Todos los usuarios han sido eliminados.");
	}
}
