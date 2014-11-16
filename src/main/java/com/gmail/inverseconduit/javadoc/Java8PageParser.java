package com.gmail.inverseconduit.javadoc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
			description = visitor.getStringBuilder().toString();
		}

		//TODO support retrieval of method docs

		return new ClassInfo(className, description);
	}

	private static Document parsePage(InputStream in) throws IOException {
		return Jsoup.parse(in, "UTF-8", "https://docs.oracle.com/javase/8/docs/api/");
	}
}
