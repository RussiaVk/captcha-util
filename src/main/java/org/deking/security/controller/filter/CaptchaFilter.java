package org.deking.security.controller.filter;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BiPredicate;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.deking.security.resolver.CaptchaKeyResolver;

@SuppressWarnings("serial")
public class CaptchaFilter extends HttpFilter implements BiPredicate<String, String>, Filter {
	private String captchaParameter, captchaAttribute;
	private boolean caseSensitive;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		captchaParameter = Optional.ofNullable(filterConfig.getInitParameter("captchaParameter"))
				.orElseGet(() -> "captcha");
		captchaAttribute = Optional.ofNullable(filterConfig.getServletContext().getInitParameter("captchaAttribute"))
				.orElseGet(() -> "piccode");
		caseSensitive = Optional.ofNullable(Boolean.valueOf((String) filterConfig.getInitParameter("caseSensitive")))
				.orElseGet(() -> false);

	}

	@Override
	public boolean test(String captchaCache, String captcha) {
		return caseSensitive ? captchaCache.equals(captcha) : captchaCache.equalsIgnoreCase(captcha);
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletResponse httpResponse = ((HttpServletResponse) response);
		Optional<String> captcha = Optional.ofNullable(request.getParameter(captchaParameter));
		if (captcha.isPresent()) {
			boolean result;
			Optional<String> captchaCache;
			Optional<HttpSession> session = Optional.ofNullable(((HttpServletRequest) request).getSession(false));
			String key = null;
			if (session.isPresent()) {
				captchaCache = Optional.ofNullable((String) session.get().getAttribute(captchaAttribute));
			} else {
				key = CaptchaKeyResolver.getInstance().apply(request.getRemoteAddr(), captchaAttribute);
				captchaCache = Optional.ofNullable((String) request.getServletContext().getAttribute(key));
			}
			if (captchaCache.isPresent()) {
				result = test(captchaCache.get(), captcha.get());
				if (result) {
					if (session.isPresent()) {
						session.get().invalidate();
					} else {
						request.getServletContext().removeAttribute(key);
					}
					chain.doFilter(request, response);
				} else {
					httpResponse.sendError(403, "Invalid captcha!");
				}
			} else {
				httpResponse.sendError(403, "Captcha not send yet or expired!");
			}
		} else {
			httpResponse.sendError(403, "The captcha parameter is missing!");
		}
	}

}
