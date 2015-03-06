package cn.seu.bingluo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.seu.bingluo.annotation.Controller;
import cn.seu.bingluo.annotation.Mapping;

public class ControllerLoader {
	/**
	 * 根据包名，返回其下所有注解为@Controller的类（递归查找）
	 * 
	 * @param packageName
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws ClassNotFoundException
	 */
	public static Map<String, HandlerMethod> getMethodMapByPackageName(
			String packageName) throws IOException, URISyntaxException,
			ClassNotFoundException {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();

		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.toURI().getPath()));
		}

		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		for (File directory : dirs) {
			classes.addAll(findControllers(directory, packageName));
		}

		Map<String, HandlerMethod> map = new HashMap<String, HandlerMethod>();
		// 找出Controller类中每一个Mapping方法
		for (Class<?> clazz : classes) {
			Object obj = null;
			try {
				obj = clazz.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(Mapping.class)) {
					Mapping mapping = method.getAnnotation(Mapping.class);
					HandlerMethod handlerMethod = new HandlerMethod(method, obj);
					map.put(handlerMethod.getUrlMapping() + ':'
							+ mapping.method().toUpperCase(), handlerMethod);
				}
			}
		}
		return map;
	}

	/**
	 * 递归查找所有@Controller
	 * 
	 * @param directory
	 * @param packageName
	 * @return
	 * @throws ClassNotFoundException
	 */
	private static List<Class<?>> findControllers(File directory,
			String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		if (!directory.exists()) {
			return classes;
		}

		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				// 递归查找文件夹下面的所有文件
				assert !file.getName().contains(".");
				classes.addAll(findControllers(file,
						packageName + '.' + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				// 递归终点
				Class<?> clazz = Class.forName(packageName
						+ "."
						+ file.getName().substring(0,
								file.getName().length() - 6));
				if (clazz.isAnnotationPresent(Controller.class)) {
					classes.add(clazz);
				}
			}
		}
		return classes;
	}
}
