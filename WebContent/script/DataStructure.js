/**
 * javascript 创建数据结构
 * 
 * 
 * @returns
 */
function RuleClass() {
	this.content = new Array();
}
function RuleUnit(name) {
	this.name = name;
	this.value = "";
	this.choose = true;
	this.path = new Array();
}
function StructUnitC(index, tagName) {
	this.index = index;
	this.tagName = tagName;
	this.keys = new Array();
}