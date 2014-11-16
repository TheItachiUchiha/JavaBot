package com.gmail.inverseconduit.javadoc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 * Parses the Jsoup Javadocs.
 * @author Michael Angstadt
 */
public class JsoupPageParser implements PageParser {
	private final PageLoader loader;

	/**
	 * @param loader loads files from the Javadocs
	 */
	public JsoupPageParser(PageLoader loader) {
		this.loader = loader;
	}

	@Override
	public List<String> getAllClassNames() throws IOException {
		Document document;
		try (InputStream in = loader.getAllClassesFile()) {
			document = parsePage(in);
		}

		List<String> classNames = new ArrayList<>();
		for (Element element : document.select("a")) {
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

		JsoupDescriptionNodeVisitor visitor = new JsoupDescriptionNodeVisitor();
		document.traverse(visitor);

		//TODO support retrieval of method docs

		return new ClassInfo(className, visitor.getStringBuilder().toString().trim());
	}

	private static Document parsePage(InputStream in) throws IOException {
		return Jsoup.parse(in, "UTF-8", "http://jsoup.org/apidocs/");
	}

	private static class JsoupDescriptionNodeVisitor extends DescriptionNodeVisitor {
		private Boolean inDescription;

		@Override
		public void head(Node node, int depth) {
			if (inDescription == Boolean.FALSE) {
				return;
			}

			if (inDescription == null) {
				if ("p".equals(node.nodeName())) {
					//the first <p> signals the start of the description
					inDescription = Boolean.TRUE;
				} else {
					return;
				}
			}

			if ("dl".equals(node.nodeName())) {
				inDescription = false;
				return;
			}

			super.head(node, depth);
		}

		@Override
		public void tail(Node node, int depth) {
			if (inDescription == Boolean.TRUE) {
				super.head(node, depth);
			}
		}
	}
}
