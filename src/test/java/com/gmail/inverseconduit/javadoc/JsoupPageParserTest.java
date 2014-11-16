package com.gmail.inverseconduit.javadoc;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * @author Michael Angstadt
 */
public class JsoupPageParserTest {
	@Test
	public void getAllClasses() throws Exception {
		JsoupPageParser parser = new JsoupPageParser(new PageLoader() {
			@Override
			public InputStream getClassPage(String className) throws IOException {
				return null;
			}

			@Override
			public InputStream getAllClassesFile() throws IOException {
				return getClass().getResourceAsStream("jsoup-allclasses-frame.html");
			}
		});

		List<String> actual = parser.getAllClassNames();
		//@formatter:off
		List<String> expected = Arrays.asList(
			"org.jsoup.nodes.Attribute",
			"org.jsoup.nodes.Attributes",
			"org.jsoup.nodes.Comment",
			"org.jsoup.Connection",
			"org.jsoup.Connection.Base"
		);
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void getClassInfo() throws Exception {
		JsoupPageParser parser = new JsoupPageParser(new PageLoader() {
			@Override
			public InputStream getClassPage(String className) throws IOException {
				return getClass().getResourceAsStream("Attribute.html");
			}

			@Override
			public InputStream getAllClassesFile() throws IOException {
				return null;
			}
		});

		ClassInfo info = parser.getClassInfo("org.jsoup.nodes.Attribute");
		assertEquals("org.jsoup.nodes.Attribute", info.getFullName());
		assertEquals("A single key + value attribute. Keys are trimmed and normalised to lower-case.", info.getDescription());
	}
}
