package com.ricardocervo.booknblock.auth;


import com.ricardocervo.booknblock.security.JwtService;
import com.ricardocervo.booknblock.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final UserRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	private final AuthenticationManager authenticationManager;


	public AuthenticationResponse authenticate(AuthenticationRequest request) {

		// I'm totally secure that if this method doesn't throw an exception, the user is
		// authenticated
		try {
			authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
		} catch (Exception e) {
			System.out.println("Error authenticating user: ");
			e.printStackTrace();
			throw e;
		}


		var user = repository.findByEmail(request.getEmail()).orElseThrow();
		var token = jwtService.generateToken(user);
		return AuthenticationResponse.builder().token(token)
				.build();
	}

}
