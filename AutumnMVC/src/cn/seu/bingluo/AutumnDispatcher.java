package cn.seu.bingluo;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.seu.bingluo.view.View;

public class AutumnDispatcher implements Servlet {
	private Map<String, HandlerMethod> methods;
	private String resourcePath;
	private ServletContext context;

	@Override
	public void destroy() {
		System.out.println("Autumn MVC is unloaded");
	}

	@Override
	public ServletConfig getServletConfig() {
		return null;
	}

	@Override
	public String getServletInfo() {
		return "BingLuo's Autumn MVC. Coding on 2012/12/28";
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			// 加载@Controller
			String packageName = config.getInitParameter("packageName");
			methods = ControllerLoader.getMethodMapByPackageName(packageName);

			// 初始化静态文件路径
			resourcePath = config.getInitParameter("resource");
			if (resourcePath.endsWith("/*")) {
				resourcePath = resourcePath.substring(0,
						resourcePath.length() - 1);
			}

			HandlerMethod.setView((View) Class.forName(
					config.getInitParameter("view")).newInstance());

			context = config.getServletContext();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		System.out.println("Autumn MVC has been loaded");
	}

	@Override
	public void service(ServletRequest request, ServletResponse response)
			throws ServletException, IOException {
		HttpServletRequest req = (HttpServletRequest) request;
		req.getHeader("Accept");
		response.setContentType(req.getHeader("Accept"));
		if (req.getRequestURI().startsWith(resourcePath)) {
			// 请求为静态资源
			ResourceHandler.getResource((HttpServletRequest) request,
					(HttpServletResponse) response, context,
					req.getRequestURI());
		} else {
			HandlerMethod handlerMethod = getMappedMethod((HttpServletRequest) request);
			if (handlerMethod != null) {
				// 请求servlet
				handlerMethod.excute((HttpServletRequest) request,
						(HttpServletResponse) response);
			} else {
				PrintWriter writer = response.getWriter();
				writer.write("<html>");
				writer.write("<head><title>404 Not Found</title></head>");
				writer.write("<body>");
				writer.write("<h1>404 Not Found</h1>");
				writer.write(req.getRequestURI()
						+ " was not found on this server.");
				writer.write("<p /><hr />");
				writer.write("<small>");
				writer.write(context.getServerInfo());
				writer.write("</small>");
				writer.write("</body></html>");
			}
		}
	}

	/**
	 * 从@Mapping注解的方法中，找到与requestUrl相对应的handler
	 * 
	 * @param request
	 * @return
	 */
	private HandlerMethod getMappedMethod(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String method = request.getMethod();
		System.out.println("Method:" + request.getMethod());
		for (String key : methods.keySet()) {
			key = key.substring(0, key.lastIndexOf(':'));
			String preUri = "";
			if (key.endsWith("/*")) {
				preUri = key.substring(0, key.length() - 1);
			} else {
				preUri = key;
			}
			if (uri.startsWith(preUri)) {
				HandlerMethod handlerMethod = methods.get(key + ':'
						+ method.toUpperCase());
				if (handlerMethod == null) {
					handlerMethod = methods.get(key + ":BOTH");
				}
				return handlerMethod;
			}
		}
		return null;
	}
}
