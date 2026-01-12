package com.lavacro.finances;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class SessionFilter implements Filter {
	@Override
	public void init(final FilterConfig filterConfig) {
		log.info("[init] [SessionFilter]");
	}

	public void doFilter(final ServletRequest req, final ServletResponse resp,
						 final FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) req;
		HttpServletResponse httpResp = (HttpServletResponse) resp;

		HttpSession session = httpReq.getSession(false);
		if(session == null || session.getAttribute("user") == null) {
			log.error("No session");
			httpResp.sendRedirect(httpReq.getContextPath() + "/login.html");	// proxy-aware due to config
			return;
		}

		chain.doFilter(req, resp);
	}


	@Override
	public void destroy() {
		log.info("Destroying filter ...");
	}
}
