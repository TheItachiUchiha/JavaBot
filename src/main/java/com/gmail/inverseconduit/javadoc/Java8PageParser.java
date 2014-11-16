package com.gmail.inverseconduit.javadoc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;

/**
 * Parses the Java 8 Javadocs.
 * @author Michael Angstadt
 */
public class Java8PageParser implements PageParser {
	private final PageLoader loader;

	/**
	 * @param loader loads files from the Javadocs
	 */
	public Java8PageParser(PageLoader loader) {
		this.loader = loader;
	}

	@Override
	public List<String> getAllClassNames() throws IOException {
		Document document;
		try (InputStream in = loader.getAllClassesFile()) {
			document = parsePage(in);
		}

		List<String> classNames = new ArrayList<>();
		for (Element element : document.select("ul li a")) {
			String url = element.attr("href");
			int dotPos = url.lastIndexOf('.');
			if (dotPos < 0) {
				continue;
			}

			url = url.substring(0, dotPos);
			url = url.replace('/', '.');
			classNames.add(url);
		}
		return classNames;
	}

	@Override
	public ClassInfo getClassInfo(String className) throws IOException {
		Document document;
		try (InputStream in = loader.getClassPage(className)) {
			document = parsePage(in);
		}

		String description;
		{
			Element descriptionElement = document.select(".block").first();
			DescriptionNodeVisitor visitor = new DescriptionNodeVisitor();
			descriptionElement.traverse(visitor);
			description = visitor.sb.toString();
		}

		//TODO support retrieval of method docs

		return new ClassInfo(className, description);
	}

	private static Document parsePage(InputStream in) throws IOException {
		return Jsoup.parse(in, "UTF-8", "https://docs.oracle.com/javase/8/docs/api/");
	}

	private static class DescriptionNodeVisitor implements NodeVisitor {
		private final StringBuilder sb = new StringBuilder();
		private boolean inPre = false;

		@Override
		public void head(Node node, int depth) {
			//for (int i = 0; i < depth; i++) {
			//	System.out.print(' ');
			//}
			//System.out.println("head " + node.nodeName());

			switch (node.nodeName()) {
			case "code":
				sb.append("`");
				break;
			case "i":
			case "em":
				sb.append("*");
				break;
			case "b":
			case "strong":
				sb.append("**");
				break;
			case "br":
			case "p":
				sb.append("\n");
				break;
			case "pre":
				inPre = true;
				sb.append("\n");
				break;
			case "#text":
				TextNode text = (TextNode) node;
				String content = inPre ? text.getWholeText() : text.text();
				sb.append(content);
				break;
			}
		}

		@Override
		public void tail(Node node, int depth) {
			//for (int i = 0; i < depth; i++) {
			//	System.out.print(' ');
			//}
			//System.out.println("tail " + node.nodeName());

			switch (node.nodeName()) {
			case "code":
				sb.append("`");
				break;
			case "i":
			case "em":
				sb.append("*");
				break;
			case "b":
			case "strong":
				sb.append("**");
				break;
			case "p":
				sb.append("\n");
				break;
			case "pre":
				inPre = false;
				sb.append("\n");
				break;
			}
		}
	}
}
