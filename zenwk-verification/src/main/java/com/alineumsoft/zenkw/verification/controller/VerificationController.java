package com.alineumsoft.zenkw.verification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alineumsoft.zenkw.verification.dto.TokenDTO;
import com.alineumsoft.zenkw.verification.service.VerificationService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * <p>
 * Controlador para la gestión de tokens de verificación de usuario. Permite
 * enviar y validar códigos de verificación vía correo electrónico.
 * </p>
 * 
 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
 * @project verification-zenwk
 * @class EmailController
 */
@RestController
@RequestMapping("/api/verification/token")
@Slf4j
@RequiredArgsConstructor
public class VerificationController {

	/**
	 * Email producer service
	 */
	private final VerificationService verificationService;

	/**
	 * <p>
	 * <b> CU003_Gestionar token de verificación. </b> Envía un token de
	 * verificación a la cuenta de correo del usuario.
	 * </p>
	 * 
	 * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
	 * @param request
	 * @param dto
	 * @param userDetails
	 * @return
	 */
	@PostMapping("/send")
	public ResponseEntity<Void> sendToken(HttpServletRequest request, @Validated @RequestBody TokenDTO dto) {
		// Enviar solicitud de correo a la cola de RabbitMQ
		verificationService.sendToken(dto, request);
		return ResponseEntity.noContent().build();
	}

	/**
	 * <p>
	 * <b> CU003_Gestionar token de verificación. </b> Valida un código de
	 * verificación generado previamente.
	 * </p>
	 * 
	 * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
	 * @param request
	 * @param code
	 * @param dto
	 * @param userDetails
	 * @return
	 */
	@PostMapping("/validate/{code}")
	public ResponseEntity<Boolean> verifyToken(HttpServletRequest request, @PathVariable String code,
			@Validated @RequestBody TokenDTO dto) {
		return ResponseEntity.ok(verificationService.verifyToken(code, dto, request));
	}
}
