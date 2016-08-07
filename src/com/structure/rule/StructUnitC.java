package com.structure.rule;

import java.util.Vector;

//structUnit结构体主要是用来存放每一层所对应的位置，标记和必须包含的关键字
public class StructUnitC {
	public int index;
	public String tagName;
	public Vector<String> keys;
	public StructUnitC() {
		keys = new Vector<String>();
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public void setKeys(Vector<String> keys) {
		this.keys = keys;
	}
}