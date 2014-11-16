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
public class Java8PageParserTest {
	@Test
	public void getAllClasses() throws Exception {
		Java8PageParser parser = new Java8PageParser(new PageLoader() {
			@Override
			public InputStream getClassPage(String className) throws IOException {
				return null;
			}

			@Override
			public InputStream getAllClassesFile() throws IOException {
				return getClass().getResourceAsStream("java8-allclasses-frame.html");
			}
		});

		List<String> actual = parser.getAllClassNames();
		//@formatter:off
		List<String> expected = Arrays.asList(
			"java.awt.List",
			"java.lang.String",
			"java.util.List",
			"java.util.Map.Entry"
		);
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void getClassInfo() throws Exception {
		Java8PageParser parser = new Java8PageParser(new PageLoader() {
			@Override
			public InputStream getClassPage(String className) throws IOException {
				return getClass().getResourceAsStream("String.html");
			}

			@Override
			public InputStream getAllClassesFile() throws IOException {
				return null;
			}
		});

		ClassInfo info = parser.getClassInfo("java.lang.String");
		assertEquals("java.lang.String", info.getFullName());

		//@formatter:off
		assertEquals(
		" The `String` class represents character strings. \n" +
		" **bold** text\n" +
		" **bold** text\n" +
		" *italic* text\n" +
		" *italic* text\n" +
		" Because String objects are immutable they can be shared. For example: \n" +
		"\n" +
		"\n" +
		"    String str = \"abc\";\n" +
		"\n" +
		"\n" +
		" is equivalent to: \n" +
		"\n" +
		"\n" +
		"    char data[] = {'a', 'b', 'c'};\n" +
		"    String str = new String(data);\n" +
		"\n" +
		" \n" +
		"ignore me \n", info.getDescription());
		//@formatter:on
	}
}
