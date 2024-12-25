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
import project.spring_restful_api.entity.Address;
import project.spring_restful_api.entity.Contact;
import project.spring_restful_api.entity.User;
import project.spring_restful_api.model.AddressResponse;
import project.spring_restful_api.model.CreateAddressRequest;
import project.spring_restful_api.model.UpdateAddressRequest;
import project.spring_restful_api.model.WebResponse;
import project.spring_restful_api.repository.AddressRepository;
import project.spring_restful_api.repository.ContactRepository;
import project.spring_restful_api.repository.UserRepository;
import project.spring_restful_api.security.BCrypt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AddressControllerTest {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private ContactRepository contactRepository;

        @Autowired
        private AddressRepository addressRepository;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {
                addressRepository.deleteAll();
                contactRepository.deleteAll();
                userRepository.deleteAll();

                User user = new User();
                user.setUsername("helmy_fadlail");
                user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
                user.setName("Helmy Fadlail");
                user.setToken("test_token");
                user.setTokenExpiredAt(System.currentTimeMillis() + 1000000000);
                userRepository.save(user);

                Contact contact = new Contact();
                contact.setId("test_contact");
                contact.setUser(user);
                contact.setFirstName("Helmy");
                contact.setLastName("Fadlail");
                contact.setEmail("helmyfadlail.5@gmail.com");
                contact.setPhone("081334105663");
                contactRepository.save(contact);
        }

        @Test
        void createAddressBadRequest() throws Exception {
                CreateAddressRequest request = new CreateAddressRequest();
                request.setCountry("");

                mockMvc.perform(
                                post("/api/contacts/test-123/addresses")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isBadRequest())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void createAddressSuccess() throws Exception {
                CreateAddressRequest request = new CreateAddressRequest();
                request.setStreet("Jln. Raya Sawahan");
                request.setCity("Nganjuk");
                request.setProvince("Jawa Timur");
                request.setCountry("Indonesia");
                request.setPostalCode("64473");

                mockMvc.perform(
                                post("/api/contacts/test_contact/addresses")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<AddressResponse> response = objectMapper
                                                        .readValue(result.getResponse().getContentAsString(),
                                                                        new TypeReference<>() {
                                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(request.getStreet(), response.getData().getStreet());
                                        assertEquals(request.getCity(), response.getData().getCity());
                                        assertEquals(request.getProvince(), response.getData().getProvince());
                                        assertEquals(request.getCountry(), response.getData().getCountry());
                                        assertEquals(request.getPostalCode(), response.getData().getPostalCode());

                                        assertTrue(addressRepository.existsById(response.getData().getId()));
                                });
        }

        @Test
        void getAddressNotFound() throws Exception {
                mockMvc.perform(
                                get("/api/contacts/test-123/addresses/test-123")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isNotFound())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void getAddressSuccess() throws Exception {
                Contact contact = contactRepository.findById("test_contact").orElseThrow();

                Address address = new Address();
                address.setId("test_address");
                address.setContact(contact);
                address.setStreet("Jln. Raya Sawahan");
                address.setCity("Nganjuk");
                address.setProvince("Jawa Timur");
                address.setCountry("Indonesia");
                address.setPostalCode("64473");
                addressRepository.save(address);

                mockMvc.perform(
                                get("/api/contacts/test_contact/addresses/test_address")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<AddressResponse> response = objectMapper
                                                        .readValue(result.getResponse().getContentAsString(),
                                                                        new TypeReference<>() {
                                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(address.getId(), response.getData().getId());
                                        assertEquals(address.getStreet(), response.getData().getStreet());
                                        assertEquals(address.getCity(), response.getData().getCity());
                                        assertEquals(address.getProvince(), response.getData().getProvince());
                                        assertEquals(address.getCountry(), response.getData().getCountry());
                                        assertEquals(address.getPostalCode(), response.getData().getPostalCode());
                                });
        }

        @Test
        void updateAddressBadRequest() throws Exception {
                UpdateAddressRequest request = new UpdateAddressRequest();
                request.setCountry("");

                mockMvc.perform(
                                patch("/api/contacts/test-123/addresses/test-123")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isBadRequest())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void updateAddressSuccess() throws Exception {
                Contact contact = contactRepository.findById("test_contact").orElseThrow();

                Address address = new Address();
                address.setId("test_address");
                address.setContact(contact);
                address.setStreet("Jln. Raya Sawahan");
                address.setCity("Nganjuk");
                address.setProvince("Jawa Timur");
                address.setCountry("Indonesia");
                address.setPostalCode("64473");
                addressRepository.save(address);

                UpdateAddressRequest request = new UpdateAddressRequest();
                address.setStreet("Jln. Raya Berbek - Sawahan");
                address.setCity("Nganjuk Selatan");
                address.setProvince("Jawa Timur Tengah");
                request.setCountry("Indonesia");
                request.setPostalCode("64473");

                mockMvc.perform(
                                patch("/api/contacts/test_contact/addresses/test_address")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request))
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<AddressResponse> response = objectMapper
                                                        .readValue(result.getResponse().getContentAsString(),
                                                                        new TypeReference<>() {
                                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(request.getStreet(), response.getData().getStreet());
                                        assertEquals(request.getCity(), response.getData().getCity());
                                        assertEquals(request.getProvince(), response.getData().getProvince());
                                        assertEquals(request.getCountry(), response.getData().getCountry());
                                        assertEquals(request.getPostalCode(), response.getData().getPostalCode());

                                        assertTrue(addressRepository.existsById(response.getData().getId()));
                                });
        }

        @Test
        void deleteAddressNotFound() throws Exception {
                mockMvc.perform(
                                delete("/api/contacts/test-123/addresses/test-123")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isNotFound())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void deleteAddressSuccess() throws Exception {
                Contact contact = contactRepository.findById("test_contact").orElseThrow();

                Address address = new Address();
                address.setId("test_address");
                address.setContact(contact);
                address.setStreet("Jln. Raya Sawahan");
                address.setCity("Nganjuk");
                address.setProvince("Jawa Timur");
                address.setCountry("Indonesia");
                address.setPostalCode("64473");
                addressRepository.save(address);

                mockMvc.perform(
                                delete("/api/contacts/test_contact/addresses/test_address")
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

                                        assertFalse(addressRepository.existsById("test_address"));
                                });
        }

        @Test
        void listAddressNotFound() throws Exception {
                mockMvc.perform(
                                get("/api/contacts/test-123/addresses")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isNotFound())
                                .andDo(result -> {
                                        WebResponse<String> response = objectMapper.readValue(
                                                        result.getResponse().getContentAsString(),
                                                        new TypeReference<>() {
                                                        });
                                        assertNotNull(response.getErrors());
                                });
        }

        @Test
        void listAddressSuccess() throws Exception {
                Contact contact = contactRepository.findById("test_contact").orElseThrow();

                for (int i = 0; i < 10; i++) {
                        Address address = new Address();
                        address.setId("test_address_" + i);
                        address.setContact(contact);
                        address.setStreet("Jln. Raya Sawahan");
                        address.setCity("Nganjuk");
                        address.setProvince("Jawa Timur");
                        address.setCountry("Indonesia");
                        address.setPostalCode("64473");
                        addressRepository.save(address);
                }

                mockMvc.perform(
                                get("/api/contacts/test_contact/addresses")
                                                .accept(MediaType.APPLICATION_JSON)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .header("X-API-TOKEN", "test_token"))
                                .andExpectAll(
                                                status().isOk())
                                .andDo(result -> {
                                        WebResponse<List<AddressResponse>> response = objectMapper
                                                        .readValue(result.getResponse().getContentAsString(),
                                                                        new TypeReference<>() {
                                                                        });
                                        assertNull(response.getErrors());
                                        assertEquals(10, response.getData().size());
                                });
        }

}