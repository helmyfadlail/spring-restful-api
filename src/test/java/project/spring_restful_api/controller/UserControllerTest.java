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
import project.spring_restful_api.model.*;
import project.spring_restful_api.repository.UserRepository;
import project.spring_restful_api.security.BCrypt;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

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
        void registerSuccess() throws Exception {
                RegisterUserRequest request = new RegisterUserRequest();
                request.setUsername("helmy_fadlail");
                request.setPassword("rahasia");
                request.setName("Helmy Fadlail Albab");

                mockMvc.perform(
                                post("/api/users")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(status().isOk())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertEquals("OK", response.getData());
                                });
        }

        @Test
        void registerBadRequest() throws Exception {
                RegisterUserRequest request = new RegisterUserRequest();
                request.setUsername("");
                request.setPassword("");
                request.setName("");

                mockMvc.perform(
                                post("/api/users")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(status().isBadRequest())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void registerDuplicate() throws Exception {
                User user = new User();
                user.setUsername("helmy_fadlail");
                user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
                user.setName("Helmy Fadlail Albab");

                userRepository.save(user);

                RegisterUserRequest request = new RegisterUserRequest();
                request.setUsername("helmy_fadlail");
                request.setPassword("rahasia");
                request.setName("Helmy Fadlail Albab");

                mockMvc.perform(
                                post("/api/users")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(status().isBadRequest())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void getUserSuccess() throws Exception {
                User user = new User();
                user.setName("Helmy Fadlail Albab");
                user.setUsername("helmy_fadlail");
                user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
                user.setToken("test_token");
                user.setTokenExpiredAt(System.currentTimeMillis() + 10000000000L);
                userRepository.save(user);

                mockMvc.perform(
                                get("/api/users/current")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<UserResponse> response = objectMapper
                                                        .readValue(result.getResponse().getContentAsString(),
                                                                        new TypeReference<>() {
                                                                        });

                                        assertNull(response.getErrors());
                                        assertEquals("helmy_fadlail", response.getData().getUsername());
                                        assertEquals("Helmy Fadlail Albab", response.getData().getName());
                                });
        }

        @Test
        void getUserUnauthorizedTokenNotSend() throws Exception {
                mockMvc.perform(
                                get("/api/users/current")
                                                .accept(MediaType.APPLICATION_JSON))
                                .andExpectAll(
                                                status().isUnauthorized())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });

                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void getUserTokenExpired() throws Exception {
                User user = new User();
                user.setName("Helmy Fadlail Albab");
                user.setUsername("helmy_fadlail");
                user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
                user.setToken("test_token");
                user.setTokenExpiredAt(System.currentTimeMillis() - 100000000);
                userRepository.save(user);

                mockMvc.perform(
                                get("/api/users/current")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isUnauthorized())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });

                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void updateUserUnauthorized() throws Exception {
                UpdateUserRequest request = new UpdateUserRequest();

                mockMvc.perform(
                                patch("/api/users/current")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpectAll(
                                                status().isUnauthorized())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });

                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void updateUserSuccess() throws Exception {
                User user = new User();
                user.setName("Helmy Fadlail Albab");
                user.setUsername("helmy_fadlail");
                user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
                user.setToken("test_token");
                user.setTokenExpiredAt(System.currentTimeMillis() + 10000000000L);
                userRepository.save(user);

                UpdateUserRequest request = new UpdateUserRequest();
                request.setName("Helmy Fadlail");
                request.setPassword("rahasiaaaa");

                mockMvc.perform(
                                patch("/api/users/current")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<UserResponse> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });

                                        assertNull(response.getErrors());
                                        assertEquals("Helmy Fadlail", response.getData().getName());
                                        assertEquals("helmy_fadlail", response.getData().getUsername());

                                        User userDb = userRepository.findById("helmy_fadlail").orElse(null);
                                        assertNotNull(userDb);
                                        assertTrue(BCrypt.checkpw("rahasiaaaa", userDb.getPassword()));
                                });
        }

}
