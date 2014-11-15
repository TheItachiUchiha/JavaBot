package com.gmail.inverseconduit.javadoc;

import java.io.IOException;
import java.util.List;

/**
 * Parses Javadoc HTML pages.
 * @author Michael Angstadt
 */
public interface PageParser {
	/**
	 * Gets the fully-qualified names of all the classes.
	 * @return the fully qualified names of each class
	 * @throws IOException if there's a problem reading the fully qualified
	 * names
	 */
	public List<String> getAllClassNames() throws IOException;

	/**
	 * Gets information on a class.
	 * @param className the fully-qualified class name
	 * @return the class info or null if the class cannot be found
	 * @throws IOException if there's a problem reading the class info
	 */
	public ClassInfo getClassInfo(String className) throws IOException;
}
