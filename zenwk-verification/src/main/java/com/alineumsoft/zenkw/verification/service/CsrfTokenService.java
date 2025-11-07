package com.alineumsoft.zenkw.verification.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.alineumsoft.zenkw.verification.common.dto.TokenDTO;
import com.alineumsoft.zenkw.verification.common.exception.FunctionalException;
import com.alineumsoft.zenkw.verification.common.helper.ApiRestSecurityHelper;
import com.alineumsoft.zenkw.verification.common.service.CsrfTokenCommonService;
import com.alineumsoft.zenkw.verification.entity.LogSecurity;
import com.alineumsoft.zenkw.verification.enums.SecurityActionEnum;
import com.alineumsoft.zenkw.verification.repository.LogSecurityRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * Service para gestión del token CSRF
 *
 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
 * @project zenwk-verification
 * @class CsrfTokenService
 */
@Service
@RequiredArgsConstructor
public class CsrfTokenService extends ApiRestSecurityHelper {
  /**
   * Repositorio para log persistible de módulo
   */
  private final LogSecurityRepository logSecurityUserRepo;
  /**
   * Util user service
   */
  private final UserUtilService userUtilService;
  /**
   * Servicio common compartido para filtro csrf
   */
  private final CsrfTokenCommonService csrfTokenCommonService;

  /**
   *
   * <p>
   * <b> CU00X_Gestionar protección contra ataques CSRF </b> Genera el token csrf
   * </p>
   *
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param dto
   * @param request
   * @return
   */
  public TokenDTO generateCsrfToken(TokenDTO dto, HttpServletRequest request) {
    String username = getUsername(dto.getEmail());
    LogSecurity logSecurity = initializeLog(request, username, getJson(dto),
        Boolean.class.getName(), SecurityActionEnum.VERIFICATION_SEND_TOKEN_CSRF.getCode());
    try {
      // Genera un nuevo token csrf
      TokenDTO outDTO = csrfTokenCommonService.generateCsrfToken(dto, request, username);
      saveSuccessLog(HttpStatus.OK.value(), logSecurity, logSecurityUserRepo);
      return outDTO;
    } catch (RuntimeException e) {
      setLogSecurityError(e, logSecurity);
      throw new FunctionalException(e.getMessage(), e.getCause(), logSecurityUserRepo, logSecurity);

    }
  }


  /**
   *
   * <p>
   * <b> CU00X_Gestionar protección contra ataques CSRF </b> Valida el token csrf
   * </p>
   *
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param dto
   * @param request
   * @return
   */
  public boolean validateCsrfToken(TokenDTO dto, HttpServletRequest request) {
    String username = getUsername(dto.getEmail());
    LogSecurity logSecurity = initializeLog(request, username, getJson(dto),
        Boolean.class.getName(), SecurityActionEnum.VERIFICATION_VALIDATE_TOKEN_CSRF.getCode());
    try {
      csrfTokenCommonService.validateCsrfToken(dto);
      saveSuccessLog(HttpStatus.OK.value(), logSecurity, logSecurityUserRepo);
      return true;
    } catch (RuntimeException e) {
      setLogSecurityError(e, logSecurity);
      throw new FunctionalException(e.getMessage(), e.getCause(), logSecurityUserRepo, logSecurity);
    }
  }


  /**
   *
   * <p>
   * <b> Util </b> Obtener username
   * </p>
   *
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param email
   * @return
   */
  private String getUsername(String email) {
    return userUtilService.getNameUserFromEmail(email);
  }



}
