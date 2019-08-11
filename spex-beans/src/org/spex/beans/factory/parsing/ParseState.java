package org.spex.beans.factory.parsing;

import java.util.Stack;

public final class ParseState {

	private final char TAB = '\t';
	private final Stack<Entry> stack;
	
	public ParseState() {
		this.stack = new Stack<Entry>();
	}
	
	@SuppressWarnings("unchecked")
	public ParseState(ParseState other) {
		this.stack = (Stack<Entry>) other.stack.clone();
	}
	
	public void push(Entry entry) {
		this.stack.push(entry);
	}
	
	public Entry pop() {
		return this.stack.pop();
	}
	
	public Entry peek() {
		return this.stack.empty() ? null : this.stack.peek();
	}
	
	public ParseState snapshot() {
		return new ParseState(this);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < this.stack.size(); x++) {
			if (x > 0) {
				sb.append('\n');
				for (int y = 0; y < x; y++) {
					sb.append(TAB);
				}
				sb.append("--> ");
			}
			sb.append(this.stack.get(x));
		}
		return sb.toString();
	}
	
	interface Entry {
		
	}
}
