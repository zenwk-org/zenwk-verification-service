package com.alineumsoft.zenkw.verification.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.alineumsoft.zenkw.verification.common.constants.GeneralConstants;
import com.alineumsoft.zenkw.verification.constants.Constants;
import com.alineumsoft.zenkw.verification.dto.EmailRequestDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Este servicio escuchará la cola de RabbitMQ y enviará el correo
 * </p>
 * 
 * @authxor <a href="mailto:alineumsoft@gmail.com">C. Alegria</a>
 * @project verification-zenwk
 * @class EmailConsumerService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConsumerService {
  /**
   * java mail sender
   */
  private final JavaMailSender mailSender;
  /**
   * templateEngine
   */
  private final TemplateEngine templateEngine;

  /**
   * 
   * <p>
   * <b> CU001_Seguridad_Creacion_Usuario </b> Listener que escuchará la cola de rabbit mq y enviará
   * el correo
   * </p>
   * 
   * @author <a href="alineumsoft@gmail.com">C. Alegria</a>
   * @param dto
   * @throws MessagingException
   */
  @RabbitListener(queues = Constants.RABBITH_EMAIL_QUEUE)
  public void receiveEmailRequest(EmailRequestDTO dto) throws MessagingException {
    // Construcción del correo
    MimeMessage mimeMessage = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, GeneralConstants.UTF8);
    // Se prepara el contexto de thymeleaf
    Context context = new Context();
    context.setVariables(dto.getVariables());

    // Cargar y procesar la plantilla
    String templateHtml = templateEngine.process(dto.getTemplateName(), context);

    helper.setTo(dto.getTo());
    helper.setSubject(dto.getSubject());
    helper.setText(templateHtml, true);
    mailSender.send(mimeMessage);
  }
}
