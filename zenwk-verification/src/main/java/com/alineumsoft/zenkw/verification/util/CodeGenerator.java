package com.alineumsoft.zenkw.verification.util;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * <p>
 * Generador de códigos
 * </p>
 * 
 * @author <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
 * @project verification-zenwk
 * @class codeGenerator
 */
public class CodeGenerator {
  /**
   * random
   */
  private static SecureRandom random = new SecureRandom();

  /**
   * 
   * <p>
   * <b> CU003_Gestionar token de verificación. </b> Generador.
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param size
   * @return
   */
  public static String generateCode(int size) {
    byte[] randomBytes = new byte[size];
    random.nextBytes(randomBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes).substring(0, size);
  }

  /**
   * 
   * <p>
   * <b> CU001_Gestionar token de verificación </b> Genera UUID
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @return
   */
  public static String generateUUID() {
    UUID uuid = UUID.randomUUID();
    return uuid.toString();

  }

}
