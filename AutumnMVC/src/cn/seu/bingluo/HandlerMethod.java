package cn.seu.bingluo;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.seu.bingluo.annotation.FormEntity;
import cn.seu.bingluo.annotation.Mapping;
import cn.seu.bingluo.annotation.RequestParam;
import cn.seu.bingluo.model.ModelAndView;
import cn.seu.bingluo.view.View;

public class HandlerMethod {
	private String urlMapping = "";
	private String requestMethod = "";
	private Method handlerMethod = null;
	private Object handlerObject = null;
	private static View view = null;

	public static void setView(View view) {
		HandlerMethod.view = view;
	}

	public HandlerMethod(Method handlerMethod, Object handlerObject) {
		this.handlerMethod = handlerMethod;
		this.handlerObject = handlerObject;

		Mapping mapping = (Mapping) handlerMethod.getAnnotation(Mapping.class);
		urlMapping = mapping.value();
		requestMethod = mapping.method().toUpperCase();
	}

	/**
	 * 执行hanlder方法
	 * 
	 * @param request
	 * @param response
	 */
	public void excute(HttpServletRequest request, HttpServletResponse response) {
		try {
			Class<?>[] parameterTypes = handlerMethod.getParameterTypes();
			Object[] parameters = new Object[parameterTypes.length];
			for (int i = 0; i < parameterTypes.length; i++) {
				String typeName = parameterTypes[i].getName();
				if (typeName.equals("javax.servlet.ServletRequest")
						|| typeName
								.equals("javax.servlet.http.HttpServletRequest")) {
					parameters[i] = request;
				} else if (typeName.equals("javax.servlet.ServletResponse")
						|| typeName
								.equals("javax.servlet.http.HttpServletResponse")) {
					parameters[i] = response;
				} else if (typeName.equals("javax.servlet.http.HttpSession")) {
					parameters[i] = request.getSession();
				} else if (getTheAnnotation(
						handlerMethod.getParameterAnnotations()[i],
						FormEntity.class) != null) {
					// 参数为@FormEntity注解
					Object object = boxFormEntity(request, parameterTypes[i]);
					parameters[i] = object;
				} else if (getTheAnnotation(
						handlerMethod.getParameterAnnotations()[i],
						RequestParam.class) != null) {
					// 参数为@RequestParam注解
					RequestParam requestParam = (RequestParam) getTheAnnotation(
							handlerMethod.getParameterAnnotations()[i],
							RequestParam.class);
					Object object = boxRequestParameter(request, requestParam,
							parameterTypes[i]);
					parameters[i] = object;
				}
			}

			try {
				Object returnVal = handlerMethod.invoke(handlerObject,
						parameters);
				if (handlerMethod.getReturnType() == String.class) {
					ModelAndView mv = new ModelAndView((String) returnVal);
					view.render(mv, request, response);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据request封装FormEntity
	 * 
	 * @param request
	 * @param clazz
	 * @return
	 */
	private Object boxFormEntity(HttpServletRequest request, Class<?> clazz) {
		Object object = null;
		try {
			object = clazz.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		for (Method method : clazz.getMethods()) {
			String methodName = method.getName();
			if (methodName.startsWith("set")
					&& method.getParameterTypes().length == 1) {
				String variableName = methodName.substring(3, 4).toLowerCase();
				variableName = variableName.concat(methodName.substring(4));
				String variableString = request.getParameter(variableName);
				if (variableString == null) {
					continue;
				}
				Object variable = null;
				try {
					variable = method.getParameterTypes()[0].getConstructor(
							String.class).newInstance(variableString);
					method.invoke(object, variable);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
			}
		}
		return object;
	}

	/**
	 * 封装请求参数
	 * 
	 * @param request
	 * @param requestParam
	 * @param parameterType
	 * @return
	 */
	private Object boxRequestParameter(ServletRequest request,
			RequestParam requestParam, Class<?> parameterType) {
		Object object = null;
		String param = request.getParameter(requestParam.parameterName());
		if (param == null) {
			return object;
		}
		try {
			object = parameterType.getConstructor(String.class).newInstance(
					param);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return object;
	}

	/**
	 * 判断是否注解实例中特定注解
	 * 
	 * @param annotations
	 * @param clazz
	 * @return
	 */
	private Annotation getTheAnnotation(Annotation[] annotations, Class<?> clazz) {
		for (Annotation annotation : annotations) {
			if (annotation.annotationType() == clazz) {
				return annotation;
			}
		}
		return null;
	}

	public String getUrlMapping() {
		return urlMapping;
	}

	public void setUrlMapping(String urlMapping) {
		this.urlMapping = urlMapping;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

}
