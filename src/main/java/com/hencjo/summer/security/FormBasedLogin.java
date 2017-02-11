package com.hencjo.summer.security;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.hencjo.summer.security.api.*;
import com.hencjo.summer.security.api.CredentialsAuthenticator.Credentials;
import com.hencjo.summer.security.utils.HttpServletRequests;

public final class FormBasedLogin {
	private final SummerLogger summerLogger;
	private final CredentialsAuthenticator authenticator;
	private final SessionWriter sessionWriter;

	private final String loginUrl;
	private final String logoutUrl;
	private final String usernameParameter;
	private final String passwordParameter;
	private final Responder loggedOut;
	private final Responder loginFailure;
	private final Responder loginSuccess;

	
	public FormBasedLogin(SummerLogger summerLogger, CredentialsAuthenticator authenticator, SessionWriter sessionWriter, String loginUrl, String logoutUrl, String usernameParameter, String passwordParameter, Responder loggedOut, Responder loginFailure, Responder loginSuccess) {
		this.summerLogger = summerLogger;
		this.authenticator = authenticator;
		this.sessionWriter = sessionWriter;
		this.loginUrl = loginUrl;
		this.logoutUrl = logoutUrl;
		this.usernameParameter = usernameParameter;
		this.passwordParameter = passwordParameter;
		this.loggedOut = loggedOut;
		this.loginFailure = loginFailure;
		this.loginSuccess = loginSuccess;
	}
	
	public RequestMatcher loginRequest() {
		return new RequestMatcher() {
			@Override
			public boolean matches(HttpServletRequest request) {
				return ("POST".equals(request.getMethod()) && HttpServletRequests.path(request).equals(loginUrl));
			}
			
			@Override
			public String describer() {
				return "FormBasedLoginRequest";
			}
		};
	}
	
	public RequestMatcher logoutRequest() {
		return new RequestMatcher() {
			@Override
			public boolean matches(HttpServletRequest request) {
				return HttpServletRequests.path(request).equals(logoutUrl);
			}
			
			@Override
			public String describer() {
				return "FormBasedLogoutRequest";
			}
		};
	}

	public Responder performLoginRequest() {
		return new Responder() {
			@Override
			public ContinueOrRespond respond(HttpServletRequest request, HttpServletResponse response) throws IOException {
				Optional<Credentials> credentialsOptional = credentials(request);

				if (!credentialsOptional.isPresent()) {
					loginFailure.respond(request, response);
					return ContinueOrRespond.RESPOND;
				}

				Credentials credentials = credentialsOptional.get();

				Optional<String> authenticate = authenticator.authenticate(credentials);
				if (!authenticate.isPresent()) {
					summerLogger.info("FormBasedLogin. Authentication failed for user \"" + credentials.username + "\".");
					loginFailure.respond(request, response);
					return ContinueOrRespond.RESPOND;
				}

				sessionWriter.startSession(request, response, credentials.username);
				loginSuccess.respond(request, response);
				summerLogger.info("FormBasedLogin. Authentication successful for user \"" + credentials.username + "\".");
				return ContinueOrRespond.RESPOND;
			}


			@Override
			public String describer() {
				return "FormBasedLogin.PerformLoginRequest";
			}
		};
	}

	public Optional<Credentials> credentials(HttpServletRequest request) {
		String username = request.getParameter(usernameParameter);
		String password = request.getParameter(passwordParameter);
		if (username == null || password == null) return Optional.empty();
		return Optional.of(new Credentials(username, password));
	}

	public Responder performLogoutRequest() {
		return new Responder() {
			@Override
			public ContinueOrRespond respond(HttpServletRequest request, HttpServletResponse response) throws IOException {
				summerLogger.info("FormBasedLogin. Logout");
				sessionWriter.stopSession(request, response);
				loggedOut.respond(request, response);
				return ContinueOrRespond.RESPOND;
			}

			@Override
			public String describer() {
				return "FormBasedLogin.PerformLogoutRequest";
			}
		};
	}
}
