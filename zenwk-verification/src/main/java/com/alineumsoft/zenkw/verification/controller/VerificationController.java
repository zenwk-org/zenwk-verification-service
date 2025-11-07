package com.alineumsoft.zenkw.verification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alineumsoft.zenkw.verification.common.dto.TokenDTO;
import com.alineumsoft.zenkw.verification.service.VerificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * <p>
 * Controlador para la gestión de tokens de verificación de usuario. Permite enviar y validar
 * códigos de verificación vía correo electrónico.
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
@CrossOrigin(origins = "${cors.allowed-origins}", allowCredentials = "true")
public class VerificationController {

  /**
   * Email producer service
   */
  private final VerificationService verificationService;

  /**
   * <p>
   * <b> CU003_Gestionar token de verificación. </b> Envía un token de verificación a la cuenta de
   * correo del usuario.
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param request
   * @param dto
   * @param userDetails
   * @return
   */
  @PostMapping("/send")
  public ResponseEntity<TokenDTO> sendToken(HttpServletRequest request,
      @Validated @RequestBody TokenDTO dto) {
    // Enviar solicitud de correo a la cola de RabbitMQ
    return ResponseEntity.ok(verificationService.sendToken(dto, request));
  }

  /**
   * <p>
   * <b> CU003_Gestionar token de verificación. </b> Valida un código de verificación generado
   * previamente.
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param request
   * @param dto
   * @param userDetails
   * @return
   */
  @PostMapping("/validate")
  public ResponseEntity<Boolean> verifyToken(HttpServletRequest request,
      @Validated @RequestBody TokenDTO dto) {
    return ResponseEntity.ok(verificationService.verifyToken(dto, request));
  }

  /**
   * 
   * <p>
   * <b> CU004_Restablecer contraseña </b> Envía la notificación al correo del usuario con link de
   * acceso para el cambio de contraseña.
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param request
   * @param email
   * @return
   */
  @PostMapping("/reset-password")
  public ResponseEntity<Boolean> sendResetPassword(HttpServletRequest request,
      @Validated @RequestBody TokenDTO dto) {
    return ResponseEntity.ok(verificationService.resetPassword(request, dto));

  }
}
