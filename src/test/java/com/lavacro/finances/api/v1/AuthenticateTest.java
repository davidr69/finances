package com.lavacro.finances.api.v1;

import com.lavacro.finances.entities.RbacUsersEntity;
import com.lavacro.finances.repositories.AuthenticateRepository;

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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticateTest {
	@Mock
	private AuthenticateRepository authenticateRepository;

	@InjectMocks
	private Authenticate authenticate;

	private MockMvc mockMvc;

	@BeforeEach
	void setup() {
		this.mockMvc = MockMvcBuilders.standaloneSetup(authenticate).build();
	}

	@Test
	void testAuth() throws Exception {

		List<RbacUsersEntity> results = new ArrayList<>();
		RbacUsersEntity rbacUsersEntity = new RbacUsersEntity();
		rbacUsersEntity.setAuthenticated(true);
		results.add(rbacUsersEntity);

		when(authenticateRepository.isAuthenticated("pass", "user")).thenReturn(results);

		// Act & Assert
		MockHttpServletResponse resp = mockMvc.perform(
			MockMvcRequestBuilders.post("/authenticate")
				.param("user", "user")
				.param("pass", "pass")
		).andReturn().getResponse();

		Assertions.assertEquals(200, resp.getStatus());
	}
}
