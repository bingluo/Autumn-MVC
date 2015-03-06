package cn.seu.bingluo.model;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
	private String viewName = "";

	private Map<String, Object> attributes = new HashMap<String, Object>();

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName.startsWith("/") ? viewName : "/" + viewName;
	}

	public ModelAndView() {
	}

	public ModelAndView(String viewName) {
		setViewName(viewName);
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	public String[] getAttributeNames() {
		return (String[]) attributes.keySet().toArray();
	}

	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	public void removeAttribute(String key) {
		attributes.remove(key);
	}
}
