package com.alineumsoft.zenkw.verification;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import com.alineumsoft.zenkw.verification.common.dto.TokenDTO;
import com.alineumsoft.zenkw.verification.common.exception.FunctionalException;
import com.alineumsoft.zenkw.verification.common.service.CsrfTokenCommonService;
import com.alineumsoft.zenkw.verification.entity.LogSecurity;
import com.alineumsoft.zenkw.verification.repository.LogSecurityRepository;
import com.alineumsoft.zenkw.verification.service.CsrfTokenService;
import com.alineumsoft.zenkw.verification.service.UserUtilService;
import jakarta.servlet.http.HttpServletRequest;

class CsrfTokenServiceTest {

  @Mock
  private LogSecurityRepository logSecurityUserRepo;

  @Mock
  private UserUtilService userUtilService;

  @Mock
  private CsrfTokenCommonService csrfTokenCommonService;

  @Mock
  private HttpServletRequest request;

  @Spy
  @InjectMocks
  private CsrfTokenService csrfTokenService;

  private TokenDTO tokenDTO;
  private LogSecurity logSecurity;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    tokenDTO = new TokenDTO();
    tokenDTO.setEmail("user@test.com");
    logSecurity = new LogSecurity();
  }

  // @Test
  // @DisplayName("Debe generar correctamente un token CSRF y registrar el log de éxito")
  // void testGenerateCsrfToken_Success() {
  // when(userUtilService.getNameUserFromEmail(anyString())).thenReturn("testUser");
  // when(csrfTokenService.initializeLog(any(), anyString(), anyString(), anyString(), anyString()))
  // .thenReturn(logSecurity);
  // when(csrfTokenCommonService.generateCsrfToken(any(), any(), anyString())).thenReturn(tokenDTO);
  //
  // TokenDTO result = csrfTokenService.generateCsrfToken(tokenDTO, request);
  //
  // assertNotNull(result);
  // assertEquals("user@test.com", result.getEmail());
  // verify(csrfTokenCommonService, times(1)).generateCsrfToken(any(), any(), anyString());
  // verify(csrfTokenService, times(1)).saveSuccessLog(eq(HttpStatus.OK.value()), eq(logSecurity),
  // eq(logSecurityUserRepo));
  // }

  @Test
  @DisplayName("Debe lanzar FunctionalException si ocurre un error inesperado al generar el token CSRF")
  void testGenerateCsrfToken_ThrowsFunctionalException() {
    when(userUtilService.getNameUserFromEmail(anyString())).thenReturn("testUser");
    when(csrfTokenService.initializeLog(any(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(logSecurity);
    when(csrfTokenCommonService.generateCsrfToken(any(), any(), anyString()))
        .thenThrow(new RuntimeException("Error al generar token"));

    FunctionalException ex = assertThrows(FunctionalException.class,
        () -> csrfTokenService.generateCsrfToken(tokenDTO, request));

    assertTrue(ex.getMessage().contains("Error al generar token"));
    verify(csrfTokenService, times(1)).setLogSecurityError(any(RuntimeException.class),
        eq(logSecurity));
  }

  @Test
  @DisplayName("Debe validar correctamente un token CSRF y registrar el log de éxito")
  void testValidateCsrfToken_Success() {
    when(userUtilService.getNameUserFromEmail(anyString())).thenReturn("testUser");
    when(csrfTokenService.initializeLog(any(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(logSecurity);

    boolean result = csrfTokenService.validateCsrfToken(tokenDTO, request);

    assertTrue(result);
    verify(csrfTokenCommonService, times(1)).validateCsrfToken(eq(tokenDTO));
    verify(csrfTokenService, times(1)).saveSuccessLog(eq(HttpStatus.OK.value()), eq(logSecurity),
        eq(logSecurityUserRepo));
  }

  @Test
  @DisplayName("Debe lanzar FunctionalException si ocurre un error inesperado al validar el token CSRF")
  void testValidateCsrfToken_ThrowsFunctionalException() {
    when(userUtilService.getNameUserFromEmail(anyString())).thenReturn("testUser");
    when(csrfTokenService.initializeLog(any(), anyString(), anyString(), anyString(), anyString()))
        .thenReturn(logSecurity);
    doThrow(new RuntimeException("Error al validar token")).when(csrfTokenCommonService)
        .validateCsrfToken(any());

    FunctionalException ex = assertThrows(FunctionalException.class,
        () -> csrfTokenService.validateCsrfToken(tokenDTO, request));

    assertTrue(ex.getMessage().contains("Error al validar token"));
    verify(csrfTokenService, times(1)).setLogSecurityError(any(RuntimeException.class),
        eq(logSecurity));
  }
}
