package org.spex.test.all;

import java.util.List;

public class TestPropertyBean {

	private String name;
	
	private List<String> parentNames;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getParentNames() {
		return parentNames;
	}

	public void setParentNames(List<String> parentNames) {
		this.parentNames = parentNames;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		if (parentNames != null) {
			for (int i = 0; i < parentNames.size(); i++) {
				sb.append(parentNames.get(i));
				if (i < parentNames.size() - 1) {
					sb.append(", ");
				} else {
					sb.append("]");
				}
			}
		}
		return "TestPropertyBean [name=" + name + ", parentNames=" + sb.toString() + "]";
	}
	
	
}
