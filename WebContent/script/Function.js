/**

*/
function getXPath(element) {
	var ruleUnit = new RuleUnit("default");
	ruleUnit.value = element.innerText;
    ruleUnit.path.push(getXPos(element).node); 
    var current = element.parentNode; 
    while (current != null && current.tagName != 'BODY') {
    	ruleUnit.path.push(getXPos(current).node);
    	current = current.parentNode;
	}
    ruleUnit.path.reverse();
	return ruleUnit;
};
function getXPos(element) {
	var tagName = element.tagName;
	var index = 0;
	var current = element.previousSibling;
	while (current != null) {
		if (current.tagName == tagName) {
			index ++;
		}
		current = current.previousSibling;
	}
	var node = new StructUnitC(index, tagName);
	return {
		node:node
	};
};
var lastShow;
var lastShowBackgroundColor;
function showChild(path, seq) {
	father = $('body');
	console.log(father);
	//展示其中的ruleUnit的seq部分
	for (var i=0; i<=seq;i++) {
		//逐步深入选取到对应的元素
		father = $(father).children(path[i].tagName)[path[i].index];
		console.log(father);
	}
	hideChild();
	lastShow = father;
	lastShowBackgroundColor = lastShow.style.backgroundColor;
	lastShow.style.backgroundColor = "#ff0000";
};
function hideChild() {
	if (lastShow != null) lastShow.style.backgroundColor = lastShowBackgroundColor;
};
