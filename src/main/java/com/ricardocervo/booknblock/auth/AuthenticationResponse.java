package com.ricardocervo.booknblock.auth;

import com.ricardocervo.booknblock.user.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
	private UserDto user;
	private String token;
	

}
