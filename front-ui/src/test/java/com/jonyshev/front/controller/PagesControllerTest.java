package com.jonyshev.front.controller;

import com.jonyshev.commons.dto.UserProfileDto;
import com.jonyshev.commons.model.Currency;
import com.jonyshev.front.client.AccountsClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagesController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "spring.thymeleaf.check-template-location=false")
@ActiveProfiles("test")
class PagesControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AccountsClient accountsClient;

    @MockitoBean
    AuthenticationManager authenticationManager;

    @MockitoBean
    SecurityContextRepository securityContextRepository;

    private UserProfileDto profileGuest;
    private UserProfileDto profileAlice;

    @BeforeEach
    void setUp() {
        var accGuest = new UserProfileDto.AccountDto();
        accGuest.setCurrency(Currency.USD);
        accGuest.setValue(new BigDecimal("100.00"));
        accGuest.setExists(true);

        var accAlice = new UserProfileDto.AccountDto();
        accAlice.setCurrency(Currency.CNY);
        accAlice.setValue(new BigDecimal("50.00"));
        accAlice.setExists(true);

        profileGuest = UserProfileDto.builder()
                .login("guest")
                .name("Guest")
                .birthdate("2000-01-01")
                .accounts(List.of(accGuest))
                .build();

        profileAlice = UserProfileDto.builder()
                .login("alice")
                .name("Alice")
                .birthdate("2000-01-01")
                .accounts(List.of(accAlice))
                .build();

        Mockito.when(accountsClient.getListUserProfile())
                .thenReturn(List.of(profileAlice, profileGuest));

        Mockito.when(accountsClient.getUserProfile("guest")).thenReturn(profileGuest);
        Mockito.when(accountsClient.getUserProfile("alice")).thenReturn(profileAlice);
    }

    @Test
    void root_redirectsToMain() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));
    }

    @Test
    void main_guest_populatesModelAndRendersMain() throws Exception {
        mockMvc.perform(get("/main"))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attribute("login", "guest"))
                .andExpect(model().attributeExists("name"))
                .andExpect(model().attributeExists("birthdate"))
                .andExpect(model().attributeExists("accounts"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("currency"))
                .andExpect(model().attribute("cashErrors", List.of()))
                .andExpect(model().attribute("transferErrors", List.of()))
                .andExpect(model().attribute("exchangeErrors", List.of()));

        Mockito.verify(accountsClient).getUserProfile("guest");
        Mockito.verify(accountsClient).getListUserProfile();
    }


    @Test
    void signupGet_rendersSignupWithEmptyErrors() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attribute("errors", List.of()));
    }

    @Test
    void signupPost_emptyFields_returnsSignupWithErrors() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("login", "")
                        .param("name", "")
                        .param("password", "")
                        .param("birthdate", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("errors"));
    }

    @Test
    void signupPost_invalidBirthdate_returnsSignupWithInvalidBirthdate() throws Exception {
        mockMvc.perform(post("/signup")
                        .param("login", "alice")
                        .param("name", "Alice")
                        .param("password", "p1")
                        .param("birthdate", "not-a-date")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("errors"));
    }

    @Test
    void signupPost_under18_returnsSignupWithAgeRestriction() throws Exception {
        var underAge = java.time.LocalDate.now().minusYears(10).toString();

        mockMvc.perform(post("/signup")
                        .param("login", "teen")
                        .param("name", "Teen")
                        .param("password", "p1")
                        .param("birthdate", underAge)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("errors"));
    }

    @Test
    void signupPost_createUserFails_returnsSignupWithError() throws Exception {
        Mockito.when(accountsClient.createUser("alice", "Alice", "p1", "2000-01-01"))
                .thenReturn(false);

        mockMvc.perform(post("/signup")
                        .param("login", "alice")
                        .param("name", "Alice")
                        .param("password", "p1")
                        .param("birthdate", "2000-01-01")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"))
                .andExpect(model().attributeExists("errors"));

        Mockito.verify(accountsClient).createUser("alice", "Alice", "p1", "2000-01-01");
    }

    @Test
    void signupPost_success_authenticatesAndRedirectsToMain() throws Exception {
        Mockito.when(accountsClient.createUser("alice", "Alice", "p1", "2000-01-01"))
                .thenReturn(true);

        Authentication auth = new TestingAuthenticationToken("alice", "p1", "ROLE_USER");
        auth.setAuthenticated(true);
        Mockito.when(authenticationManager.authenticate(any()))
                .thenReturn(auth);

        ArgumentCaptor<org.springframework.security.core.context.SecurityContext> ctxCaptor =
                ArgumentCaptor.forClass(org.springframework.security.core.context.SecurityContext.class);

        mockMvc.perform(post("/signup")
                        .param("login", "alice")
                        .param("name", "Alice")
                        .param("password", "p1")
                        .param("birthdate", "2000-01-01")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"));

        Mockito.verify(authenticationManager).authenticate(any());
        Mockito.verify(securityContextRepository)
                .saveContext(ctxCaptor.capture(), any(), any());

        var savedAuth = ctxCaptor.getValue().getAuthentication();
        org.assertj.core.api.Assertions.assertThat(savedAuth).isNotNull();
        org.assertj.core.api.Assertions.assertThat(savedAuth.getName()).isEqualTo("alice");
        org.assertj.core.api.Assertions.assertThat(savedAuth.isAuthenticated()).isTrue();
    }
}
