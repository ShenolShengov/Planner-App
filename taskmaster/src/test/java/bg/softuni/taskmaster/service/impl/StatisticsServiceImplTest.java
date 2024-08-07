package bg.softuni.taskmaster.service.impl;

import bg.softuni.taskmaster.model.dto.StatisticsDTO;
import bg.softuni.taskmaster.repository.AnswerRepository;
import bg.softuni.taskmaster.repository.QuestionRepository;
import bg.softuni.taskmaster.repository.TaskRepository;
import bg.softuni.taskmaster.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    public static final long ALL_USERS_COUNT = 20L;
    public static final long ADMIN_USERS_COUNT = 5L;
    public static final long REGULAR_USERS_COUNT = 15L;
    public static final long TASKS_COUNT = 77L;
    public static final long QUESTIONS_COUNT = 57L;
    public static final long ANSWERS_COUNT = 123L;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private TaskRepository mockTaskRepository;
    @Mock
    private QuestionRepository mockQuestionRepository;
    @Mock
    private AnswerRepository mockAnswerRepository;

    private StatisticsServiceImpl statisticsServiceToTest;

    @BeforeEach
    void setUp() {
        statisticsServiceToTest = new StatisticsServiceImpl(mockUserRepository, mockTaskRepository,
                mockQuestionRepository, mockAnswerRepository);
    }

    @Test
    public void test_GetStatistics() {
        when(mockUserRepository.count()).thenReturn(ALL_USERS_COUNT);
        when(mockUserRepository.countAdminUsers()).thenReturn(ADMIN_USERS_COUNT);
        when(mockUserRepository.countRegularUser()).thenReturn(REGULAR_USERS_COUNT);
        when(mockTaskRepository.count()).thenReturn(TASKS_COUNT);
        when(mockQuestionRepository.count()).thenReturn(QUESTIONS_COUNT);
        when(mockAnswerRepository.count()).thenReturn(ANSWERS_COUNT);

        StatisticsDTO statistics = statisticsServiceToTest.getStatistics();

        assertEquals(ALL_USERS_COUNT, statistics.getUsersCount());
        assertEquals(ADMIN_USERS_COUNT, statistics.getAdminUsersCount());
        assertEquals(REGULAR_USERS_COUNT, statistics.getRegularUsersCount());
        assertEquals(TASKS_COUNT, statistics.getTasksCount());
        assertEquals(QUESTIONS_COUNT, statistics.getQuestionsCount());
        assertEquals(ANSWERS_COUNT, statistics.getAnswersCount());
    }
}