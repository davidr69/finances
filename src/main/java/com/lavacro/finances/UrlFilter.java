package com.lavacro.finances;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UrlFilter {
	@Bean
	public FilterRegistrationBean<SessionFilter> myFilterBean() {
		final FilterRegistrationBean<SessionFilter> filterBean = new FilterRegistrationBean<>();
		Collection<String> urls = new ArrayList<>();
		urls.add("/");
		urls.add("/home");
		urls.add("/nav");
		urls.add("/reports/byEntity");
		urls.add("/reports/summaryByYear");
		urls.add("/spreadsheet");
		urls.add("/transaction");
		urls.add("/api/*");
		filterBean.setFilter(new SessionFilter());
		filterBean.setUrlPatterns(urls);
		filterBean.setEnabled(Boolean.TRUE);
		filterBean.setName("Session filter");
		filterBean.setOrder(1);
		filterBean.setAsyncSupported(Boolean.TRUE);
		return filterBean;
	}
}
