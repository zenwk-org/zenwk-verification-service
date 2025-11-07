package com.alineumsoft.zenkw.verification.enums;

import com.alineumsoft.zenkw.verification.common.exception.enums.CoreExceptionEnum;
import com.alineumsoft.zenkw.verification.common.message.component.MessageSourceAccessorComponent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * Enum para los mensajes de verificación del usuario en el sistema.
 * </p>
 * 
 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
 * @project verification-zenwk
 * @class MessagesVerification
 */
@Getter
@RequiredArgsConstructor
public enum MessagesVerificationEnum {
  TEMPLATE_REGISTER_FLOW_EMAIL_SUBJECT(
      "verification.token.email.subject"), TEMPLATE_RESET_PASSWORD_EMAIL_SUBJECT(
          "verification.resetpassword.email.subject");

  /**
   * messageKey
   */
  private final String messageKey;

  /**
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @return
   */
  public String getMessage() {
    try {
      return MessageSourceAccessorComponent.getMessage(messageKey);
    } catch (Exception e) {
      throw new RuntimeException(
          CoreExceptionEnum.TECH_COMMON_MESSAGE_NOT_FOUND.getCodeMessage(messageKey));
    }

  }

  /**
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @return
   */
  public String getMessage(String... params) {
    try {
      return MessageSourceAccessorComponent.getMessage(messageKey, params);
    } catch (Exception e) {
      throw new RuntimeException(
          CoreExceptionEnum.TECH_COMMON_MESSAGE_NOT_FOUND.getCodeMessage(messageKey));
    }
  }

}
