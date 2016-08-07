package Servlet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.gson.Gson;
import com.parse.WebCrawler;
import com.parse.WebPatternParse;
import com.structure.rule.Feature;
import com.structure.rule.Record;
import com.structure.rule.RuleClass;
import com.structure.rule.RuleUnit;
import com.structure.rule.StructUnitC;

/**
 * Servlet implementation class HtmlServlet
 */
public class HtmlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HtmlServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		processRequest(request, response);
	}
	
	private void info(String str){
		System.out.println("INFO: " + str);
	}

	private void processRequest(HttpServletRequest request, HttpServletResponse response) {
		String type = request.getParameter("type");
		if (type == null) {
			System.err.println("method is null");
		}
		int ty = Integer.parseInt(type);
		switch(ty) {
		case 1:
			//载入页面
			loadHTML(request, response);
			break;
		case 2:
			parse(request, response);
			break;
			default:
				System.err.println("干什么来了");
				break;
		}
		
	}
	
	/**
	 * 载入要求的页面
	 * @param request
	 * @param response
	 */
	private void loadHTML(HttpServletRequest request, HttpServletResponse response) {
		String url = request.getParameter("url");
		try {
			url  = URLDecoder.decode(url ,"utf-8");
		} catch(Exception e) {
			e.printStackTrace();
		}
		//验证路径信息提取结果
		info("1 : " + url);
		int timeout = 1000;
		Document doc = null;
		try {
			doc = Jsoup.parse(new URL(url), timeout);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(doc!=null) info("2 : " + doc.body().text());
		response.setCharacterEncoding("UTF-8");
		try {
			response.getWriter().write(new Gson().toJson((doc.html())));
			info("3 : " + new Gson().toJson((doc.html())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void parse(HttpServletRequest request, HttpServletResponse response) {
		System.out.println("解析");
		
		String ruleStr = request.getParameter("ruleStr");
		String site = request.getParameter("site");
		System.out.println(ruleStr);
		Gson g = new Gson();
		RuleClass rc = g.fromJson(ruleStr, RuleClass.class);
		for (int i=0; i<rc.content.size(); i++) {
			RuleUnit ru = rc.content.elementAt(i);
			System.out.println(ru.value);
			for (int j=0; j<ru.path.size(); j++) {
				StructUnitC suc = ru.path.get(j);
				System.out.println(suc.tagName+" "+suc.index);
			}
		}
		
		String urlsStr = request.getParameter("urlsStr");
		ArrayList<String> urls = new ArrayList<String>();
		System.out.println(site);
		urls.add(site);
		String dest = request.getRealPath(request.getRequestURI())+"/htmlDocument/";
		int maxCount = 100;
		WebCrawler.crawling(urls, dest, maxCount);
		WebPatternParse wpp = new WebPatternParse();
		Vector<Record> result = new Vector<Record>();
		for (String url: urls) {
			File file = new File(dest+WebCrawler.getFileNameByUrl(url));
			if (file.isDirectory()) {
				wpp.pickRecord(file.listFiles(), rc, result);
				System.out.println("当前网页群提取结果 "+url);
				File res = new File(dest+WebCrawler.getFileNameByUrl(url)+"/result.txt");
				try {
					BufferedWriter bw = new BufferedWriter(
											new OutputStreamWriter(
													new FileOutputStream(res),"UTF-8"));
					for (Record r:result) {
						System.out.println("");
						for (Feature f:r.features) {
							System.out.println(f.key + " "+f.value);
							bw.write(f.key + " "+f.value);
							bw.newLine();
						}
						bw.write("");
						bw.newLine();
					}
					bw.flush();
					bw.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				result.clear();
			}
		}
		System.out.println("over");
	}
	
	private void outputParameter(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Enumeration<String> names = request.getParameterNames(); 
		while (names.hasMoreElements()) { 
			String string = (String) names.nextElement(); 
			System.out.println(string); 
		} 
	}
	
}
