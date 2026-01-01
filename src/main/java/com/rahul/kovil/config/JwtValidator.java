package com.rahul.kovil.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rahul.kovil.common.response.ErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtValidator extends OncePerRequestFilter {

	private final String header;
	private final JwtProvider jwtProvider;

	public JwtValidator(JwtProperties jwtProperties, JwtProvider jwtProvider) {
		this.header = jwtProperties.getJWT_HEADER();
		this.jwtProvider = jwtProvider;
	}
	private static final List<String> EXCLUDED_PATHS = List.of(
		    "/web/",
		    "/assets/",
		    "/dynamic/",
		    "/auth/"
		);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

	    String path = request.getServletPath();
	    for (String exclude : EXCLUDED_PATHS) {
	        if (path.startsWith(exclude)) {
	            filterChain.doFilter(request, response);
	            return;
	        }
	    }

		String token = request.getHeader(header);
		if (token != null && token.startsWith("Bearer ")) {
			token = token.substring(7);
		}
		try {
			if (token != null) {
				String email = jwtProvider.getEmailFromJwtToken(token);
				List<GrantedAuthority> authorities = new ArrayList<>();
				Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			}else {
				throw new RuntimeException();
			}
			filterChain.doFilter(request, response);
		} catch (Exception ex) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			String json = String.format("""
					    {
					        "timestamp": "%s",
					        "status": 401,
					        "error": "Unauthorized",
					        "message": "%s",
					        "path": "%s"
					    }
					""", LocalDateTime.now(), ex.getMessage(), request.getRequestURI());

			response.getWriter().write(json);

		}

	}
}
