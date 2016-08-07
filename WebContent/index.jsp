<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>网页格式提取</title>
<script src="script/jquery-2.0.3.js" type="text/javascript"></script>
<script src="script/DataStructure.js" type="text/javascript"></script>
<script src="script/Function.js" type="text/javascript"></script>
<script>
function add() {
	var doc = document.getElementById("frame").contentDocument;
	//加入jquery包
	var jq = document.createElement("script");
	jq.src = "script/jquery-2.0.3.js";
	jq.type="text/javascript";
	doc.head.appendChild(jq);
	//加入数据结构
	var ds = document.createElement("script");
	ds.src = "script/DataStructure.js";
	ds.type="text/javascript";
	doc.head.appendChild(ds);
	//加入功能函数
	var fc = document.createElement("script");
	fc.src = "script/Function.js";
	fc.type="text/javascript";
	doc.head.appendChild(fc);
	//加入删除所有链接的选项
	var disa = document.createElement("script");
	disa.text = "$('a').removeAttr('href');";
	doc.head.appendChild(disa);
	//嵌入那些必须在页面的代码
	var script1 = document.createElement("script");
	script1.text = 
	"     var last;"+
	"     var lastBackgroundColor;"+
	"    $(document).on('mousemove',function(e){"+
	"		 hideChild();"+
	"        var current = e.target;"+
	"        if (current != 'DOCUMENT' && current.tagName != 'HTML' && current.className != 'overlayStyle' && current.tagName != 'BODY') {"+
	"           if (last != null) last.style.backgroundColor = lastBackgroundColor;"+
	"          last = current;"+
	"           lastBackgroundColor = current.style.backgroundColor;"+
	"           current.style.backgroundColor = '#00ffff';"+
	"            e.stopPropagation();"+
	"        }"+
	"    });"+
	"    $(document).on('click', function(e){"+
	"		  hideChild();"+
	"         var current = e.target;"+
	"         if (current != 'DOCUMENT' && current.tagName != 'HTML' && current.className != 'overlayStyle' && current.tagName != 'BODY') {"+
	"            var ruleUnit = getXPath(current);"+
	"            console.log(ruleUnit);"+
	"			 window.parent.addPath(ruleUnit)	"+					
	"         }"+
	"     });";
	doc.head.appendChild(script1);
}
function load() {
	var url = document.getElementById("url").value;
	console.log(url);
	$.ajax({
		url: "/Analyze/AnalyzeHtml",
		data: {
			url: url,
			method: "load",
			type: 1
		},
		type : "POST",
		async : false,
		dataType : "json",
		contentType:'application/x-www-form-urlencoded; charset=utf-8',
		success : function( json) {
			console.log(json);
			document.getElementById("frame").contentDocument.write(json);
		},
		error : function( xhr, status ) {
			return false;
		},
		complete : function(xhr, status) {
			console.log("complete");
		}
	});
}
rule = new RuleClass();
//筛选出有用的项
function rebuildRule() {
	var nr = new RuleClass();
	for (var i=0; i<rule.content.length; i++) {
		if (rule.content[i].choose) {
			rule.content[i].name = $('#name'+i).val();
			nr.content.push(rule.content[i]);
		}
	}
	return nr;
};
//要求后台处理
function parse() {
	//重新清洗下结果数据项
	var newRule = rebuildRule();
	var url = document.getElementById("url").value;
	var site = document.getElementById("site").value;
	console.log(url);
	console.log("send"+newRule);
	ruleStr = JSON.stringify(newRule);
	$.ajax({
		url: "/Analyze/AnalyzeHtml",
		data: {
			ruleStr: ruleStr,
			site:site,
			urlsStr: url,
			method: "parse",
			type:2
		},
		type : "POST",
		async : false,
		dataType : "json",
		contentType:'application/x-www-form-urlencoded; charset=utf-8',
		success : function( json) {
			console.log(json);
		},
		error : function( xhr, status ) {
			return false;
		},
		complete : function(xhr, status) {
			console.log("complete");
		}
	});
}
//展示选中区块
function showArea(index,seq) {
	document.getElementById("frame").contentWindow.showChild(rule.content[index].path, seq);
};

function addPath(ruleUnit) {
	rule.content.push(ruleUnit);
	addList(ruleUnit, rule.content.length-1);
};
//删除元素标记
function deleteElement(index){
	console.log(index);
	rule.content[index].choose = false;
	$('#index'+index).hide();
};
//修改关键字包含（目前只支持一个)
function modify(index) {
	//提交修改时  先去除子页面的变色
	document.getElementById("frame").contentWindow.hideChild();
	$('#edit').hide();
	$('#list').show();
	var trs = $('#pathbody').find('tr');
	for (var i=0; i<trs.length; i++) {
	//先删除全部关键字
		rule.content[index].path[i].keys = [];
		var td = $(trs[i]).find('td:nth-child(4)');
		var input = $(td).find('input')[0];
		//提取关键字
		var text = $.trim($(input).val());
		if (text != "") {
			rule.content[index].path[i].keys.push(text);
		}
	}
};
//编辑路径元素
function edit(index) {
	$('#list').hide();
	$('#edit').show();
	$('#pathbody').empty();
	for (var i=0; i<rule.content[index].path.length; i++) {
		unit = rule.content[index].path[i];
		value = "";
		if (unit.keys.length>0) value = unit.keys[0];
		$('#pathbody').append(
			"<tr>"+
			"<td><label>"+i+"</label></td>"+
			"<td><label>"+unit.tagName+"</label></td>"+
			"<td><label>"+unit.index+"</label></td>"+
			"<td><input type='text' value='"+value+"'</td>"+
			"<td><button onclick='showArea("+index+","+i+")'>显示</button></td>"+
			"</tr>"
		);
	}
	$('#pathbody').append("<button onclick='modify("+index+")'>提交修改</button>");
};
//采用三元组展示
function addList(ruleUnit, index) {
	$('#tbody').append(
		"<tr id='index"+index+"'>"+
		"<td><button onclick='deleteElement("+index+")'>删除</button></td>"+
		"<td><label>"+index+"</label></td>"+
		"<td><input type='text' id='name"+index+"' value='default'/></td>"+
		"<td><label>"+ruleUnit.value+"</label></td>"+
		"<td><button onclick='edit("+index+")'>编辑</button></td>"+
		"</tr>"
	);
};
</script>
</head>
<body>
<p>请输入网站<input id="site" type="text" style="width:30%" /></p>
<p>请输入模板网页<input id="url" type="text" style="width:30%" /></p> 
<button onclick="load()">读取网页</button>
<button onclick="add()" >显示当前提取结果</button>
<button onclick="parse()" >提交选择结果</button>
<div id="list">
<table border="1" align="center">
<thead>
	<tr>
		<th>删除</th>
		<th>序号</th>
		<th>表项名称</th>
		<th>表项内容</th>
		<th>路径</th>
	</tr>
</thead>
<tbody id="tbody">
</tbody>
</table>
</div>
<br>
<div id="edit" style="display:none;">
<table border="1" align="center">
<thead>
	<tr>
		<th>编号</th>
		<th>名称</th>
		<th>顺序</th>
		<th>关键字</th>
		<th>显示范围</th>
	</tr>
</thead>
<tbody id="pathbody">
</tbody>
</table>
</div>
<iframe id="frame" width="1050px" height="500px" src="content.html"></iframe>
</body>
</html>