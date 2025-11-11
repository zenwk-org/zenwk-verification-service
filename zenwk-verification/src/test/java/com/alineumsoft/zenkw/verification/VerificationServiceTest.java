package com.alineumsoft.zenkw.verification;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.core.AmqpTemplate;
import com.alineumsoft.zenkw.verification.common.dto.TokenDTO;
import com.alineumsoft.zenkw.verification.common.exception.FunctionalException;
import com.alineumsoft.zenkw.verification.constants.Constants;
import com.alineumsoft.zenkw.verification.dto.EmailRequestDTO;
import com.alineumsoft.zenkw.verification.entity.Token;
import com.alineumsoft.zenkw.verification.repository.LogSecurityRepository;
import com.alineumsoft.zenkw.verification.repository.TokenRepository;
import com.alineumsoft.zenkw.verification.service.UserUtilService;
import com.alineumsoft.zenkw.verification.service.VerificationService;
import com.alineumsoft.zenkw.verification.util.CryptoUtil;
import jakarta.servlet.http.HttpServletRequest;

class VerificationServiceTest {
  @Mock
  private LogSecurityRepository logRepo;
  @Mock
  private TokenRepository tokenRepository;
  @Mock
  private AmqpTemplate rabbitTemplate;
  @Mock
  private UserUtilService userUtilService;
  @Mock
  private HttpServletRequest request;

  @InjectMocks
  private VerificationService service;

  @BeforeEach
  void setUp() {
    request = Mockito.mock(HttpServletRequest.class);

    MockitoAnnotations.openMocks(this);
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost/api/test"));
    when(request.getMethod()).thenReturn("POST");
    when(request.getHeader("User-Agent")).thenReturn("JUnit-Test-Agent");
    when(request.getHeader("X-Forwarded-For")).thenReturn("127.0.0.1");
    when(request.getRemoteAddr()).thenReturn("127.0.0.1");
  }

  /**
   * <p>
   * sendToken()
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   */
  @Test
  @DisplayName("Debe generar, guardar y enviar correctamente un token de verificación al correo del usuario")
  void testSendTokenSuccess() {
    TokenDTO input = new TokenDTO();
    input.setEmail("test@example.com");

    when(tokenRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    TokenDTO result = service.sendToken(input, request);

    assertNotNull(result.getCode());
    verify(tokenRepository).save(any(Token.class));
    verify(rabbitTemplate).convertAndSend(eq(Constants.RABBITH_EMAIL_QUEUE),
        any(EmailRequestDTO.class));
  }

  @Test
  @DisplayName("Debe lanzar FunctionalException si ocurre un error inesperado al enviar el token de verificación")
  void testSendTokenThrowsFunctionalException() {
    TokenDTO input = new TokenDTO();
    input.setEmail("test@example.com");
    when(tokenRepository.findByEmail("test@example.com"))
        .thenThrow(new RuntimeException("DB error"));

    assertThrows(FunctionalException.class, () -> service.sendToken(input, request));
  }


  /**
   * 
   * <p>
   * verifyToken()
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   */
  @Test
  @DisplayName("Debe validar correctamente un token de verificación válido y vigente")
  void testVerifyTokenSuccess() {
    TokenDTO dto = new TokenDTO();
    dto.setEmail("user@test.com");
    dto.setCode("12345");
    dto.setUuid("uuid-test");

    Token token = new Token();
    token.setEmail("user@test.com");
    token.setCode(CryptoUtil.encryptCode(dto.getCode()));
    token.setUuid(CryptoUtil.encryptCode(dto.getUuid()));
    token.setExpirationDate(LocalDateTime.now().plusMinutes(10));

    when(tokenRepository.findByEmail("user@test.com")).thenReturn(Optional.of(token));

    boolean result = service.verifyToken(dto, request);

    assertTrue(result);
  }

  @Test
  @DisplayName("Debe lanzar FunctionalException si el token de verificación no existe para el correo indicado")
  void testVerifyTokenTokenNotFound() {
    TokenDTO dto = new TokenDTO();
    dto.setEmail("notfound@test.com");

    when(tokenRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
    assertThrows(FunctionalException.class, () -> service.verifyToken(dto, request));
  }

  @Test
  @DisplayName("Debe lanzar FunctionalException si el token de verificación está expirado")
  void testVerifyTokenExpired() {
    TokenDTO dto = new TokenDTO();
    dto.setEmail("expired@test.com");
    dto.setCode("12345");
    dto.setUuid("uuid");

    Token token = new Token();
    token.setEmail("expired@test.com");
    token.setCode("12345");
    token.setUuid("uuid");
    token.setExpirationDate(LocalDateTime.now().minusMinutes(1));

    when(tokenRepository.findByEmail("expired@test.com")).thenReturn(Optional.of(token));

    assertThrows(FunctionalException.class, () -> service.verifyToken(dto, request));
  }

  /**
   * 
   * <p>
   * resetPassword()
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   */
  @Test
  @DisplayName("Debe generar un nuevo token y enviar correo de restablecimiento de contraseña con URL válida")
  void testResetPasswordSuccess() {
    TokenDTO dto = new TokenDTO();
    dto.setEmail("user@test.com");
    dto.setPathUrl("https://zenwk.com/reset");

    Token token = new Token();
    token.setEmail("user@test.com");
    when(userUtilService.getNameUserFromEmail(dto.getEmail())).thenReturn("user");
    when(tokenRepository.findByEmail("user@test.com")).thenReturn(Optional.of(token));

    boolean result = service.resetPassword(request, dto);

    assertTrue(result);
    verify(rabbitTemplate).convertAndSend(eq(Constants.RABBITH_EMAIL_QUEUE),
        any(EmailRequestDTO.class));
    verify(tokenRepository, times(1)).save(any(Token.class));
  }

  @Test
  @DisplayName("Debe lanzar FunctionalException si la URL para restablecer contraseña es nula o vacía")
  void testResetPasswordInvalidUrl() {
    TokenDTO dto = new TokenDTO();
    dto.setEmail("user@test.com");
    dto.setPathUrl("");

    when(userUtilService.getNameUserFromEmail(dto.getEmail())).thenReturn("user");

    assertThrows(FunctionalException.class, () -> service.resetPassword(request, dto));
  }

  @Test
  @DisplayName("Debe lanzar FunctionalException si ocurre un error inesperado durante el proceso de restablecimiento")
  void testResetPasswordUnexpectedError() {
    TokenDTO dto = new TokenDTO();
    dto.setEmail("user@test.com");
    dto.setPathUrl("https://zenwk.com/reset");

    when(userUtilService.getNameUserFromEmail(dto.getEmail())).thenReturn("user");
    when(tokenRepository.findByEmail("user@test.com")).thenThrow(new RuntimeException("DB error"));

    assertThrows(FunctionalException.class, () -> service.resetPassword(request, dto));
  }
}
