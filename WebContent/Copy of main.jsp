<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<script src="script/jquery-2.0.3.js" type="text/javascript"></script>
<script src="script/DataStructure.js" type="text/javascript"></script>
<script>
function add() {
	var doc = document.getElementById("frame").contentDocument;
	//加入jquery包
	var jq = document.createElement("script");
	jq.src = "script/jquery-2.0.3.js";
	jq.type="text";
	jq.language = "javascript";
	doc.head.appendChild(jq);
	//加入数据结构
	var ds = document.createElement("script");
	ds.src = "script/DataStructure.js";
	ds.type="text";
	ds.language = "javascript";
	doc.head.appendChild(ds);
	//加入删除所有链接的选项
	var disa = document.createElement("script");
	disa.text = "$('a').removeAttr('href');";
	doc.head.appendChild(disa);
	//嵌入那些必须在页面的代码
	var script = document.createElement("script");
	script.text = 
		
	"function getXPath(element) {"+
    "  var path = getXPos(element).Pos; "+
    "  var current = element.parentNode; "+
    "  while (current != null && current.tagName != 'BODY') {"+
    "      path = getXPos(current).Pos + ' '+path;"+
    "      current = current.parentNode;"+
    "  }"+
 	"     return path;"+
 	" };"+
	"    function getXPos(element) {"+
	"         var tagName = element.tagName;"+
	"         var index = 0;"+
	"          var current = element.previousSibling;"+
	"          while (current != null) {"+
	"             if (current.tagName == tagName) {"+
	"                 index ++;"+
	"              }"+
	"              current = current.previousSibling;"+
	"          }"+
	"         return {"+
	"             Pos: tagName+' '+index"+
	"         };"+
	"     };"+
	"     var last;"+
	"     var lastBackgroundColor;"+
	"    $(document).on('mousemove',function(e){"+
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
	"         var current = e.target;"+
	"         if (current != 'DOCUMENT' && current.tagName != 'HTML' && current.className != 'overlayStyle' && current.tagName != 'BODY') {"+
	"            var path = getXPath(current);"+
	"            console.log(path);"+
	"			 window.parent.addItem(path)	"+					
	"         }"+
	"     });";
	doc.head.appendChild(script);
}
function load() {
	var url = document.getElementById("url").value;
	console.log(url);
	$.ajax({
		url: "/Analyze/AnalyzeHtml",
		data: {
			url: url
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
function parse() {
	
}
</script>
<script>
function addItem(str) {
	$('#list').append("<tr><td>"+str+"</td></tr>");
};
</script>
</head>
<body>
<a onclick="alert('1')">fasdf</a>
<p>网页模式提取</p>
输入网址<input id="url" type="text" /> 
<button onclick="load()">读取网页</button>
<button onclick="add()" >显示当前提取结果</button>
<button>提交选择结果</button>
<table border="1" id="list"></table>
<iframe id="frame" width="800px" height="500px" src="content.html"></iframe>
</body>
</html>