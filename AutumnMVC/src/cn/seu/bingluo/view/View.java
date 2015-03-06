package cn.seu.bingluo.view;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.seu.bingluo.model.ModelAndView;

public interface View {
	void render(ModelAndView mv, HttpServletRequest request,
			HttpServletResponse response);
}
