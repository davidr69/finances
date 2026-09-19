package com.lavacro.finances.domain.auth;

import com.lavacro.finances.domain.auth.permission.PermissionService;
import com.lavacro.finances.domain.auth.permission.UserDTO;
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
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateAPITest {
	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private Authentication authentication;

	@Mock
	private PermissionService permissionService;

	@Mock
	private SecurityContextRepository securityContextRepository;

	@Mock
	private AuthenticateService authenticateService;

	@InjectMocks
	private AuthenticateAPI authenticateAPI;

	private MockMvc mockMvc;

	@BeforeEach
	void setup() {
		this.mockMvc = MockMvcBuilders.standaloneSetup(authenticateAPI).build();
	}

	@Test
	void testAuth() throws Exception {
		UserDTO userDTO = new UserDTO(
			1, "user", "$2a$10$encodedPassword", null, null, false, null, null
		);

		when(permissionService.getUserPermissions("user")).thenReturn(userDTO);
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
		doNothing().when(securityContextRepository).saveContext(any(), any(), any());
		doNothing().when(authenticateService).updateLoginTime(any());

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
		UserDTO userDTO = new UserDTO(
			1, "user", "$2a$10$encodedPassword", null, null, false, null, null
		);

		when(permissionService.getUserPermissions("user")).thenReturn(userDTO);

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
		when(permissionService.getUserPermissions("user")).thenReturn(null);

		// Act & Assert
		MockHttpServletResponse resp = mockMvc.perform(
			MockMvcRequestBuilders.post("/authenticate")
				.param("user", "user")
				.param("pass", "pass")
		).andReturn().getResponse();

		Assertions.assertEquals(200, resp.getStatus());
	}
}
