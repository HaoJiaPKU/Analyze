package com.structure.rule;

import java.io.Serializable;
import java.util.Vector;

public class RuleClass implements Serializable{
	public Vector<RuleUnit> content;
	public RuleClass() {
		content =  new Vector<RuleUnit>();
	}
	
	public Vector<RuleUnit> getContent() {
		return this.content;
	}
	
	public void setContent(Vector<RuleUnit> content) {
		this.content = content;
	}
}
