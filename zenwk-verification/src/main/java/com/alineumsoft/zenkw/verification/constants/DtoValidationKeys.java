package com.alineumsoft.zenkw.verification.constants;

/**
 * <p>
 * Contiene las claves de los mensajes de validación utilizados en los DTOs para anotaciones de
 * validación como {@code @Valid}, {@code @NotNull}, {@code @Size}, entre otras. Estas claves se
 * utilizan en archivos de internacionalización (i18n) para proporcionar mensajes de error
 * personalizados en las validaciones.
 * </p>
 *
 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
 * @project verification-zenwk
 * @class DtoValidationKeys
 */
public final class DtoValidationKeys {
  public static final String USER_EMAIL_MAX_LENGTH = "validation.user.email.maxlength";
  public static final String USER_EMAIL_INVALID = "validation.user.email.invalid";
  public static final String USER_EMAIL_NOT_NULL = "validation.user.email.notnull";
  public static final String PERSON_DATE_INVALID = "validation.person.date.invalidformat";
  public static final String URL_OR_EMPTY_REGEX = "validation.resetpassaord.url.not.valid";


}
