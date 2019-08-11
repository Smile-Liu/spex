package org.spex.beans.factory;

import java.util.LinkedList;
import java.util.List;

public class BeanCreationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private List<Throwable> relatedCauses;
	
	public BeanCreationException(String msg) {
		super(msg);
	}
	
	public BeanCreationException(String msg, Throwable e) {
		super(msg, e);
	}
	
	public BeanCreationException(String beanName, String msg) {
		super("´´½¨ " + beanName + " Ê§°Ü£º" + msg);
	}

	public BeanCreationException(String beanName, String msg, Throwable e) {
		this(beanName, msg);
		initCause(e);
	}
	
	public void addRelatedCauses(Throwable ex) {
		if (this.relatedCauses == null) {
			this.relatedCauses = new LinkedList<Throwable>();
		}
		this.relatedCauses.add(ex);
	}
	
	public Throwable[] getRelatedCauses() {
		if (this.relatedCauses == null) {
			return null;
		}
		return this.relatedCauses.toArray(new Throwable[this.relatedCauses.size()]);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(super.toString());
		if (this.relatedCauses != null) {
			for (Throwable cause : this.relatedCauses) {
				sb.append("\nRelatedCause: ");
				sb.append(cause);
			}
		}
		return sb.toString();
	}
	
	
}
