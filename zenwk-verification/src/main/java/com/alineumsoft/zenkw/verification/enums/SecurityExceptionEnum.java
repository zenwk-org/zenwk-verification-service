package com.alineumsoft.zenkw.verification.enums;

import com.alineumsoft.zenkw.verification.common.constants.CommonMessageConstants;
import com.alineumsoft.zenkw.verification.common.exception.enums.CoreExceptionEnum;
import com.alineumsoft.zenkw.verification.common.message.component.MessageSourceAccessorComponent;

import lombok.Getter;

/**
 * <p>
 * Enum para los mensajes a mostrar cuando se generan excepciones funcionales
 * predefinidas en el sistema.
 * <p>
 * 
 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
 * @project verification-zenwk
 * @class ErrorCodeException
 */
@Getter
public enum SecurityExceptionEnum {
	/**
	 * Verificación 
	 */
	FUNC_VERIFICATION_TOKEN_EXPIRATION("FUNC_SEC_0116","functional.verification.token.expiration"),
	FUNC_VERIFICATION_TOKEN_NOT_FOUND("FUNC_SEC_0117","functional.verification.token.notfound");
	/**
	 * code
	 */
	private final String code;
	/**
	 * key
	 */
	private final String key;

	/**
	 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
	 * @param codeException
	 * @param keyMessage
	 */
	SecurityExceptionEnum(String code, String keyMessage) {
		this.code = code;
		this.key = keyMessage;

	}

	/**
	 * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
	 * @return
	 */
	public String getMessage() {
		try {
			return MessageSourceAccessorComponent.getMessage(key);
		} catch (Exception e) {
			throw new RuntimeException(CoreExceptionEnum.TECH_COMMON_MESSAGE_NOT_FOUND.getCodeMessage(key));
		}
	}

	/**
	 * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
	 * @return
	 */
	public String getMessage(String... params) {
		try {
			return MessageSourceAccessorComponent.getMessage(key, params);
		} catch (Exception e) {
			throw new RuntimeException(CoreExceptionEnum.TECH_COMMON_MESSAGE_NOT_FOUND.getCodeMessage(key));
		}
	}

	/**
	 * <p>
	 * <b> General User Exception. </b> Recupera el mensaje incluyendo el codigo
	 * </p>
	 * 
	 * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
	 * @return
	 */
	public String getCodeMessage() {
		return String.format(CommonMessageConstants.FORMAT_EXCEPTION, code, getMessage());
	}

	/**
	 * <p>
	 * <b> General User Exception. </b> Recupera el mensaje incluyendo el codigo con
	 * parametros
	 * </p>
	 * 
	 * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
	 * @return
	 */
	public String getCodeMessage(String... params) {
		return String.format(CommonMessageConstants.FORMAT_EXCEPTION, code, getMessage(params));
	}

}
