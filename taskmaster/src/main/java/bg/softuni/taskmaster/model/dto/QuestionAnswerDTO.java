package bg.softuni.taskmaster.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

@Getter
@Setter
public class QuestionAnswerDTO implements Serializable {

    @NotNull
    @Length(min = 20, max = 2000)
    private String description;

    @NotNull
    @Length(min = 300, max = 5000)
    private String code;
}