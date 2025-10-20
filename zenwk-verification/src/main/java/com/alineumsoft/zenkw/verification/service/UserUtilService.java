package com.alineumsoft.zenkw.verification.service;

import org.springframework.stereotype.Service;
import com.alineumsoft.zenkw.verification.entity.Person;
import com.alineumsoft.zenkw.verification.entity.User;
import com.alineumsoft.zenkw.verification.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserUtilService {
  /**
   * Repositorio para la gestion de la entidad el usuario.
   */
  private final UserRepository userRepository;

  /**
   * <p>
   * <b> CU004_Restablecer contraseña </b> Recupera el username desde el email.
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param email
   * @return
   */
  public String getNameUserFromEmail(String email) {
    try {
      User user =
          userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException());
      Person person = user.getPerson();

      if (person != null && person.getFirstName() != null && person.getLastName() != null) {
        return person.getFirstName().concat(" ").concat(person.getLastName());

      }

      return user.getUsername();
    } catch (RuntimeException e) {
      return email;
    }
  }

}
