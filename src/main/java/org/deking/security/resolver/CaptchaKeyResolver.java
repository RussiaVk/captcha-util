package org.deking.security.resolver;

import java.util.function.BiFunction;

public class CaptchaKeyResolver implements BiFunction<String, String, String> {
	private static CaptchaKeyResolver instance;

	public static CaptchaKeyResolver getInstance() {
		if (instance == null)
			instance = new CaptchaKeyResolver();
		return instance;
	}

	@Override
	public String apply(String ip, String attribute) {
		return ip + '-' + attribute;
	}

}
