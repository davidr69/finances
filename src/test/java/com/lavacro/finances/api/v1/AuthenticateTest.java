package com.lavacro.finances.api.v1;

import com.lavacro.finances.entities.AuthenticatedEntity;
import com.lavacro.finances.entities.RbacUsersEntity;
import com.lavacro.finances.repositories.AuthenticateRepository;

import com.lavacro.finances.repositories.RbacUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateTest {
	@Mock
	private AuthenticateRepository authenticateRepository;

	@Mock
	private RbacUserRepository rbacUserRepository;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Mock
	private HttpSession httpSession;

	@InjectMocks
	private Authenticate authenticate;

	private MockMvc mockMvc;

	@BeforeEach
	void setup() {
		this.mockMvc = MockMvcBuilders.standaloneSetup(authenticate).build();
	}

	@Test
	void testAuth() throws Exception {
		AuthenticatedEntity authenticatedEntity = new AuthenticatedEntity();
		authenticatedEntity.setId(1);
		authenticatedEntity.setAuthenticated(true);
		when(authenticateRepository.getUser("pass", "user")).thenReturn(authenticatedEntity);

		RbacUsersEntity rbacUsersEntity = new RbacUsersEntity();
		rbacUsersEntity.setId(1);
		rbacUsersEntity.setName("user");
		rbacUsersEntity.setPassword("pass");
		when(rbacUserRepository.findById(1)).thenReturn(Optional.of(rbacUsersEntity));

		// Act & Assert
		MockHttpServletResponse resp = mockMvc.perform(
			MockMvcRequestBuilders.post("/authenticate")
				.param("user", "user")
				.param("pass", "pass")
		).andReturn().getResponse();

		Assertions.assertEquals(200, resp.getStatus());
	}
}
