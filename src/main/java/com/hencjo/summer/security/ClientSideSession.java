package com.hencjo.summer.security;

import com.hencjo.summer.security.api.RequestMatcher;
import com.hencjo.summer.security.api.Responder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ClientSideSession {
	private final String cookieName;
	private final int expiresInSeconds;
	private final SecureCookies secureCookies;

	public ClientSideSession(
		String cookieName,
		int expiresInSeconds,
		SecureCookies secureCookies
	) {
		this.cookieName = cookieName;
		this.expiresInSeconds = expiresInSeconds;
		this.secureCookies = secureCookies;
	}

	public Responder renewSessionContinue() {
		return new Responder() {
			@Override
			public ContinueOrRespond respond(
				HttpServletRequest request,
				HttpServletResponse response
			) throws IOException {
				Optional<String> s = sessionData(request);
				if (!s.isPresent())
					throw new IllegalStateException("About to renew Session. But found no Session Data in Request.");

				sessionWriter().startSession(request, response, s.get());
				return ContinueOrRespond.CONTINUE;
			}

			@Override
			public String describer() {
				return "RenewSessionAndAllow";
			}
		};
	}

	public Optional<String> sessionData(HttpServletRequest request)  {
		for (Cookie c : cookiesWithName(request, cookieName))
			return secureCookies.cookieValue(c);
		return Optional.empty();
	}

	private static List<Cookie> cookiesWithName(HttpServletRequest request, String cookieName) {
		return Cookies.cookies(request)
			.stream()
			.filter(x -> cookieName.equals(x.getName()))
			.collect(Collectors.toList());
	}

	public RequestMatcher exists() {
		return new RequestMatcher() {
			@Override
			public boolean matches(HttpServletRequest request) {
				return sessionData(request) != null;
			}
			
			@Override
			public String describer() {
				return "ClientSideSession";
			}
		};
	}
	
	public SessionWriter sessionWriter() {
		return new SessionWriter() {
			@Override
			public void startSession(HttpServletRequest request, HttpServletResponse response, String username) {
				secureCookies.setCookie(request, response, cookieName, username, expiresInSeconds);
			}
			
			@Override
			public void stopSession(HttpServletRequest request, HttpServletResponse response) {
				secureCookies.delete(request, response, cookieName);
			}
		};
	}
}