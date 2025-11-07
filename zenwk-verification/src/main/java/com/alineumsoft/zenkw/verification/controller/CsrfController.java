package com.alineumsoft.zenkw.verification.controller;

import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alineumsoft.zenkw.verification.common.constants.AuthConfigConstants;
import com.alineumsoft.zenkw.verification.common.dto.TokenDTO;
import com.alineumsoft.zenkw.verification.service.CsrfTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller token CSRF y !!!! Nota: esta api no se va utilizar porque se configura el httpOnly
 * para las solicitudes, se inyectara esta cookie atumáticamente para las apis no publicas, y la
 * validación será automarica por el uso directo de springsecurity en el filtro de validación CSRF
 * !!!!
 *
 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
 * @project zenwk-verification
 * @class CsrfController
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/verification/csrf")
// Pendiente de realizar:
// Para desplegar varios microservicios con CORS, mover esta configuración a un
// starter común (zenwk-security-starter) que exporte:
// @Bean
// public CorsConfigurationSource corsConfigurationSource(Environment env)
@CrossOrigin(origins = "${cors.allowed-origins}", allowCredentials = "true")
@Slf4j
public class CsrfController {

  /**
   * Servicio que gestiona el csrfToken
   */
  private final CsrfTokenService csrfTokenService;


  /**
   * <p>
   * <b> CU00X_Gestionar protección contra ataques CSRF </b> Genera el token csrf
   * </p>
   *
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param dto
   * @param request
   * @return
   */
  @PostMapping("/token")
  public ResponseEntity<Void> getCsrfToken(@Validated @RequestBody TokenDTO inDto,
      HttpServletRequest request, HttpServletResponse response) {
    TokenDTO OutDto = csrfTokenService.generateCsrfToken(inDto, request);

    ResponseCookie csrfCookie =
        ResponseCookie.from(AuthConfigConstants.XCSRF_TOKEN, OutDto.getCode()).httpOnly(true)
            .secure(true).path("/").maxAge(Duration.ofHours(2)).build();

    response.addHeader(HttpHeaders.SET_COOKIE, csrfCookie.toString());
    return ResponseEntity.noContent().build();
  }

  /**
   * <p>
   * <b> CU00X_Gestionar protección contra ataques CSRF </b> Valida el token csrf
   * </p>
   *
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param dto
   * @param request
   * @return
   */
  @PostMapping("/token/validate")
  public ResponseEntity<Boolean> validatCsrfToken(@Validated @RequestBody TokenDTO dto,
      HttpServletRequest request) {
    return ResponseEntity.ok(csrfTokenService.validateCsrfToken(dto, request));
  }

}
