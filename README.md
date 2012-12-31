# Summer Security

Summer Security is a library/DSL for Java Web Applications that need a simpler authentication mechanism. 

It's design goals are:
* Only authentication. Not authorization. Authorization is generally too domain specific to warrant using a library.
* Minimalistic dependencies. Summer Security don't clutter your classpath. And it doesn't depend on any logging framework. The only dependency is the Servlet API.
* Configuration in code, not XML or Annotations. 
* Simple to extend with own Request Mathers and Responders.
* Summer Security does not use ThreadLocal to store the logged in user, but the HttpServletRequest.

Example:

```java
    import java.io.IOException;
  	import javax.servlet.*;
  	import com.hencjo.summer.*;
  	import com.hencjo.summer.api.*;
  	import static com.hencjo.summer.api.DSL.*;
  
  	public class AuthenticationFilter extends AbstractFilter {
  		private final Authenticator authenticator = Authenticators.allowEveryoneAuthenticator();
  		private final ServerSideSession session = new ServerSideSession("JSESSIONID");
  		private final HttpBasicAuthenticator httpBasicAuthenticator = new HttpBasicAuthenticator(authenticator);
  		private final FormBasedLogin formBasedLogin = new FormBasedLogin(authenticator, session.sessionWriter(), 
  				"/j_spring_security_check", "/j_spring_security_logout", 
  				"j_username", "j_password", 
  				redirect("/login.html#?logout=true"), redirect("/login.html#?failure=true"), redirect("/index.html"));
  
  		private final Summer summer = new Summer(
  				when(pathEquals("/")).thenAllow(),
  				when(pathBeginsWith("/img/")).thenAllow(),
  				when(pathBeginsWith("/lib/")).thenAllow(),
  				when(pathEquals("/login.html")).thenAllow(),
  				when(pathEquals("/login.js")).thenAllow(),
  				when(formBasedLogin.logoutRequest()).then(formBasedLogin.performLogoutRequest()),
  				when(session.exists()).thenAllow(),
  				when(formBasedLogin.loginRequest()).then(formBasedLogin.performLoginRequest()),
  				when(httpBasicAuthenticator.authorizes()).thenAllow(),
  				when(header("X-Requested-With").equals("XMLHttpRequest")).then(status(403)),
  				otherwise().then(redirect("/login.html"))
  			);
  		
  		@Override
  		public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
  			summer.doFilter(request, response, filterChain);
  		}
  	}
```
