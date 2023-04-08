package org.deking.security.service.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import javax.imageio.ImageIO;

import org.deking.security.service.ICaptchaService;

public final class CaptchaServiceImpl implements ICaptchaService {
	private static final char[] CHARS;
	private static final short HIGHT, WIDTH, CHAR_LEN;
	private static final Font FONT;
	private static final String FORMAT;
	static {
		CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
		CHAR_LEN = 4;
		HIGHT = 40;
		WIDTH = 120;
		FONT = new Font("Times New Roman", Font.BOLD, WIDTH / CHAR_LEN);
		FORMAT = "jpg";
	}
	private static CaptchaServiceImpl instance;

	public static CaptchaServiceImpl getInstance() {
		if (instance == null) {
			instance = new CaptchaServiceImpl();
		}
		return instance;
	}

	private static BufferedImage smoothing(BufferedImage bi) {
		float[] elements = { 0.11111f, 0.11111f, 0.11111f, 0.11111f, 0.11111f, 0.11111f, 0.11111f, 0.11111f, 0.11111f };
		Kernel kernel = new Kernel(3, 3, elements);
		ConvolveOp cop = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
		BufferedImage bi2 = new BufferedImage(WIDTH, HIGHT, BufferedImage.TYPE_INT_RGB);
		cop.filter(bi, bi2);
		return bi2;
	}

	public String create(OutputStream outputStream) throws IOException {
		BufferedImage bi = new BufferedImage(WIDTH, HIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		Color c = Color.WHITE;
		g.setColor(c);
		g.fillRect(0, 0, WIDTH, HIGHT);
		g.setFont(FONT);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		Random r = new Random();
		int len = CHARS.length, index;
		StringBuffer sb = new StringBuffer();
		int lastX = 0;
		for (int i = 1; i < CHAR_LEN + 1; i++) {
			index = r.nextInt(len);
			int angdeg = i / 2 == 0 ? -r.nextInt(10) : r.nextInt(10);
			double radis = Math.toRadians(angdeg);
			int x = i == 1 ? WIDTH / CHAR_LEN : lastX + r.nextInt(FONT.getSize() / 4, FONT.getSize());
			lastX = x;
			int y = HIGHT / 2 + angdeg;
			g.rotate(radis, x, y);
			g.setColor(Color.BLACK);
			g.drawString(CHARS[index] + "", x, y);
			if (i == 4) {
				g.drawLine(x, y, x + r.nextInt(10), y + r.nextInt(10));
			}
			sb.append(CHARS[index]);
		}
		BufferedImage bi2 = smoothing(bi);
		ImageIO.write(bi2, FORMAT, outputStream);
		return sb.toString();
	}

	public static void main(String[] args) throws IOException {
		FileOutputStream file = new FileOutputStream("D://test.jpg");
		String captcha = CaptchaServiceImpl.getInstance().create(file);
		System.out.println(captcha);
	}
}
