package bg.softuni.taskmaster.web.controller;

import bg.softuni.taskmaster.model.entity.Answer;
import bg.softuni.taskmaster.model.entity.Question;
import bg.softuni.taskmaster.model.entity.User;
import bg.softuni.taskmaster.repository.QuestionRepository;
import bg.softuni.taskmaster.testutils.QuestionTestDataUtils;
import bg.softuni.taskmaster.testutils.UserTestDataUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class QuestionControllerTest {

    public static final String TEST_SORT_PROPERTIES = "createdTime";
    public static final String TEST_SORT_DIRECTION = "asc";
    public static final String TEST_TITLE = "Lists in java";
    public static final String TEST_TAGS = "java lists";
    public static final String TEST_DESCRIPTION = "Create list of numbers";
    public static final String TEST_CODE = "List.of(2, 4, 7, 6)";
    public static final String TEST_ANSWER_DESCRIPTION = "This is easy to solve. Use StringUtils";
    public static final String TEST_ANSWER_CODE = "StringUtils.capitalize(\"Test string\");";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionTestDataUtils questionTestDataUtils;

    @Autowired
    private UserTestDataUtils userTestDataUtils;
    private User testUser;

    private Question testQuestion;

    @BeforeEach
    void setUp() {
        testUser = userTestDataUtils.saveTestUser("testUser", "test@me.com");
        testQuestion = questionTestDataUtils
                .saveTestQuestion(testUser, TEST_TITLE, TEST_TAGS, TEST_DESCRIPTION, TEST_CODE);
    }

    @AfterEach
    void tearDown() {
        userTestDataUtils.clearDB();
        questionTestDataUtils.clearDB();
    }

    @Test
    @WithMockUser("testUser")
    public void test_GetAll() throws Exception {
        addTestQuestions();
        mockMvc.perform(get("/questions")
                        .param("page", "1")
                        .param("sort", TEST_SORT_PROPERTIES + "," + TEST_SORT_DIRECTION)
                        .param("search_query", TEST_TITLE)
                )
                .andExpect(status().isOk())
                .andExpect(view().name("questions"))
                .andExpect(model().attributeExists("sortProperties", "sortDirection",
                        "searchQuery", "foundedQuestions"))
                .andExpect(model().attribute("searchQuery", Matchers.equalTo(TEST_TITLE)))
                .andExpect(model().attribute("sortProperties", Matchers.equalTo(TEST_SORT_PROPERTIES)))
                .andExpect(model().attribute("sortDirection", Matchers.equalTo(TEST_SORT_DIRECTION)))
                .andExpect(model().attribute("foundedQuestions",
                        hasProperty("content", Matchers.hasSize(5))))
                .andExpect(model().attribute("foundedQuestions",
                        hasProperty("content", hasItem(
                                allOf(
                                        hasProperty("title", equalTo(TEST_TITLE))
                                )
                        ))));

    }

    @Test
    @WithMockUser("testUser")
    public void test_GetAll_WithDefaultSort_And_Empty_SearchQuery() throws Exception {
        addTestQuestions();
        mockMvc.perform(get("/questions"))
                .andExpect(status().isOk())
                .andExpect(view().name("questions"))
                .andExpect(model().attributeExists("sortProperties", "sortDirection",
                        "searchQuery", "foundedQuestions"))
                .andExpect(model().attribute("searchQuery", Matchers.equalTo("")))
                .andExpect(model().attribute("sortProperties", Matchers.equalTo("createdTime")))
                .andExpect(model().attribute("sortDirection", Matchers.equalTo("desc")))
                .andExpect(model().attribute("foundedQuestions",
                        hasProperty("totalElements", equalTo(7L))));

    }

    @Test
    @WithMockUser("testUser")
    public void test_GetAll_With_NotValidSort() throws Exception {
        addTestQuestions();
        mockMvc.perform(get("/questions")
                        .param("page", "1")
                        .param("sort", TEST_SORT_PROPERTIES + "wrong" + "," + TEST_SORT_DIRECTION)
                        .param("search_query", TEST_TITLE)
                )
                .andExpect(status().isFound());
    }

    private void addTestQuestions() {
        for (int i = 0; i < 5; i++) {
            questionTestDataUtils.saveTestQuestion(testUser, TEST_TITLE, TEST_TAGS, TEST_DESCRIPTION, TEST_CODE);
        }
        questionTestDataUtils.saveTestQuestion(testUser, "Other Title", TEST_TAGS, TEST_DESCRIPTION, TEST_CODE);
    }

    @Test
    @WithMockUser("testUser")
    public void test_AksView() throws Exception {
        mockMvc.perform(get("/questions/ask"))
                .andExpect(status().isOk())
                .andExpect(view().name("ask-question"))
                .andExpect(model().attributeExists("askQuestionData"));
    }

    @Test
    @WithMockUser("testUser")
    public void test_Aks() throws Exception {
        questionRepository.deleteAll();
        mockMvc.perform(post("/questions/ask")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .formFields(getValidQuestionAskDTOFormFields()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("/questions/{id:[0-9]+}"));
        assertEquals(1, questionRepository.count());
    }

    @Test
    @WithMockUser("testUser")
    public void test_Aks_With_NotValid_DTO() throws Exception {
        mockMvc.perform(post("/questions/ask")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .formFields(getNotValidQuestionAskDTOFormFields()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/questions/ask"))
                .andExpect(flash().attributeCount(2))
                .andExpect(flash().attributeExists("askQuestionData",
                        "org.springframework.validation.BindingResult.askQuestionData"));
    }

    @Test
    @WithMockUser("testUser")
    public void test_Answer() throws Exception {
        mockMvc.perform(post(ServletUriComponentsBuilder
                        .fromPath("/questions/answer/{id}").build(testQuestion.getId()))
                        .with(csrf())
                        .formFields(getValidAnswerDTOFormFields()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("/questions/{id:[0-9]+}"))
                .andExpect(flash().attributeCount(1))
                .andExpect(flash().attributeExists("successfullyAddedAnswer"))
                .andExpect(flash().attribute("successfullyAddedAnswer", equalTo(true)));

        Optional<Question> questionWithAnswer = questionRepository.findById(testQuestion.getId());
        assertTrue(questionWithAnswer.isPresent());

        testQuestion = questionWithAnswer.get();
        assertEquals(1, testQuestion.getAnswers().size());
        Answer answer = testQuestion.getAnswers().getFirst();
        assertEquals(TEST_ANSWER_DESCRIPTION, answer.getDescription());
        assertEquals(TEST_ANSWER_CODE, answer.getCode());
    }

    @Test
    @WithMockUser("testUser")
    public void test_Answer_With_NotValid_AnswerDTO() throws Exception {
        mockMvc.perform(post(ServletUriComponentsBuilder
                        .fromPath("/questions/answer/{id}").build(testQuestion.getId()))
                        .with(csrf())
                        .formFields(getNotValidAnswerDTOFormFields()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("/questions/{id:[0-9]+}"))
                .andExpect(flash().attributeCount(3))
                .andExpect(flash().attributeExists("invalidData", "questionAnswerData",
                        "org.springframework.validation.BindingResult.questionAnswerData"))
                .andExpect(flash().attribute("invalidData", equalTo(true)));

        Optional<Question> questionWithAnswer = questionRepository.findById(testQuestion.getId());
        assertTrue(questionWithAnswer.isPresent());
        testQuestion = questionWithAnswer.get();
        assertEquals(0, testQuestion.getAnswers().size());
    }

    @Test
    @WithMockUser("testUser")
    public void test_Answer_With_NotValid_QuestionId() throws Exception {
        mockMvc.perform(post(ServletUriComponentsBuilder
                        .fromPath("/questions/answer/{id}").build(-2))
                        .with(csrf())
                        .formFields(getValidAnswerDTOFormFields()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("object-not-found"));
    }

    private MultiValueMap<String, String> getNotValidAnswerDTOFormFields() {
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("description", "T");
        return map;
    }

    private MultiValueMap<String, String> getValidAnswerDTOFormFields() {
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("description", TEST_ANSWER_DESCRIPTION);
        map.add("code", TEST_ANSWER_CODE);
        return map;
    }


    @Test
    @WithMockUser("testUser")
    public void test_detailsView() throws Exception {
        mockMvc.perform(get(ServletUriComponentsBuilder.fromPath("/questions/{id}")
                        .build(testQuestion.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("question-details"))
                .andExpect(model().attributeExists("questionData", "questionAnswerData"))
                .andExpect(model().attribute("questionData",
                        hasProperty("id", hasToString(String.valueOf(testQuestion.getId())))))
                .andExpect(model().attribute("questionData",
                        hasProperty("title", equalTo(TEST_TITLE))))
                .andExpect(model().attribute("questionData",
                        hasProperty("description", equalTo(TEST_DESCRIPTION))))
                .andExpect(model().attribute("questionData",
                        hasProperty("code", equalTo(TEST_CODE))))
                .andExpect(model().attribute("questionData",
                        hasProperty("tags",
                                equalTo(Arrays.stream(TEST_TAGS.split("\\s+"))
                                        .collect(Collectors.toCollection(LinkedHashSet::new))))))
                .andExpect(model().attribute("questionData",
                        hasProperty("user",
                                hasProperty("username", equalTo(testUser.getUsername())))))
                .andExpect(model().attribute("questionData",
                        hasProperty("user",
                                hasProperty("profilePictureUrl",
                                        equalTo(testUser.getProfilePicture().getUrl())))));
    }

    @Test
    @WithMockUser("testUser")
    public void test_detailsView_With_InvalidId() throws Exception {
        mockMvc.perform(get(ServletUriComponentsBuilder.fromPath("/questions/{id}")
                        .build(-2L)))
                .andExpect(status().isNotFound())
                .andExpect(view().name("object-not-found"));
    }

    @Test
    @WithMockUser("testUser")
    public void test_Delete() throws Exception {
        mockMvc.perform(delete(ServletUriComponentsBuilder.fromPath("/questions/{id}")
                        .build(testQuestion.getId()))
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
        assertEquals(0, questionRepository.count());
    }

    @Test
    @WithMockUser("otherUser")
    public void test_Delete_WithOtherUser() throws Exception {
        userTestDataUtils.saveTestUser("otherUser", "other@me.com");
        mockMvc.perform(delete(ServletUriComponentsBuilder.fromPath("/questions/{id}")
                        .build(testQuestion.getId()))
                        .with(csrf()))
                .andExpect(status().isForbidden());
        assertEquals(1, questionRepository.count());
    }

    @Test
    @WithMockUser(username = "testAdminUser", roles = {"USER", "ADMIN"})
    public void test_Delete_With_InvalidId() throws Exception {
        userTestDataUtils.saveTestUser("testAdminUser", "admin@me.com");
        mockMvc.perform(delete(ServletUriComponentsBuilder.fromPath("/questions/{id}")
                        .build(-2L))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(view().name("object-not-found"));
        assertEquals(1, questionRepository.count());
    }

    private MultiValueMap<String, String> getNotValidQuestionAskDTOFormFields() {
        MultiValueMap<String, String> map = getValidQuestionAskDTOFormFields();
        map.add("title", "List");
        map.add("tags", "a o n a a");
        return map;
    }

    private MultiValueMap<String, String> getValidQuestionAskDTOFormFields() {
        LinkedMultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("title", TEST_TITLE);
        map.add("tags", TEST_TAGS);
        map.add("description", TEST_DESCRIPTION);
        map.add("code", TEST_CODE);
        return map;
    }
}