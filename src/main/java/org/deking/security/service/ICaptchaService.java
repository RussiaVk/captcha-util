package org.deking.security.service;

import java.io.IOException;
import java.io.OutputStream;

public interface ICaptchaService {

	String create(OutputStream outputStream) throws IOException;
}
