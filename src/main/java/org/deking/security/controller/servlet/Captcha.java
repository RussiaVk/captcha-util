package org.deking.security.controller.servlet;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.deking.security.resolver.CaptchaKeyResolver;
import org.deking.security.service.impl.CaptchaServiceImpl;

@SuppressWarnings("serial")
@WebServlet("/captcha")
public final class Captcha extends HttpServlet {
	private String captchaAttribute;

	@Override
	public void init(ServletConfig filterConfig) {
		captchaAttribute = Optional.ofNullable(filterConfig.getServletContext().getInitParameter("captchaAttribute"))
				.orElseGet(() -> "piccode");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setHeader("content-type", "image/jpeg");
		response.setDateHeader("expries", -1);
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
		try (ServletOutputStream servletOutputStream = response.getOutputStream()) {
			HttpSession session = request.getSession();
			String captcha = CaptchaServiceImpl.getInstance().create(servletOutputStream);
			session.setAttribute(captchaAttribute, captcha);
			request.getServletContext().setAttribute( 
					CaptchaKeyResolver.getInstance().apply(request.getRemoteAddr(), captchaAttribute)
					, captcha);
			servletOutputStream.flush();
			
		}

	}

}
