package com.alineumsoft.zenkw.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import com.alineumsoft.zenkw.verification.common.message.component.MessageSourceAccessorComponent;

class MessageSourceAccessorComponentTest {

  @Mock
  private MessageSource messageSource;

  private AutoCloseable closeable;

  @BeforeEach
  void setUp() {
    closeable = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  void tearDown() throws Exception {
    closeable.close();
  }

  @Test
  @DisplayName("Debe inicializar correctamente el MessageSource y establecerlo en el holder")
  void shouldInitializeMessageSourceAndSetInHolder() {
    MessageSourceAccessorComponent component = new MessageSourceAccessorComponent(messageSource);
    assertNotNull(component.getMessageSource());
    assertEquals(messageSource, component.getMessageSource());
  }

  @Test
  @DisplayName("Debe obtener mensaje desde el MessageSource (sin parámetros)")
  void shouldGetMessageWithoutParams() {
    Locale locale = Locale.getDefault();
    LocaleContextHolder.setLocale(locale);

    when(messageSource.getMessage(eq("test.key"), isNull(), eq(locale))).thenReturn("Test Message");

    new MessageSourceAccessorComponent(messageSource);
    String result = MessageSourceAccessorComponent.getMessage("test.key");

    assertEquals("Test Message", result);
    verify(messageSource).getMessage("test.key", null, locale);
  }

  @Test
  @DisplayName("Debe obtener mensaje con parámetros")
  void shouldGetMessageWithParams() {
    Locale locale = Locale.FRENCH;
    LocaleContextHolder.setLocale(locale);

    when(messageSource.getMessage(eq("param.key"), eq(new String[] {"A", "B"}), eq(locale)))
        .thenReturn("Message with A and B");

    new MessageSourceAccessorComponent(messageSource);
    String result = MessageSourceAccessorComponent.getMessage("param.key", "A", "B");

    assertEquals("Message with A and B", result);
    verify(messageSource).getMessage("param.key", new String[] {"A", "B"}, locale);
  }

  @Test
  @DisplayName("Debe lanzar excepción si el MessageSource no fue inicializado")
  void shouldThrowExceptionWhenMessageSourceNotInitialized() throws Exception {
    // Forzar reinicio del holder mediante reflexión
    var holderClass = Class.forName(
        "com.alineumsoft.zenkw.verification.common.message.component.MessageSourceAccessorComponent$MessageSourceHolder");
    var field = holderClass.getDeclaredField("source");
    field.setAccessible(true);
    field.set(null, null);

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> MessageSourceAccessorComponent.getMessage("no.init.key"));
    assertTrue(exception.getMessage().contains("MessageSource not initialized"));
  }
}
