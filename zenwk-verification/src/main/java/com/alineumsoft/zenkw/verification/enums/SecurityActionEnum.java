package com.alineumsoft.zenkw.verification.enums;

import lombok.Getter;

/**
 * <p>
 * Enum que representa las acciones de seguridad en el sistema.
 * </p>
 * 
 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
 * @project verification-zenwk
 * @class ServiceNameEnum
 */
@Getter
public enum SecurityActionEnum {
  /**
   * Verificación de token
   */
  VERIFICATION_SEND_TOKEN("VERIFICATION.SEND_TOKEN"), VERIFICATION_VALIDATE_TOKEN(
      "VERIFICATION.VARIFY_TOKEN"),
  /**
   * Token CSRF
   */
  VERIFICATION_SEND_TOKEN_CSRF("VERIFICATION.SEND_TOKEN_CSRF"), VERIFICATION_VALIDATE_TOKEN_CSRF(
      "VERIFICATION.VARIFY_TOKEN_CSRF");



  /**
   * code
   */
  private final String code;

  /**
   * <p>
   * <b> Constructor </b>
   * </p>
   * 
   * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
   * @param code
   */
  SecurityActionEnum(String code) {
    this.code = code;
  }

}
