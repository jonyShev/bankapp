package com.jonyshev.commons.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class UserCreateRequest {
    @NotBlank
    @Size(min = 3, max = 64)
    private String login;
    @NotBlank
    @Size(min = 3, max = 128)
    private String name;
    @NotBlank
    @Size(min = 6, max = 128)
    private String password;
    @NotBlank
    private String birthdate;
}
