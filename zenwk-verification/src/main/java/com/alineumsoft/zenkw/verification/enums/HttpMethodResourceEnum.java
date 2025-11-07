package com.alineumsoft.zenkw.verification.enums;

import org.springframework.http.HttpMethod;

import lombok.Getter;

/**
 * <p>
 * Enum que define los recursos expuestos y sus metodos http para el api de
 * seguridad a ser usado en RBAC definido.
 * </p>
 * 
 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
 * @project verification-zenwk
 * @class HttpMethodResourceEnum
 */
@Getter
public enum HttpMethodResourceEnum {
	/**
	 * Verification
	 */
	VERIFICATION_TOKEN(HttpMethod.POST, "/api/verification/**");

	/**
	 * Metodo http
	 */
	private final HttpMethod method;
	/**
	 * recurso
	 */
	private final String resource;

	/**
	 * <p>
	 * <b> Constructor </b>
	 * </p>
	 * 
	 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
	 * @param method
	 * @param resource
	 */
	HttpMethodResourceEnum(HttpMethod method, String resource) {
		this.method = method;
		this.resource = resource;
	}
}
