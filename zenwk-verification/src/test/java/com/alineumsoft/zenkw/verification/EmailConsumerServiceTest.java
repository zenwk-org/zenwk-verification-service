package com.alineumsoft.zenkw.verification;



import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.MessagingException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.alineumsoft.zenkw.verification.dto.EmailRequestDTO;
import com.alineumsoft.zenkw.verification.service.EmailConsumerService;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

/**
 * Pruebas unitarias para EmailConsumerService.
 */
class EmailConsumerServiceTest {

  @Mock
  private JavaMailSender mailSender;

  @Mock
  private TemplateEngine templateEngine;

  @InjectMocks
  private EmailConsumerService emailConsumerService;

  @Mock
  private MimeMessage mimeMessage;

  @Captor
  private ArgumentCaptor<MimeMessage> mimeCaptor;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
  }

  @Test
  @DisplayName("Debe enviar correctamente el correo procesando la plantilla con Thymeleaf")
  void testReceiveEmailRequest_Success() throws Exception {
    // Arrange
    EmailRequestDTO dto = new EmailRequestDTO();
    dto.setTo("destinatario@test.com");
    dto.setSubject("Asunto de prueba");
    dto.setTemplateName("template-test");

    Map<String, Object> variables = new HashMap<>();
    variables.put("nombre", "Carlos");
    dto.setVariables(variables);

    when(templateEngine.process(eq("template-test"), any(Context.class)))
        .thenReturn("<html>Contenido generado</html>");

    // Act
    emailConsumerService.receiveEmailRequest(dto);

    // Assert
    verify(mailSender, times(1)).createMimeMessage();
    verify(mailSender, times(1)).send(mimeMessage);
    verify(templateEngine, times(1)).process(eq("template-test"), any(Context.class));
  }

  @Test
  @DisplayName("Debe lanzar MessagingException si ocurre un error al enviar el correo")
  void testReceiveEmailRequest_ThrowsMessagingExceptionOnSend() throws Exception {
    // Arrange
    EmailRequestDTO dto = new EmailRequestDTO();
    dto.setTo("user@test.com");
    dto.setSubject("Falla en envío");
    dto.setTemplateName("template-error");

    // Usa una instancia real de MimeMessage, no un mock
    MimeMessage realMessage = new MimeMessage((Session) null);

    when(mailSender.createMimeMessage()).thenReturn(realMessage);
    when(templateEngine.process(eq("template-error"), any(Context.class)))
        .thenReturn("<html>Error</html>");
    doThrow(new MessagingException("Error al enviar")).when(mailSender)
        .send(any(MimeMessage.class));

    // Act & Assert
    assertThrows(MessagingException.class, () -> emailConsumerService.receiveEmailRequest(dto));

    // Verify
    verify(mailSender, times(1)).createMimeMessage();
    verify(mailSender, times(1)).send(any(MimeMessage.class));
  }


  @Test
  @DisplayName("Debe procesar el correo incluso con variables vacías en el contexto")
  void testReceiveEmailRequest_WithEmptyVariables() throws Exception {
    // Arrange
    EmailRequestDTO dto = new EmailRequestDTO();
    dto.setTo("user@test.com");
    dto.setSubject("Correo sin variables");
    dto.setTemplateName("simple-template");
    dto.setVariables(new HashMap<>());

    when(templateEngine.process(eq("simple-template"), any(Context.class)))
        .thenReturn("<html>Simple</html>");

    // Act
    emailConsumerService.receiveEmailRequest(dto);

    // Assert
    verify(mailSender, times(1)).send(mimeMessage);
    verify(templateEngine, times(1)).process(eq("simple-template"), any(Context.class));
  }
}
