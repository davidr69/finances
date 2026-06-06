package com.lavacro.finances.config;

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
		urls.add("/cashbook");
		urls.add("/entities");
		urls.add("/logout");
		urls.add("/nav");
		urls.add("/reportBalanceSheet");
		urls.add("/reportByEntity");
		urls.add("/reportSummaryByYear");
		urls.add("/reportWeekly");
		urls.add("/transaction");
		urls.add("/upload");
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
