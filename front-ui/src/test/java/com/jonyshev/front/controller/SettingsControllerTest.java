package com.jonyshev.front.controller;

import com.jonyshev.front.client.AccountsClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SettingsController.class)
@ActiveProfiles("test")
class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AccountsClient accounts;

    // ---------- /user/{login}/editPassword ----------

    @Test
    void editPassword_blankParams_redirectsEmptyPassword() throws Exception {
        mockMvc.perform(post("/user/alice/editPassword")
                        .param("password", "")
                        .param("confirm", "")
                        .with(user("alice"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"))
                .andExpect(flash().attribute("passwordErrors", List.of("empty_password")));
    }

    @Test
    void editPassword_userMismatch_redirectsNotAllowed() throws Exception {
        mockMvc.perform(post("/user/alice/editPassword")
                        .param("password", "p1")
                        .param("confirm", "p1")
                        .with(user("bob")) // другой пользователь
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"))
                .andExpect(flash().attribute("passwordErrors", List.of("not_allowed")));
    }

    @Test
    void editPassword_changeFails_setsMismatch() throws Exception {
        Mockito.when(accounts.changePassword("alice", "p1", "p2")).thenReturn(false);

        mockMvc.perform(post("/user/alice/editPassword")
                        .param("password", "p1")
                        .param("confirm", "p2")
                        .with(user("alice"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"))
                .andExpect(flash().attribute("passwordErrors", List.of("mismatch")));
    }

    @Test
    void editPassword_changeOk_noErrors() throws Exception {
        Mockito.when(accounts.changePassword("alice", "p1", "p1")).thenReturn(true);

        mockMvc.perform(post("/user/alice/editPassword")
                        .param("password", "p1")
                        .param("confirm", "p1")
                        .with(user("alice"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"))
                .andExpect(flash().attributeCount(0));
    }

    // ---------- /user/{login}/editUserAccounts ----------

    @Test
    void editUserAccounts_userMismatch_redirectsNotAllowed() throws Exception {
        mockMvc.perform(post("/user/alice/editUserAccounts")
                        .param("name", "Alice")
                        .with(user("bob"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"))
                .andExpect(flash().attribute("userAccountsErrors", List.of("not_allowed")));
    }

    @Test
    void editUserAccounts_profileError_stopsAndReturnsThatError() throws Exception {
        Mockito.when(accounts.updateProfile("alice", "Alice", "2000-01-01"))
                .thenReturn("bad_birthdate");

        mockMvc.perform(post("/user/alice/editUserAccounts")
                        .param("name", "Alice")
                        .param("birthdate", "2000-01-01")
                        .with(user("alice"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"))
                .andExpect(flash().attribute("userAccountsErrors", List.of("bad_birthdate")));

        Mockito.verify(accounts).updateProfile("alice", "Alice", "2000-01-01");
        Mockito.verify(accounts, Mockito.never()).updateAccounts(anyString(), anyList());
    }

    @Test
    void editUserAccounts_accountsUpdateError_setsThatError() throws Exception {
        Mockito.when(accounts.updateProfile("alice", null, null)).thenReturn(""); // без ошибки профиля
        Mockito.when(accounts.updateAccounts("alice", List.of("USD", "EUR")))
                .thenReturn("cannot_update");

        mockMvc.perform(post("/user/alice/editUserAccounts")
                        .param("account", "USD")
                        .param("account", "EUR")
                        .with(user("alice"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"))
                .andExpect(flash().attribute("userAccountsErrors", List.of("cannot_update")));
    }

    @Test
    void editUserAccounts_allOk_noErrors() throws Exception {
        Mockito.when(accounts.updateProfile("alice", "Alice", "2000-01-01")).thenReturn("");
        Mockito.when(accounts.updateAccounts("alice", List.of("USD", "EUR"))).thenReturn("");

        mockMvc.perform(post("/user/alice/editUserAccounts")
                        .param("name", "Alice")
                        .param("birthdate", "2000-01-01")
                        .param("account", "USD")
                        .param("account", "EUR")
                        .with(user("alice"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main"))
                .andExpect(flash().attributeCount(0));
    }
}
