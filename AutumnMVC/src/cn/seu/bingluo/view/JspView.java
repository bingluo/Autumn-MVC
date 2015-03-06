package cn.seu.bingluo.view;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.seu.bingluo.model.ModelAndView;

public class JspView implements View {
	@Override
	public void render(ModelAndView mv, HttpServletRequest request,
			HttpServletResponse response) {
		Map<String, Object> attributes = mv.getAttributes();
		mergeAttributes(attributes, request);
		RequestDispatcher rd = request.getRequestDispatcher(mv.getViewName());
		try {
			rd.forward(request, response);
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void mergeAttributes(Map<String, Object> attributes,
			HttpServletRequest request) {
		for (Entry<String, Object> entry : attributes.entrySet()) {
			request.setAttribute(entry.getKey(), entry.getValue());
		}
	}
}
