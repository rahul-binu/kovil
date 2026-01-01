package com.rahul.kovil.config;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

	private final String provider;
	private final SecretKey key;

	public JwtProvider(JwtProperties jwtProperties) {
		this.provider = jwtProperties.getPROVIDER();
		this.key = Keys.hmacShaKeyFor(jwtProperties.getSECRET_KEY().getBytes());
	}

	public String generateToken(Map<String, Object> claims) {
		return Jwts.builder().issuer("my-app").claims(claims).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + 86_400_000)).signWith(key).compact();
	}

	private Claims parseClaims(String jwt) {
		if (jwt.startsWith("Bearer ")) {
			jwt = jwt.substring(7);
		}

		return Jwts.parser().verifyWith(key) // key = SecretKey or PublicKey
				.build().parseSignedClaims(jwt).getPayload();
	}

	public String getEmailFromJwtToken(String jwt) {
		if (jwt.startsWith("Bearer ")) {
			jwt = jwt.substring(7);
		}

		Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(jwt).getPayload();

		return claims.get("email", String.class);
	}

	public String getTenantId(String token) {
		Claims claims = parseClaims(token);
		return claims.get("tenantId", String.class);
	}

	public String getUserId(String token) {
		Claims claims = parseClaims(token);
		return claims.get("userId", String.class);
	}

}
