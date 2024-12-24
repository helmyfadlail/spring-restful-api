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
import project.spring_restful_api.entity.Contact;
import project.spring_restful_api.entity.User;
import project.spring_restful_api.model.ContactResponse;
import project.spring_restful_api.model.CreateContactRequest;
import project.spring_restful_api.model.UpdateContactRequest;
import project.spring_restful_api.model.WebResponse;
import project.spring_restful_api.repository.ContactRepository;
import project.spring_restful_api.repository.UserRepository;
import project.spring_restful_api.security.BCrypt;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ContactRepository contactRepository;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                contactRepository.deleteAll();
                userRepository.deleteAll();

                User user = new User();
                user.setUsername("helmy_fadlail");
                user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
                user.setName("Helmy Fadlail");
                user.setToken("test_token");
                user.setTokenExpiredAt(System.currentTimeMillis() + 1000000000);
                userRepository.save(user);
        }

        @Test
        void createContactBadRequest() throws Exception {
                CreateContactRequest request = new CreateContactRequest();
                request.setFirstName("albabbb");
                request.setEmail("helmy.5@gmail.com");

                mockMvc.perform(
                                post("/api/contacts")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isBadRequest())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<String>>() {
                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void createContactSuccess() throws Exception {
                CreateContactRequest request = new CreateContactRequest();
                request.setFirstName("Helmy");
                request.setLastName("Fadlail");
                request.setEmail("helmyfadlail.5@gmail.com");
                request.setPhone("081334105663");

                mockMvc.perform(
                                post("/api/contacts")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<ContactResponse> response = objectMapper
                                                        .readValue(result.getResponse().getContentAsString(),
                                                                        new TypeReference<>() {
                                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals("Helmy", response.getData().getFirstName());
                                        assertEquals("Fadlail", response.getData().getLastName());
                                        assertEquals("helmyfadlail.5@gmail.com", response.getData().getEmail());
                                        assertEquals("081334105663", response.getData().getPhone());

                                        assertTrue(contactRepository.existsById(response.getData().getId()));
                                });
        }

        @Test
        void getContactNotFound() throws Exception {
                mockMvc.perform(
                                get("/api/contacts/5000")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isNotFound())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<String>>() {
                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void getContactSuccess() throws Exception {
                User user = userRepository.findById("helmy_fadlail").orElseThrow();

                Contact contact = new Contact();
                contact.setId(UUID.randomUUID().toString());
                contact.setUser(user);
                contact.setFirstName("Helmy");
                contact.setLastName("Fadlail");
                contact.setEmail("helmyfadlail.5@gmail.com");
                contact.setPhone("081334105663");
                contactRepository.save(contact);

                mockMvc.perform(
                                get("/api/contacts/" + contact.getId())
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<ContactResponse> response = objectMapper
                                                        .readValue(result.getResponse().getContentAsString(),
                                                                        new TypeReference<>() {
                                                                        });
                                        assertNull(response.getErrors());

                                        assertEquals(contact.getId(), response.getData().getId());
                                        assertEquals(contact.getFirstName(), response.getData().getFirstName());
                                        assertEquals(contact.getLastName(), response.getData().getLastName());
                                        assertEquals(contact.getEmail(), response.getData().getEmail());
                                        assertEquals(contact.getPhone(), response.getData().getPhone());
                                });
        }

        @Test
        void updateContactBadRequest() throws Exception {
                UpdateContactRequest request = new UpdateContactRequest();
                request.setFirstName("");
                request.setEmail("helmy");

                mockMvc.perform(
                                patch("/api/contacts/5000")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isBadRequest())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<String>>() {
                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void updateContactSuccess() throws Exception {
                User user = userRepository.findById("helmy_fadlail").orElseThrow();

                Contact contact = new Contact();
                contact.setId(UUID.randomUUID().toString());
                contact.setUser(user);
                contact.setFirstName("Helmy");
                contact.setLastName("Fadlail");
                contact.setEmail("helmyfadlail.5@gmail.com");
                contact.setPhone("081334105663");
                contactRepository.save(contact);

                CreateContactRequest request = new CreateContactRequest();
                request.setFirstName("Helmyyy");
                request.setLastName("Fadlaillll");
                request.setEmail("helmy.5@gmail.com");
                request.setPhone("081334101234");

                mockMvc.perform(
                                patch("/api/contacts/" + contact.getId())
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<ContactResponse> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(request.getFirstName(), response.getData().getFirstName());
                                        assertEquals(request.getLastName(), response.getData().getLastName());
                                        assertEquals(request.getEmail(), response.getData().getEmail());
                                        assertEquals(request.getPhone(), response.getData().getPhone());

                                        assertTrue(contactRepository.existsById(response.getData().getId()));
                                });
        }

        @Test
        void deleteContactNotFound() throws Exception {
                mockMvc.perform(
                                delete("/api/contacts/5000")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isNotFound())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<WebResponse<String>>() {
                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void deleteContactSuccess() throws Exception {
                User user = userRepository.findById("helmy_fadlail").orElseThrow();

                Contact contact = new Contact();
                contact.setId(UUID.randomUUID().toString());
                contact.setUser(user);
                contact.setFirstName("Helmy");
                contact.setLastName("Fadlail");
                contact.setEmail("helmyfadlail.5@gmail.com");
                contact.setPhone("081334105663");
                contactRepository.save(contact);

                mockMvc.perform(
                                delete("/api/contacts/" + contact.getId())
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals("OK", response.getData());
                                });
        }

        @Test
        void searchNotFound() throws Exception {
                mockMvc.perform(
                                get("/api/contacts")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<List<ContactResponse>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(0, response.getData().size());
                                        assertEquals(0, response.getPaging().getTotalPage());
                                        assertEquals(0, response.getPaging().getCurrentPage());
                                        assertEquals(10, response.getPaging().getSize());
                                });
        }

        @Test
        void searchSuccess() throws Exception {
                User user = userRepository.findById("helmy_fadlail").orElseThrow();

                for (int i = 0; i < 100; i++) {
                        Contact contact = new Contact();
                        contact.setId(UUID.randomUUID().toString());
                        contact.setUser(user);
                        contact.setFirstName("Helmy " + i);
                        contact.setLastName("Fadlail");
                        contact.setEmail("helmyfadlail.5@gmail.com");
                        contact.setPhone("081334105663");
                        contactRepository.save(contact);
                }

                mockMvc.perform(
                                get("/api/contacts")
                                                .queryParam("name", "Helmy")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<List<ContactResponse>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(10, response.getData().size());
                                        assertEquals(10, response.getPaging().getTotalPage());
                                        assertEquals(0, response.getPaging().getCurrentPage());
                                        assertEquals(10, response.getPaging().getSize());
                                });

                mockMvc.perform(
                                get("/api/contacts")
                                                .queryParam("name", "Fadlail")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<List<ContactResponse>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(10, response.getData().size());
                                        assertEquals(10, response.getPaging().getTotalPage());
                                        assertEquals(0, response.getPaging().getCurrentPage());
                                        assertEquals(10, response.getPaging().getSize());
                                });

                mockMvc.perform(
                                get("/api/contacts")
                                                .queryParam("email", "fadlail.5")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<List<ContactResponse>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(10, response.getData().size());
                                        assertEquals(10, response.getPaging().getTotalPage());
                                        assertEquals(0, response.getPaging().getCurrentPage());
                                        assertEquals(10, response.getPaging().getSize());
                                });

                mockMvc.perform(
                                get("/api/contacts")
                                                .queryParam("phone", "5663")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<List<ContactResponse>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(10, response.getData().size());
                                        assertEquals(10, response.getPaging().getTotalPage());
                                        assertEquals(0, response.getPaging().getCurrentPage());
                                        assertEquals(10, response.getPaging().getSize());
                                });

                mockMvc.perform(
                                get("/api/contacts")
                                                .queryParam("phone", "5663")
                                                .queryParam("page", "500")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<List<ContactResponse>> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(0, response.getData().size());
                                        assertEquals(10, response.getPaging().getTotalPage());
                                        assertEquals(500, response.getPaging().getCurrentPage());
                                        assertEquals(10, response.getPaging().getSize());
                                });
        }
}