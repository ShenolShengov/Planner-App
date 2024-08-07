package bg.softuni.taskmaster.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class TaskInfoDTO implements Serializable {

    private Long id;
    private String name;
    private String category;
    private String priority;
    private LocalDate dueDate;
    private String description;
    private String startTime;
    private String endTime;
    private boolean allDay;
}
