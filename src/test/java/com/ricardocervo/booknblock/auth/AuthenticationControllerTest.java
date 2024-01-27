package com.ricardocervo.booknblock.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ricardocervo.booknblock.block.BlockRepository;
import com.ricardocervo.booknblock.booking.BookingRepository;
import com.ricardocervo.booknblock.property.PropertyRepository;
import com.ricardocervo.booknblock.user.User;
import com.ricardocervo.booknblock.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    User existingUser;
    String existingUserPassword;

    @BeforeEach
    public void setup() {
        existingUser = userRepository.findByEmail("alexa.richmond@example.com").get();
        existingUserPassword = "user123";
    }
    @Test
    public void testAuthenticateSuccess() throws Exception {

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(existingUser.getEmail())
                .password(existingUserPassword)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.user").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.name").value(existingUser.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.email").value(existingUser.getEmail()))
                .andReturn();
    }

    @Test
    public void testAuthenticateInvalidPassword() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(existingUser.getEmail())
                .password("wrongPassword")
                .build();

        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

    @Test
    public void testAuthenticateInvalidUsername() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("invalid_user_name@email.com")
                .password(existingUserPassword)
                .build();

        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn();
    }

}
