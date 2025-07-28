package com.alineumsoft.zenkw.verification.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import com.alineumsoft.zenkw.verification.common.exception.FunctionalException;
import com.alineumsoft.zenkw.verification.common.helper.ApiRestSecurityHelper;
import com.alineumsoft.zenkw.verification.constants.Constants;
import com.alineumsoft.zenkw.verification.dto.EmailRequestDTO;
import com.alineumsoft.zenkw.verification.dto.TokenDTO;
import com.alineumsoft.zenkw.verification.entity.LogSecurity;
import com.alineumsoft.zenkw.verification.entity.Token;
import com.alineumsoft.zenkw.verification.enums.MessagesVerificationEnum;
import com.alineumsoft.zenkw.verification.enums.SecurityActionEnum;
import com.alineumsoft.zenkw.verification.enums.VerificationExceptionEnum;
import com.alineumsoft.zenkw.verification.repository.LogSecurityRepository;
import com.alineumsoft.zenkw.verification.repository.TokenRepository;
import com.alineumsoft.zenkw.verification.util.CodeGenerator;
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
  private final LogSecurityRepository logSecurityUserRespository;
  /**
   * tokenRepository
   */
  private final TokenRepository tokenRepository;
  /**
   * rabbitTemplate
   */
  private final AmqpTemplate rabbitTemplate;

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
  public TokenDTO sendToken(@Validated TokenDTO dto, HttpServletRequest request) {
    String username = dto.getEmail();
    LogSecurity logSecurity = initializeLog(request, username, getJson(dto), notBody,
        SecurityActionEnum.VERIFICATION_SEND_TOKEN.getCode());
    String email = dto.getEmail();
    try {
      String code = CodeGenerator.generateCode(Constants.TOKEN_CODE_ZISE);
      String uuid = CodeGenerator.generateUUID();
      Token token = tokenRepository.findByEmail(email).orElse(null);

      if (token == null) {
        token = new Token(CryptoUtil.encryptCode(code), dto.getEmail(), username,
            CryptoUtil.encryptCode(uuid));
      } else {
        token.setCode(CryptoUtil.encryptCode(code));
        token.setUuid(CryptoUtil.encryptCode(uuid));
      }

      token.setExpirationDate(LocalDateTime.now().plusMinutes(Constants.TOKEN_CODE_MINUTES));
      // Se guarda el token o se actualiza. Un usuario siempre tendra un token asociado
      // Si aumenta el alcance, gestionar mas de un token activos al mismo tiempo.
      TokenDTO tokenDto = new TokenDTO(tokenRepository.save(token));

      EmailRequestDTO emailDTO = generateEmailToToken(email, code, username);
      rabbitTemplate.convertAndSend(Constants.RABBITH_EMAIL_QUEUE, emailDTO);
      saveSuccessLog(HttpStatus.OK.value(), logSecurity, logSecurityUserRespository);
      return tokenDto;
    } catch (RuntimeException e) {
      setLogSecurityError(e, logSecurity);
      throw new FunctionalException(e.getMessage(), e.getCause(), logSecurityUserRespository,
          logSecurity);
    }

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
  private EmailRequestDTO generateEmailToToken(String email, String code, String username) {
    EmailRequestDTO dto = new EmailRequestDTO();
    Map<String, Object> metadata = new HashMap<>();
    // Metadatos para la plantilla.
    metadata.put(Constants.TOKEN_TEMPLATE_EMAIL_NAME_CODE, code);
    metadata.put(Constants.TOKEN_TEMPLATE_EMAIL_NAME_USERNAME, username != null ? username : email);
    metadata.put(Constants.TOKEN_EMPLATE_EMAIL_NAME_CORPORATION, Constants.ZENWK);
    // Dto para enviar a cola de rabbithMq.
    dto.setTo(email);
    dto.setSubject(MessagesVerificationEnum.TOKEN_EMAIL_SUBJECT.getMessage(code));
    dto.setVariables(metadata);
    dto.setTemplateName(Constants.TOKEN_EMAIL_TEMPLATE);
    return dto;
  }

  /**
   * <p>
   * <b> CU003_Gestionar token de verificación. </b> lida un código de verificación generado
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
      Token token = getToken(dto);
      String messageError = validateToken(dto, token);
      // Validacion del token
      if (!messageError.isEmpty()) {
        throw new IllegalArgumentException(messageError);
      }
      // Si todo es exitoso se elimina el token
      // tokenRepository.delete(token);
      saveSuccessLog(HttpStatus.OK.value(), logSecurity, logSecurityUserRespository);
      return true;
    } catch (RuntimeException e) {
      setLogSecurityError(e, logSecurity);
      throw new FunctionalException(e.getMessage(), e.getCause(), logSecurityUserRespository,
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
    } else if (token.getExpirationDate().isBefore(LocalDateTime.now())) {
      messageError = VerificationExceptionEnum.FUNC_VERIFICATION_TOKEN_EXPIRATION.getCodeMessage();
    } else if (!token.getUuid().equals(dto.getUuid())) {
      messageError = VerificationExceptionEnum.FUNC_VERIFICATION_UUID_NOT_MATCH.getCodeMessage();
    }
    return messageError;
  }

  /**
   * <p>
   * <b> U003_Gestionar token de verificación. </b> Recupera el token con todos sus datos
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param dto
   * @return
   */
  private Token getToken(TokenDTO dto) {
    if (dto.getCode() != null && !dto.getCode().isEmpty()) {
      return tokenRepository.findByCode(dto.getCode())
          .orElseThrow(() -> new EntityNotFoundException(
              VerificationExceptionEnum.FUNC_VERIFICATION_TOKEN_NOT_FOUND.getCodeMessage()));
    }
    if (dto.getUuid() != null && !dto.getUuid().isEmpty()) {
      return tokenRepository.findByUuid(dto.getUuid())
          .orElseThrow(() -> new EntityNotFoundException(
              VerificationExceptionEnum.FUNC_VERIFICATION_UUID_NOT_FOUND.getCodeMessage()));

    } else {
      return tokenRepository.findByEmail(dto.getEmail())
          .orElseThrow(() -> new EntityNotFoundException(
              VerificationExceptionEnum.FUNC_VERIFICATION_TOKEN_NOT_FOUND.getCodeMessage()));
    }

  }

}
