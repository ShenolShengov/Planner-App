package bg.softuni.taskmaster.events.listeners;

import bg.softuni.taskmaster.events.AnswerToQuestionEvent;
import bg.softuni.taskmaster.model.dto.Payload;
import bg.softuni.taskmaster.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static bg.softuni.taskmaster.model.enums.EmailParam.*;
import static bg.softuni.taskmaster.model.enums.EmailTemplate.ANSWER_TO_QUESTION;
import static bg.softuni.taskmaster.utils.EmailUtils.*;

@Component
@RequiredArgsConstructor
public class AnswerToQuestionListener {

    private final MailService mailService;

    @EventListener
    public void handleAnswerToQuestionEvent(AnswerToQuestionEvent event) {
        Payload payload = mailService.createPayload(APP_MAIL,
                getSubject(event.getQuestionName()), ANSWER_TO_QUESTION,
                toParams(QUESTIONER_USERNAME, event.getQuestionerUsername(), QUESTION_NAME, event.getQuestionName(),
                        ANSWERER_USERNAME, event.getAnswererUsername(), QUESTION_LINK, event.getQuestionLink()),
                event.getQuestionerEmail());
        mailService.send(payload);
    }

    private static String getSubject(String questionName) {
        return String.format(ANSWER_TO_QUESTION_SUBJECT, questionName);
    }
}
