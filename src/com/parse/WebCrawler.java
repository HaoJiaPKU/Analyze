package com.parse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {

	public static int MAX_COUNT = 10;

	public static void main(String[] args) {
		ArrayList<String> urls = new ArrayList<String>();
		urls.add("http://www.jxdyf.com");
		urls.add("http://www.yaofang.cn");
		crawling(urls, "pages", 10);
	}
	
	public static void crawling(ArrayList<String> urls, String dest, int maxCount) {
		System.out.println(maxCount);
		MAX_COUNT = maxCount;
		Queue<String> queue = new LinkedList<String>();
		SimpleBloomFilter sbf = new SimpleBloomFilter();
		for (String url : urls) {
			queue.clear();
			int count = 0;
			String dir = createDir(dest, url);
			queue.add(url);
			sbf.add(url);
			while (!queue.isEmpty() && count <= MAX_COUNT) {
				System.out.println(count);
				String cur = queue.poll();
				count++;
				String content = getPage(cur);
				String path = dir + "/" + getFileNameByUrl(cur);
				writeToFile(content, path);
				Set<String> links = extractLinks(content, cur, url);
				for (String link : links) {
					if (sbf.contains(link))
						continue;
					queue.add(link);
					sbf.add(link);
					
				}
			}
		}
	}

	private static String createDir(String dest, String url) {
		String path = dest + "/" + getFileNameByUrl(url);
		File f = new File(path);
		if (!f.isDirectory())
			f.mkdirs();
		return path;
	}

	private static void writeToFile(String content, String path) {
		File f = new File(path);
		if (f.exists())
			return;
		try {
			FileOutputStream fos = new FileOutputStream(f);
			OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
			out.write(content);
			out.flush();
			out.close();
//			FileWriter fw = new FileWriter(f);
//			fw.write(content);
//			fw.flush();
//			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Set<String> extractLinks(String content, String baseUrl,
			String scope) {
		Set<String> ret = new HashSet<String>();
		Document doc = Jsoup.parse(content, baseUrl);
		Elements links = doc.select("a[href]");
		print("\nLinks: (%d)", links.size());
		for (Element link : links) {
			String url = link.attr("abs:href");
			url = url.replaceAll("#.*", "");
			if (!url.matches("[a-zA-z]+://[^\\s]*"))
				continue;
			if (accept(url, scope) && !ret.contains(url)) {
				ret.add(url);
				// print(" * a: <%s>  (%s)", url, trim(link.text(), 35));
			}
		}
		return ret;

	}

	private static boolean accept(String url, String scope) {

		if (url.indexOf(scope) != -1)
			return true;
		return false;
	}

	private static void print(String msg, Object... args) {
		System.out.println(String.format(msg, args));
	}

	private static String getPage(String url) {
		String content = new String();
		/* 1.生成 HttpClinet 对象并设置参数 */
		DefaultHttpClient httpclient = new DefaultHttpClient();
		// html = null;
		try {
			HttpGet httpget = new HttpGet(url);
			httpclient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			// 读取超时
			httpclient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, 5000);
			// httpget.addHeader("Content-Type", "text/html;charset=GB2312");

			System.out.println("executing request " + httpget.getURI());

			HttpResponse rsp = httpclient.execute(httpget);
			HttpEntity entity = rsp.getEntity();

			// get charset
			String charSet = EntityUtils.getContentCharSet(entity);
			byte[] bytes = EntityUtils.toByteArray(entity);

			// String charSet = charset.toString();
			if (charSet == null || charSet.equals("")) {

				String regEx = "(?=<meta).*?(?<=charset=[\\'|\\\"]?)([[a-z]|[A-Z]|[0-9]|-]*)";

				Pattern p = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);

				Matcher m = p.matcher(new String(bytes)); // 默认编码转成字符串，因为我们的匹配中无中文，所以串中可能的乱码对我们没有影响
				if (m.find() && m.groupCount() == 1) {
					charSet = m.group(1);
				}
			}
			if (charSet == null || charSet.equals("")) {
				charSet = "UTF-8";
			}
			// charSet = "gb2312";
			// System.out
			// .println("---------------------------------------------------------");
			// System.out.println("Last get charSet: " + charSet);
			System.out.println("----------------------------------------");
			System.out.println(rsp.getStatusLine());

			content = new String(bytes, charSet);
			content = new String(content.getBytes("UTF-8"), "UTF-8");

		} catch (Exception e) {
			// 发生网络异常
			e.printStackTrace();
			//saveState();
			System.out.println("Exception! Save State!");
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
		return content;
	}

	private static void saveState(Collection<String> coll, String filename) {
		File f = new File(filename);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(f));
			for (String str : coll) {
				bw.write(str);
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getFileNameByUrl(String url) {
		// remove http://
		url = url.substring(7);
		url = url.replaceAll("[\\?/:*|<>\"]", "_");
		return url;

	}


}
