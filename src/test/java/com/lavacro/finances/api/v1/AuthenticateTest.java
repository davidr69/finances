package com.lavacro.finances.api.v1;

import com.lavacro.finances.entities.RbacUsersEntity;
import com.lavacro.finances.repositories.RbacUserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateTest {
	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private RbacUserRepository userRepository;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private Authenticate authenticate;

	private MockMvc mockMvc;

	@BeforeEach
	void setup() {
		this.mockMvc = MockMvcBuilders.standaloneSetup(authenticate).build();
	}

	@Test
	void testAuth() throws Exception {
		RbacUsersEntity rbacUsersEntity = new RbacUsersEntity();
		rbacUsersEntity.setId(1);
		rbacUsersEntity.setName("user");
		rbacUsersEntity.setPassword("$2a$10$encodedPassword");
		rbacUsersEntity.setLocked(false);
		rbacUsersEntity.setLoginAttempts(null);

		when(userRepository.findByName("user")).thenReturn(Optional.of(rbacUsersEntity));
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

		// Act & Assert
		MockHttpServletResponse resp = mockMvc.perform(
			MockMvcRequestBuilders.post("/authenticate")
				.param("user", "user")
				.param("pass", "pass")
		).andReturn().getResponse();

		Assertions.assertEquals(200, resp.getStatus());
	}

	@Test
	void testAuthFailure() throws Exception {
		RbacUsersEntity rbacUsersEntity = new RbacUsersEntity();
		rbacUsersEntity.setId(1);
		rbacUsersEntity.setName("user");
		rbacUsersEntity.setPassword("$2a$10$encodedPassword");
		rbacUsersEntity.setLocked(false);
		rbacUsersEntity.setLoginAttempts(0);

		when(userRepository.findByName("user")).thenReturn(Optional.of(rbacUsersEntity));
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
			.thenThrow(new BadCredentialsException("Bad credentials"));

		// Act & Assert
		MockHttpServletResponse resp = mockMvc.perform(
			MockMvcRequestBuilders.post("/authenticate")
				.param("user", "user")
				.param("pass", "wrongpass")
		).andReturn().getResponse();

		Assertions.assertEquals(200, resp.getStatus());
	}

	@Test
	void testAuthUserNotFound() throws Exception {
		when(userRepository.findByName("user")).thenReturn(Optional.empty());

		// Act & Assert
		MockHttpServletResponse resp = mockMvc.perform(
			MockMvcRequestBuilders.post("/authenticate")
				.param("user", "user")
				.param("pass", "pass")
		).andReturn().getResponse();

		Assertions.assertEquals(200, resp.getStatus());
	}
}
