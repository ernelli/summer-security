package com.hencjo.summer.security;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.hencjo.summer.security.api.RequestMatcher;

import java.util.Optional;

public final class ServerSideSession {
	private final String sessionAttribute;
	private final Cookies cookies = new Cookies();

	public ServerSideSession(String sessionAttribute) {
		this.sessionAttribute = sessionAttribute;
	}

	public Optional<String> sessionData(HttpServletRequest request) {
		if (request.getSession(false) == null) return Optional.empty();
		Object attribute = request.getSession(false).getAttribute(sessionAttribute);
		if (attribute == null || !(attribute instanceof String)) return Optional.empty();
		return Optional.of((String) attribute);
	}

	public RequestMatcher exists() {
		return new RequestMatcher() {
			@Override
			public boolean matches(HttpServletRequest request) {
				return sessionData(request).isPresent();
			}

			@Override
			public String describer() {
				return "ServerSideSession";
			}
		};
	}

	public SessionWriter sessionWriter() {
		return new SessionWriter() {
			@Override
			public void startSession(HttpServletRequest request, HttpServletResponse response, String username) {
				request.getSession(true).setAttribute(sessionAttribute, username);
			}

			@Override
			public void stopSession(HttpServletRequest request, HttpServletResponse response) {
				request.getSession(true).invalidate();
				for (Cookie cookie : cookies.withName(request.getCookies(), "JSESSIONID"))
					Cookies.setCookie(response, cookies.removeCookie(cookie.getName(), cookie.getPath()));
			}
		};
	}
}