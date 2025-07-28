package com.alineumsoft.zenkw.verification.dto;

import java.io.Serializable;
import com.alineumsoft.zenkw.verification.common.constants.RegexConstants;
import com.alineumsoft.zenkw.verification.constants.DtoValidationKeys;
import com.alineumsoft.zenkw.verification.entity.Token;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * DTO para la gestión de la solicitud de token de verificación.
 * </p>
 * 
 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
 * @project verification-zenwk
 * @class TokenDTO
 */
@Data
@NoArgsConstructor
public class TokenDTO implements Serializable {
  private static final long serialVersionUID = 1L;
  /**
   * Email (max 254).
   */
  @Pattern(regexp = RegexConstants.EMAIL, message = DtoValidationKeys.USER_EMAIL_INVALID)
  @NotNull(message = DtoValidationKeys.USER_EMAIL_NOT_NULL)
  @Size(max = 254, message = DtoValidationKeys.USER_EMAIL_MAX_LENGTH)
  private String email;
  /**
   * Codigo del token.
   */
  private String code;
  /**
   * UUID asociado al objecto.
   */
  private String uuid;

  /**
   * 
   * <p>
   * <b> Constructor </b> Recibe una entidad Token y regresa su DTO.
   * </p>
   * 
   * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
   * @param token
   */
  public TokenDTO(Token token) {
    email = token.getEmail();
    code = token.getCode();
    uuid = token.getUuid();

  }


}
