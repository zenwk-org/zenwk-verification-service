package com.alineumsoft.zenkw.verification;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.alineumsoft.zenkw.verification.entity.Person;
import com.alineumsoft.zenkw.verification.entity.User;
import com.alineumsoft.zenkw.verification.repository.UserRepository;
import com.alineumsoft.zenkw.verification.service.UserUtilService;
import jakarta.persistence.EntityNotFoundException;

class UserUtilServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserUtilService userUtilService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Debe retornar el nombre completo del usuario cuando existe la persona con nombres y apellidos")
  void testGetNameUserFromEmail_WithFullPersonData() {
    Person person = new Person();
    person.setFirstName("Carlos");
    person.setLastName("Alegria");

    User user = new User();
    user.setEmail("user@test.com");
    user.setPerson(person);

    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

    String result = userUtilService.getNameUserFromEmail("user@test.com");

    assertEquals("Carlos Alegria", result);
    verify(userRepository).findByEmail("user@test.com");
  }

  @Test
  @DisplayName("Debe retornar el username cuando la persona no tiene nombres o apellidos")
  void testGetNameUserFromEmail_WithIncompletePerson() {
    Person person = new Person(); // Sin nombres definidos
    User user = new User();
    user.setUsername("user123");
    user.setEmail("user@test.com");
    user.setPerson(person);

    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

    String result = userUtilService.getNameUserFromEmail("user@test.com");

    assertEquals("user123", result);
    verify(userRepository).findByEmail("user@test.com");
  }

  @Test
  @DisplayName("Debe retornar el username cuando la persona es nula")
  void testGetNameUserFromEmail_WithNullPerson() {
    User user = new User();
    user.setUsername("userXYZ");
    user.setEmail("user@test.com");
    user.setPerson(null);

    when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

    String result = userUtilService.getNameUserFromEmail("user@test.com");

    assertEquals("userXYZ", result);
    verify(userRepository).findByEmail("user@test.com");
  }

  @Test
  @DisplayName("Debe retornar el email cuando no se encuentra el usuario")
  void testGetNameUserFromEmail_UserNotFound() {
    when(userRepository.findByEmail("notfound@test.com")).thenThrow(new EntityNotFoundException());

    String result = userUtilService.getNameUserFromEmail("notfound@test.com");

    assertEquals("notfound@test.com", result);
    verify(userRepository).findByEmail("notfound@test.com");
  }

  @Test
  @DisplayName("Debe retornar el email cuando ocurre un error inesperado")
  void testGetNameUserFromEmail_RuntimeException() {
    when(userRepository.findByEmail("fail@test.com"))
        .thenThrow(new RuntimeException("Error inesperado"));

    String result = userUtilService.getNameUserFromEmail("fail@test.com");

    assertEquals("fail@test.com", result);
    verify(userRepository).findByEmail("fail@test.com");
  }
}
