package cn.seu.bingluo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResourceHandler {
	private static int BUFFER_SIZE = 1024;

	/**
	 * 读取静态资源并发送给客户端
	 * 
	 * @param response
	 * @param context
	 * @param uri
	 * @throws IOException
	 */
	public static void getResource(HttpServletRequest request,
			HttpServletResponse response, ServletContext context, String uri)
			throws IOException {
		BufferedOutputStream out = null;
		try {
			out = new BufferedOutputStream(response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (out == null) {
			return;
		}

		BufferedInputStream in = new BufferedInputStream(
				context.getResourceAsStream(uri));

		byte[] bytes = new byte[BUFFER_SIZE];
		try {
			int flag = in.read(bytes, 0, BUFFER_SIZE);
			while (flag != -1) {
				out.write(bytes);
				out.flush();
				flag = in.read(bytes, 0, BUFFER_SIZE);
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}
}
