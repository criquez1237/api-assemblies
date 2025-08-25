package com.assembliestore.api.module.user.infrastructure.config;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.assembliestore.api.module.user.application.dto.JwtTokenDto;
import com.assembliestore.api.module.user.application.dto.UserDto;
import com.assembliestore.api.module.user.application.port.TokenPort;
import com.assembliestore.api.module.user.application.port.UserPort;
import com.google.firebase.internal.NonNull;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	// private final JwtService _jwtService;
	private final UserDetailsService _userDetailsService;
	private final TokenPort _tokenPort;
	private final UserPort _userPort;

	@Override
	protected void doFilterInternal(
			@NonNull HttpServletRequest request,
			@NonNull HttpServletResponse response,
			@NonNull FilterChain filterChain)

			throws ServletException, IOException {

		if (request.getServletPath().contains("/auth")) {

			filterChain.doFilter(request, response);

			return;
		}

		final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {

			filterChain.doFilter(request, response);
			return;
		}

		final String jwtToken = authHeader.substring(7);
		final String userName = _tokenPort.getUserNameFromToken(jwtToken);
		if (userName == null || SecurityContextHolder.getContext().getAuthentication() != null) {

			filterChain.doFilter(request, response);
			return;
		}

		final JwtTokenDto jwtTokenDto = _tokenPort.findByToken(jwtToken);

		if (jwtTokenDto == null) {

			filterChain.doFilter(request, response);

			return;
		}

		final UserDetails details = this._userDetailsService.loadUserByUsername(userName);

		final UserDto user = this._userPort.findByEmail(userName);

		if (user == null) {

			filterChain.doFilter(request, response);

			return;
		}


		final boolean isTokenValid = this._tokenPort.validateToken(jwtToken, user);

		if(!isTokenValid){

			filterChain.doFilter(request, response);
			return;
		}

		final var authToken = new UsernamePasswordAuthenticationToken(
			details,
			null,
			details.getAuthorities()
		);

		authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

		SecurityContextHolder.getContext().setAuthentication(authToken);

		filterChain.doFilter(request, response);


	}

	/*
	 * @Override
	 * protected void doFilterInternal(
	 * 
	 * @NonNull HttpServletRequest request,
	 * 
	 * @NonNull HttpServletResponse response,
	 * 
	 * @NonNull FilterChain filterChain)
	 * 
	 * throws ServletException, IOException {
	 * 
	 * final String authHeader = request.getHeader("Authorization");
	 * final String jwt;
	 * 
	 * if (authHeader == null || !authHeader.startsWith("Bearer ")) {
	 * 
	 * filterChain.doFilter(request, response);
	 * 
	 * return;
	 * }
	 * 
	 * jwt = authHeader.substring(7);
	 * 
	 * //userEmail = _jwtService.extractuserName(jwt);
	 * }
	 */
}