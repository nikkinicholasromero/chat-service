package com.chat.common.mail;

import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.model.message.Message;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
public class MailService {
    private final String chatPortalUrl;
    private final String fromAddress;
    private final String supportAddress;
    private final String domain;
    private final Boolean enabled;
    private final MailgunMessagesApi mailgunMessagesApi;

    public MailService(
            @Value("${chat.portal.url}") String chatPortalUrl,
            @Value("${mailgun.from.address}") String fromAddress,
            @Value("${mailgun.support.address}") String supportAddress,
            @Value("${mailgun.domain}") String domain,
            @Value("${mailgun.enabled}") Boolean enabled,
            MailgunMessagesApi mailgunMessagesApi) {
        this.chatPortalUrl = chatPortalUrl;
        this.fromAddress = fromAddress;
        this.supportAddress = supportAddress;
        this.domain = domain;
        this.enabled = enabled;
        this.mailgunMessagesApi = mailgunMessagesApi;
    }

    public void send(Mail mail) {
        send(mail, List.of());
    }

    public void send(Mail mail, List<File> attachments) {
        List<String> recipients = mail.recipients();

        Map<String, Object> variables = mail.variables();
        variables.put("chat_portal_url", chatPortalUrl);

        Message.MessageBuilder messageBuilder = Message.builder()
                .template(mail.template().template())
                .subject(mail.template().subject())
                .to(recipients)
                .from(fromAddress)
                .bcc(supportAddress)
                .mailgunVariables(variables);

        if (CollectionUtils.isNotEmpty(attachments)) {
            messageBuilder = messageBuilder.attachment(attachments);
        }

        if (Boolean.TRUE.equals(enabled)) {
            mailgunMessagesApi.sendMessage(domain, messageBuilder.build());
        }

        attachments.forEach(File::deleteOnExit);
        attachments.forEach(FileUtils::deleteQuietly);
    }
}
