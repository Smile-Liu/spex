package org.spex.test.all;

import java.util.List;
import java.util.Map;

public class TestBean {

	private String name;
	
	private TestPropertyBean he;
	
	private List<TestPropertyBean> children;
	
	private Map<String, TestPropertyBean> childrenMap;


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public TestPropertyBean getHe() {
		return he;
	}

	public void setHe(TestPropertyBean he) {
		this.he = he;
	}

	public List<TestPropertyBean> getChildren() {
		return children;
	}

	public void setChildren(List<TestPropertyBean> children) {
		this.children = children;
	}

	public Map<String, TestPropertyBean> getChildrenMap() {
		return childrenMap;
	}

	public void setChildrenMap(Map<String, TestPropertyBean> childrenMap) {
		this.childrenMap = childrenMap;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				sb.append(children.get(i));
				if (i < children.size() - 1) {
					sb.append(", ");
				} else {
					sb.append("]");
				}
			}
		}
		
		return "TestBean [name=" + name + ", he=" + he
				+ ", children=" + sb.toString() + ", childrenMap=" + childrenMap
				+ "]";
	}
	
}
