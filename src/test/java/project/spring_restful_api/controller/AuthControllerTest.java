package project.spring_restful_api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import project.spring_restful_api.entity.User;
import project.spring_restful_api.model.LoginUserRequest;
import project.spring_restful_api.model.TokenResponse;
import project.spring_restful_api.model.WebResponse;
import project.spring_restful_api.repository.UserRepository;
import project.spring_restful_api.security.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void loginFailedUserNotFound() throws Exception {
        LoginUserRequest request = new LoginUserRequest();
        request.setUsername("helmy_fadlail_albab");
        request.setPassword("rahasia");

        mockMvc.perform(
            post("/api/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isUnauthorized())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(),new TypeReference<>() {});
                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void loginFailedWrongPassword() throws Exception {
        User user = new User();
        user.setName("Helmy Fadlail Albab");
        user.setUsername("helmy_fadlail");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        userRepository.save(user);

        LoginUserRequest request = new LoginUserRequest();
        request.setUsername("helmy_fadlailll");
        request.setPassword("rahasiaku");

        mockMvc.perform(
            post("/api/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isUnauthorized())
                .andDo(result -> {
                    WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
                    assertNotNull(response.getErrors());
                });
    }

    @Test
    void loginSuccess() throws Exception {
        User user = new User();
        user.setName("Helmy Fadlail Albab");
        user.setUsername("helmy_fadlail");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        userRepository.save(user);

        LoginUserRequest request = new LoginUserRequest();
        request.setUsername("helmy_fadlail");
        request.setPassword("rahasia");

        mockMvc.perform(
            post("/api/auth/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpectAll(status().isOk())
                .andDo(result -> {
                    WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});
                    assertNull(response.getErrors());
                    assertNotNull(response.getData().getToken());
                    assertNotNull(response.getData().getExpiredAt());

                    User userDb = userRepository.findById("helmy_fadlail").orElse(null);
                    assertNotNull(userDb);
                    assertEquals(userDb.getToken(), response.getData().getToken());
                    assertEquals(userDb.getTokenExpiredAt(), response.getData().getExpiredAt());
                });
    }
}