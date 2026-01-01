package com.rahul.kovil.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProperties {

	@Value("${app.constants.jwt.provider}")
	protected String PROVIDER;

	@Value("${app.constants.jwt.secretkey}")
	protected String SECRET_KEY;
	

	@Value("${app.constants.jwt.header}")
	protected String JWT_HEADER;


	public String getPROVIDER() {
		return PROVIDER;
	}


	public void setPROVIDER(String pROVIDER) {
		PROVIDER = pROVIDER;
	}


	public String getSECRET_KEY() {
		return SECRET_KEY;
	}


	public void setSECRET_KEY(String sECRET_KEY) {
		SECRET_KEY = sECRET_KEY;
	}


	public String getJWT_HEADER() {
		return JWT_HEADER;
	}


	public void setJWT_HEADER(String jWT_HEADER) {
		JWT_HEADER = jWT_HEADER;
	}
}
