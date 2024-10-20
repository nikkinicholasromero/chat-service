package com.chat.common.mail;

import com.chat.BaseUnitTest;
import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.model.message.Message;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class MailServiceTest extends BaseUnitTest {
    @InjectMocks
    private MailService target;

    @Mock
    private MailgunMessagesApi mailgunMessagesApi;

    @Mock
    private File file;

    @Captor
    private ArgumentCaptor<Message> messageCaptor;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(target, "chatPortalUrl", "http://localhost:4200");
        ReflectionTestUtils.setField(target, "fromAddress", "no-reply@chat.com");
        ReflectionTestUtils.setField(target, "supportAddress", "support@chat.com");
        ReflectionTestUtils.setField(target, "domain", "mailgun-domain");
        ReflectionTestUtils.setField(target, "enabled", true);
    }

    @Test
    void send_withoutAttachments() throws JSONException {
        List<String> recipients = new ArrayList<>();
        recipients.add("test@test.com");

        Map<String, Object> variables = new HashMap<>();
        variables.put("test", "testVariable");

        Mail mail = new Mail(MailTemplate.REGISTRATION_CONFIRMATION, recipients, variables);

        target.send(mail);

        verify(mailgunMessagesApi).sendMessage(eq("mailgun-domain"), messageCaptor.capture());

        Message savedMessage = messageCaptor.getValue();
        assertThat(savedMessage.getTemplate()).isEqualTo(MailTemplate.REGISTRATION_CONFIRMATION.template());
        assertThat(savedMessage.getSubject()).isEqualTo(MailTemplate.REGISTRATION_CONFIRMATION.subject());
        assertThat(savedMessage.getTo()).contains(recipients.get(0));
        assertThat(savedMessage.getBcc()).isEqualTo(Set.of("support@chat.com"));
        assertThat(savedMessage.getFrom()).isEqualTo("no-reply@chat.com");

        String expected = """
                {
                    "chat_portal_url": "http://localhost:4200",
                    "test": "testVariable"
                }
                """;

        String actual = savedMessage.getMailgunVariables();
        assertEquals(expected, actual, false);
    }

    @Test
    void send_withAttachments() throws JSONException {
        List<String> recipients = new ArrayList<>();
        recipients.add("test@test.com");

        Map<String, Object> variables = new HashMap<>();
        variables.put("test", "testVariable");

        Mail mail = new Mail(MailTemplate.REGISTRATION_CONFIRMATION, recipients, variables);

        target.send(mail, List.of(file));

        verify(mailgunMessagesApi).sendMessage(eq("mailgun-domain"), messageCaptor.capture());

        Message savedMessage = messageCaptor.getValue();
        assertThat(savedMessage.getTemplate()).isEqualTo(MailTemplate.REGISTRATION_CONFIRMATION.template());
        assertThat(savedMessage.getSubject()).isEqualTo(MailTemplate.REGISTRATION_CONFIRMATION.subject());
        assertThat(savedMessage.getTo()).contains(recipients.get(0));
        assertThat(savedMessage.getBcc()).isEqualTo(Set.of("support@chat.com"));
        assertThat(savedMessage.getFrom()).isEqualTo("no-reply@chat.com");

        String expected = """
                {
                    "chat_portal_url": "http://localhost:4200",
                    "test": "testVariable"
                }
                """;

        String actual = savedMessage.getMailgunVariables();
        assertEquals(expected, actual, false);
    }

    @Test
    void send_disabled() {
        ReflectionTestUtils.setField(target, "enabled", false);

        List<String> recipients = new ArrayList<>();
        recipients.add("test@test.com");

        Map<String, Object> variables = new HashMap<>();
        variables.put("test", "testVariable");

        Mail mail = new Mail(MailTemplate.REGISTRATION_CONFIRMATION, recipients, variables);

        target.send(mail, List.of(file));

        verifyNoInteractions(mailgunMessagesApi);
    }
}
