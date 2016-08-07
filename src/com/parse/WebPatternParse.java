package com.parse;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import com.structure.rule.*;



/**
 * 
 * @author lenovo
 * 实现方案是寻找content中包含的关键字，并且把路径的顺序记录下来
 * 实现的时候 如果网页结构相同则能够准确的提取出相关的内容
 * 但是出现了网页结构类似但内容不相同的情况
 * 为此进行健壮性的改进   亦即如果规则下找不到对应的内容 那么放宽条件寻找兄弟节点的情况
 */


public class WebPatternParse {
	
	public static boolean LOG = true;
	public BufferedWriter log = null;
	public Date date = null;
	public SimpleDateFormat dateformat= null;
	public String logAddr = null;
	public String charset = "utf-8";
	
	private static String charsetName = "utf-8";
	private static String ruleFile1,ruleFile4;
	private static File[] targetFiles;
	private static int total = 0;
	private static boolean debugThisClass = true;
	static {
		//static的初始化
	}
	public  void debug(String str) {
		if (debugThisClass) {
			System.out.println("WebpatternParse: "+str);
		}
	}
	public void debug(Integer s) {
		if (debugThisClass) {
			System.out.println("WebpatternParse: "+s);
		}
	}
	/**
	 * 采用新的思路，具体方法是首先根据内容锁定树的层次结构（对于网站应认为层次结构必须固定）
	 * 其次是，对于树的具体节点位置，需要通过key的内容（即对应层必须包含的自然语言）
	 * 匹配上以后才能认为是进入到正确的子树
	 * 
	 * 改进成纯解析网页后出现了新问题， 就是路径上的关键字不再与每一项内容呈现对应关系，
	 * 也就是之前的约束条件（关键字必定出在路径上有逻辑问题，正确的说法是，必定出现在路径上的节点的自有内容中）
	 * 但是改成这样正确的逻辑后出现了新问题，就是信息不够充足，判断不够准确，
	 * 因为自有内容太稀少，还是需要一些非自有内容（兄弟内容 比如  相邻的span什么）
	 * 
	 * 多个会有交叉干扰 少了不能保证找到
	 * @param args
	 */
//	public static void main(String args[]) {
////		ruleFile1 = "resource/1.htm";
////		ruleFile4 = "resource/4.htm";
//		
//		String ruleFileNew = "data/2.htm";
//		File dir = new File("data");
//		if (dir.isDirectory()) {
//			targetFiles = dir.listFiles();
//		}
//
//		
//		RuleClass rule2 = new AnalyzeHtmlTwoPart().analyzeSample(ruleFileNew);
////		Gson gson = new Gson();
////		String temp = gson.toJson(rule2);
////		System.out.println(temp);
//		Vector<Vector<String[]>> result = new Vector<Vector<String[]>>();
////		RuleClass rl = gson.fromJson(temp, RuleClass.class);
//		RuleClass rl = rule2;
//		new AnalyzeHtmlTwoPart().getKey2(targetFiles, rl, result);
////		System.out.println(new Gson().toJson(result));
//	}
	
	public void processRuleString(String srcDir, String html, Vector<Record> result) {
		RuleClass rule = analyzeSample(html);
//		debug(html);
		//获取规则
		debug("rule " + new Gson().toJson(rule));
		File dir = new File(srcDir);
		debug(srcDir);
		if (dir.isDirectory()) {
			debug("find dir");
			File files[] = dir.listFiles();
			pickRecord(files, rule, result);
		}
	}
	
	public RuleClass analyzeSample(String html) {
		RuleClass rule2 = new RuleClass();
		Vector<ContentUnitC> matchString = new Vector<ContentUnitC>();
		
		String startBody = "**@@";
		String endName = "@@@@";
		String startContent = "||";
		String endContent = "||**";
		String startKey = "##^^";
		String endKey = "$$##";
		//寻找标记的名字和内容的过程
		String nextHtml = html,nameOfHtml, contentOfHtml;
		int startCInd = nextHtml.indexOf(startBody);
		int endNameInd,endContentInd,startContentInd;
		while (startCInd != -1) {
			//如果找到一个可能的关键词
			//对于新的子串而言，名字内容从第一个开始
			endNameInd = nextHtml.indexOf(endName);
			debug(startCInd+startBody.length());
			debug(endNameInd);
			if (endNameInd == -1) {
				debug(" match fail: "+endName);
				break;
			}
			nameOfHtml = nextHtml.substring(startCInd+startBody.length(), endNameInd);
			//对于真正的标记而言  名字内容从第一个开始
			startContentInd = nextHtml.indexOf(startContent);
			endContentInd = nextHtml.indexOf(endContent);
			contentOfHtml = nextHtml.substring(startContentInd + startContent.length(), endContentInd);
			
			nextHtml = nextHtml.substring(endContentInd + endContent.length());
			startCInd = nextHtml.indexOf(startBody);
			
			ContentUnitC cu = new ContentUnitC();
			cu.content = contentOfHtml;
			cu.name = nameOfHtml;
			debug(" name:"+cu.name+" content:"+cu.content);
			matchString.add(cu);
//			debug(cu.name+":"+cu.content);
		}

		//寻找关键词的过程(每一个层级都将包含全部关键词，这个不合道理）
		//采取改进办法  是有owntext才能直接被包含 text的话只保留距离最近的一个
		String keyOfHtml;
		int startKeyInd,endKeyInd;
		nextHtml = html;
		startKeyInd = nextHtml.indexOf(startKey);
		while (startKeyInd != -1) {
			//找到一个关键词的开头
			nextHtml = nextHtml.substring(startKeyInd+startKey.length());
			endKeyInd = nextHtml.indexOf(endKey);
			keyOfHtml = nextHtml.substring(0, endKeyInd);
			nextHtml = nextHtml.substring(endKeyInd + endKey.length());
			for (ContentUnitC cu: matchString) {
				cu.keys.add(keyOfHtml);
			}
			startKeyInd = nextHtml.indexOf(startKey);
		}
		new WebPatternParse().buildRule(html, rule2, matchString);
		return rule2;
	}
	
	//
	/**
	 * 方法二  不仅仅记录路径，同时要求准确命中对应标签
	 * 
	 */
	private void buildRule(String html,RuleClass rule,Vector<ContentUnitC> matchString) {
		Document doc = Jsoup.parse(html, charsetName);
		Element body = doc.body();
		String text = body.text();
		Vector<StructUnitC> path = new Vector<StructUnitC>();
		for (ContentUnitC content:matchString) {
			if (text.contains(content.content)) {
				searchKey2(body, path,rule,content);	
			}
		}
	}
	
	private void searchKey2(Element element, Vector<StructUnitC> path,RuleClass rule,ContentUnitC content) {
		Elements eles = element.children();
		int index = 0;
		//所有的孩子进行遍历
		for (Element ele: eles) {
			String txt = ele.text();
			if (txt.contains(content.content)) {
				String tagName = ele.tagName();
				int seri = -1,childpos = 0;
				for (Element te:eles) {
					if (te.tagName().compareTo(tagName)==0) seri++;
					if (childpos>=index) break;
					childpos++;
				}
				path = (Vector<StructUnitC>) path.clone();
				StructUnitC su = new StructUnitC();
				su.tagName = tagName;
//				debug(" tagName:"+tagName);
//				debug(" text:"+txt);
				su.index = seri;
				//需要对之前的内容处理一下 防止被识别成关键字
				for (String key:content.keys) {
					if (txt.contains(key)) {
						//节点自身所拥有的才进行记录
						su.keys.add(key);
//						debug(" key:"+key);
					}
				}
				path.add(su);
				searchKey2(ele, path,rule,content);
			}
			index++;
		}
		//对只属于自己的内容进行查找  是否命中
		String owntxt = element.ownText();
		if (owntxt.contains(content.content)) {
			RuleUnit ru = new RuleUnit();
			ru.path = path;
			ru.name = content.name;
			rule.content.add(ru);
//			debug(owntxt);
		}
	}
	/**
	 * 
	 * @param files
	 * @param rule
	 * @param contents
	 */
	public void pickRecord(File files[], RuleClass rule, Vector<Record> result) {
		//必须保证 如果没有任何内容被提取出来  那么就不建立对应的model
		Record r;
		for (File file:files) {
			//每个文件都有对应的若干条记录
//			debug("[web parse]"+total++);
			r = new Record();
			r.uri = file.getName();
			try {
				Document doc = Jsoup.parse(file,charsetName);
				Element body = doc.body();
//				debug("[web parse]"+body.text());
				String path = "";
				String spaces = "";
				Feature features = null; 
				for (RuleUnit ru:rule.content) {
					debug(ru.name);
					spaces = "";
					features = new Feature();
					if (!pickItem(body,ru.path,0,path,ru.name, features)) {
						for (int i=0; i<5-ru.name.length(); i++) {
							spaces += "  ";
						}
						debug(ru.name+ spaces + ": " +ru.path.get(ru.path.size()-1).keys.toString()+" not found");
					} else {
						r.features.add(features);
						debug(ru.name+ spaces + ": " +ru.path.get(ru.path.size()-1).keys.toString()+" found "+features.value);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (r.features.size() <= 0) {
				debug(r.uri + " don't match pattern!!");
			}
			else {
				result.add(r);
			}
		}
	}
	
	private boolean pickItem(Element body,Vector<StructUnitC> rule,int index, String path, String name, Feature feature) {
		/**
		 * 如果是最后一条 输出最终结果
		 */
//		debug(path+" : name| "+body.html() + " index:"+index +" rule:"+rule.size());
		if (index >= rule.size()) {
			String spaces = "";
			for (int i=0; i<5-name.length(); i++) {
				spaces += "  ";
			}
			feature.key = name;
			//将html中的空格&nbsp;转化为"" 否则会在文本中变成问号
			feature.value = body.text().replace(Jsoup.parse("&nbsp;").text(), "");
			debug(name + spaces + ": " + feature.value);
			return true;
		}
		StructUnitC su = rule.get(index);
		String tagName = su.tagName;
		int offsetIndex = su.index;
		Elements tagChilds = body.children();
		for (Element tag :tagChilds) {
			debug(body.tagName()+" has "+tag.tagName());
		}
		if (offsetIndex >= tagChilds.size()) {
			debug(" offset:"+offsetIndex+" tag:"+tagChilds.size());
			return false;
		}
		/**
		 * 判断直接的offsetIndex是否符合要求（即位置完全对应的内容   理想的情况）
		 */
		Element child = getChild(tagChilds,offsetIndex, tagName);
		if (child!=null) {
			//对于每一层 原本是要求关键字必须全部匹配
			//现在该换要求，只要保证匹配上至少一个关键字
			boolean match = false;
			for (String key:su.keys) {
//				debug(" key:"+key);
				if (child.text().contains(key)) {
					match =  true;
					break;
				}
			}
			//本层符合要求 则进一步 判断下一层是否符合要求
			if ((su.keys.isEmpty() || match) && pickItem(child, rule, index+1, path+" "+tagName+"-"+offsetIndex+" ", name, feature)) {
				return true;
			}
		}
		debug("deep "+index+" "+tagName+"-"+offsetIndex);
		/**
		 * 倘若标准的位置上并不符合要求 则在同一层级上遍历，找到符合要求的内容（即位置出现了错位 普遍情况）
		 */
		for (int i= 0;i<tagChilds.size(); i++) {
			if (i == offsetIndex) continue;
			Element ele = getChild(tagChilds,i, tagName);
			if (ele==null) break;
			boolean match = false;
			for (String key:su.keys) {
				if (ele.text().contains(key)) {
					match =  true;
					break;
				}
			}
			if ((su.keys.isEmpty() || match) && pickItem(ele,rule,index+1, path+" "+tagName+"-"+i+" ", name, feature)) {
				return true;
			}
		}
		return false;
	}
	//获得某个节点下面第i个tag类型的子节点
	private Element getChild(Elements children, int i, String tagName) {
		int index = 0;
		for (Element e:children) {
			debug(tagName+" "+e.tagName());
			if (tagName.compareToIgnoreCase(e.tagName())==0) {
				if (i==index) {
					return e;
				} else {
					index ++;
				}
			}
		}
		return null;
	}
	
//	public void log(String str) {
//		
//		if (logAddr == null) {
//			logAddr =  "E:/ProgramData/WorkSpaceFor3.7.1/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/KWL/log"; 
//		}
//		if (log == null) {
//			try {
//				log = new BufferedWriter(
//						new OutputStreamWriter(
//								new FileOutputStream(new File(logAddr),true),charset));
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		date = new Date();
//		if (dateformat == null) {
//			dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
//		}
//		try {
//			log.write(dateformat.format(date)+" "+str+"\n\r");
//			log.flush();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//	}
}
/**
 *  目前的方法是先确定出树的层次，而后通过tag和key来锁定子树，快速方法是记录tag的顺序，如果命中可节省时间
 */
//contentUnit结构体主要是用来记录每一个内容和对应的必须包含的关键字
class ContentUnitC {
	public String name;//对应的项名
	public String content;
	public Vector<String> keys;
	public ContentUnitC() {
		keys = new Vector<String>();
	}
}


