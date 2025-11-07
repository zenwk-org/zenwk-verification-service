package com.alineumsoft.zenkw.verification.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.alineumsoft.zenkw.verification.common.dto.TokenDTO;
import com.alineumsoft.zenkw.verification.common.exception.FunctionalException;
import com.alineumsoft.zenkw.verification.common.helper.ApiRestSecurityHelper;
import com.alineumsoft.zenkw.verification.common.util.CodeGenerator;
import com.alineumsoft.zenkw.verification.constants.Constants;
import com.alineumsoft.zenkw.verification.dto.EmailRequestDTO;
import com.alineumsoft.zenkw.verification.entity.LogSecurity;
import com.alineumsoft.zenkw.verification.entity.Token;
import com.alineumsoft.zenkw.verification.enums.MessagesVerificationEnum;
import com.alineumsoft.zenkw.verification.enums.SecurityActionEnum;
import com.alineumsoft.zenkw.verification.enums.VerificationExceptionEnum;
import com.alineumsoft.zenkw.verification.repository.LogSecurityRepository;
import com.alineumsoft.zenkw.verification.repository.TokenRepository;
import com.alineumsoft.zenkw.verification.util.CryptoUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * Servicio para el api encargado de la gestión del token de verificación de usuario.
 * </p>
 * 
 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
 * @project verification-zenwk
 * @class VerificationService
 */
@Service
@RequiredArgsConstructor
public class VerificationService extends ApiRestSecurityHelper {
  /**
   * Repositorio para log persistible de modulo
   */
  private final LogSecurityRepository logSecurityUserRepo;
  /**
   * tokenRepository
   */
  private final TokenRepository tokenRepository;
  /**
   * rabbitTemplate
   */
  private final AmqpTemplate rabbitTemplate;
  /**
   * Util user service
   */
  private final UserUtilService userUtilService;



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
  public TokenDTO sendToken(TokenDTO dto, HttpServletRequest request) {
    String username = dto.getEmail();
    LogSecurity logSecurity = initializeLog(request, username, getJson(dto), notBody,
        SecurityActionEnum.VERIFICATION_SEND_TOKEN.getCode());

    try {
      Token token = tokenRepository.findByEmail(dto.getEmail()).orElse(null);
      // Se elimina token si este ya existe
      if (token != null) {
        tokenRepository.delete(token);
      }
      TokenDTO tokenDTO = generateTokenDTO(dto.getEmail());
      // Se guarda el token o se actualiza. Un usuario siempre tendra un token asociado
      // Si aumenta el alcance, gestionar mas de un token activos al mismo tiempo.
      tokenRepository.save(new Token(tokenDTO, username));

      EmailRequestDTO emailDTO =
          generateTemplateRegisterFlow(tokenDTO.getEmail(), tokenDTO.getCode(), username);

      rabbitTemplate.convertAndSend(Constants.RABBITH_EMAIL_QUEUE, emailDTO);
      saveSuccessLog(HttpStatus.OK.value(), logSecurity, logSecurityUserRepo);
      return tokenDTO;
    } catch (RuntimeException e) {
      setLogSecurityError(e, logSecurity);
      throw new FunctionalException(e.getMessage(), e.getCause(), logSecurityUserRepo,
          logSecurity);
    }

  }

  /**
   * <p>
   * <b> CU003_Gestionar token de verificación </b> Genera el dto con los datos del token
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param email
   * @return
   */
  private TokenDTO generateTokenDTO(String email) {
    TokenDTO dto = new TokenDTO();
    dto.setEmail(email);
    dto.setCode(CodeGenerator.generateCode(Constants.TOKEN_CODE_ZISE));
    dto.setHashCode(CryptoUtil.encryptCode(dto.getCode()));
    dto.setUuid(CodeGenerator.generateUUID());
    dto.setHashUuid(CryptoUtil.encryptCode(dto.getUuid()));
    dto.setExpirationDate(LocalDateTime.now().plusMinutes(Constants.TOKEN_CODE_MINUTES));
    return dto;
  }

  /**
   * <p>
   * <b> CU003_Gestionar token de verificación </b> Genera lo datos para el email con el código de
   * verificación.
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param email
   * @param code
   * @param username
   * @return
   */
  private EmailRequestDTO generateTemplateRegisterFlow(String email, String code, String username) {
    EmailRequestDTO dto = new EmailRequestDTO();
    Map<String, Object> metadata = new HashMap<>();
    // Metadatos para la plantilla.
    metadata.put(Constants.REGISTER_TEMPLATE_EMAIL_NAME_CODE, code);
    metadata.put(Constants.TEMPLATE_EMAIL_NAME_USERNAME, username != null ? username : email);
    metadata.put(Constants.TEMPLATE_EMAIL_NAME_CORPORATION, Constants.ZENWK);
    // Plantilla html
    dto.setTemplateName(Constants.RABBIT_REGISTER_USER_TEMPLATE);
    dto.setSubject(MessagesVerificationEnum.TEMPLATE_REGISTER_FLOW_EMAIL_SUBJECT.getMessage(code));
    // Dto para enviar a cola de rabbithMq.
    prepareEmailSubject(email, dto, metadata);
    return dto;
  }

  /**
   * 
   * <p>
   * <b> CU001_CU003_Gestionar token de verificación </b> Setea los datos de la planilla que se
   * notificará por correo.
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param email
   * @param dto
   * @param metadata
   */
  private void prepareEmailSubject(String email, EmailRequestDTO dto,
      Map<String, Object> metadata) {
    dto.setTo(email);
    dto.setVariables(metadata);

  }

  /**
   * <p>
   * <b> CU003_Gestionar token de verificación. </b> valida un código de verificación generado
   * previamente.
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param dto
   * @param userDetails
   * @return
   */
  public boolean verifyToken(TokenDTO dto, HttpServletRequest request) {
    String username = dto.getEmail();
    LogSecurity logSecurity = initializeLog(request, username, getJson(dto),
        Boolean.class.getName(), SecurityActionEnum.VERIFICATION_VALIDATE_TOKEN.getCode());
    try {
      Token token = getToken(dto.getEmail());
      String messageError = validateToken(dto, token);

      if (!messageError.isEmpty()) {
        throw new IllegalArgumentException(messageError);
      }
      saveSuccessLog(HttpStatus.OK.value(), logSecurity, logSecurityUserRepo);
      return true;
    } catch (RuntimeException e) {
      setLogSecurityError(e, logSecurity);
      throw new FunctionalException(e.getMessage(), e.getCause(), logSecurityUserRepo,
          logSecurity);
    }

  }


  /**
   * 
   * <p>
   * <b> CU003_Gestionar token de verificación. </b> Validacion del token
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param dto
   * @param token
   * @return
   */
  private String validateToken(TokenDTO dto, Token token) {
    String messageError = "";
    if (!token.getEmail().equals(dto.getEmail())) {
      messageError = VerificationExceptionEnum.FUNC_VERIFICATION_EMAIL_NOT_MATCH.getCodeMessage();
    } else if (!CryptoUtil.matchesCode(dto.getUuid(), token.getUuid())) {
      messageError = VerificationExceptionEnum.FUNC_VERIFICATION_UUID_NOT_MATCH.getCodeMessage();
    } else if (token.getExpirationDate().isBefore(LocalDateTime.now())) {
      messageError = VerificationExceptionEnum.FUNC_VERIFICATION_TOKEN_EXPIRATION.getCodeMessage();
    } else if (dto.getCode() != null && !dto.getCode().isEmpty()
        && !CryptoUtil.matchesCode(dto.getCode(), token.getCode())) {
      messageError =
          VerificationExceptionEnum.FUNC_VERIFICATION_CODE_TOKEN_NOT_MATCH.getCodeMessage();
    }
    return messageError;
  }

  /**
   * <p>
   * <b>CU003_Gestionar token de verificación. </b> Recupera el token con todos sus datos
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param email
   * @return
   */
  private Token getToken(String email) {
    return tokenRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException(
        VerificationExceptionEnum.FUNC_VERIFICATION_TOKEN_NOT_FOUND.getCodeMessage()));

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
   * @param dto
   * @return
   */
  public boolean resetPassword(HttpServletRequest request, TokenDTO dto) {
    String username = userUtilService.getNameUserFromEmail(dto.getEmail());
    LogSecurity logSecurity = initializeLog(request, username, notBody, Boolean.class.getName(),
        SecurityActionEnum.VERIFICATION_SEND_TOKEN.getCode());
    String urlParam = null;
    String url = null;
    try {
      if (dto.getPathUrl() == null || dto.getPathUrl().isEmpty()) {
        throw new IllegalArgumentException(
            VerificationExceptionEnum.FUNC_VERIFICATION_URL_PATH_NOT_NULL.getCodeMessage());
      }
      Token token = tokenRepository.findByEmail(dto.getEmail())
          .orElseThrow(() -> new EntityNotFoundException());

      // Gestión del token
      TokenDTO tokenDTO = generateTokenDTO(dto.getEmail());
      tokenRepository.delete(token);
      tokenRepository.save(new Token(tokenDTO, username));

      urlParam = "?email=".concat(tokenDTO.getEmail()).concat("&uuid=").concat(tokenDTO.getUuid())
          .concat("&code=").concat(tokenDTO.getCode());
      url = dto.getPathUrl().concat(urlParam);

      EmailRequestDTO emailDTO =
          generateTemplateToResetPassword(tokenDTO.getEmail(), url, username);

      rabbitTemplate.convertAndSend(Constants.RABBITH_EMAIL_QUEUE, emailDTO);
      saveSuccessLog(HttpStatus.OK.value(), logSecurity, logSecurityUserRepo);
      return true;
    } catch (RuntimeException e) {
      setLogSecurityError(e, logSecurity);
      throw new FunctionalException(e.getMessage(), e.getCause(), logSecurityUserRepo,
          logSecurity);

    }
  }



  /**
   * <p>
   * <b> CU004_Restablecer contraseña </b> Genera lo datos para el email con el enlace para
   * restablecer la contraseña
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param email
   * @param code
   * @param username
   * @return
   */
  private EmailRequestDTO generateTemplateToResetPassword(String email, String url,
      String username) {
    EmailRequestDTO dto = new EmailRequestDTO();
    Map<String, Object> metadata = new HashMap<>();
    // Metadatos para la plantilla.
    metadata.put(Constants.TEMPLATE_EMAIL_NAME_USERNAME, username != null ? username : email);
    metadata.put(Constants.TEMPLATE_EMAIL_NAME_CORPORATION, Constants.ZENWK);
    metadata.put(Constants.RESET_PASSWORD_TEMPLATE_EMAIL_RESET_URL, url);
    // Plantilla html
    dto.setTemplateName(Constants.RABBIT_RESET_PASSWORD_TEMPLATE);
    dto.setSubject(MessagesVerificationEnum.TEMPLATE_RESET_PASSWORD_EMAIL_SUBJECT.getMessage());
    prepareEmailSubject(email, dto, metadata);
    return dto;
  }

}
