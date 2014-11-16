package com.gmail.inverseconduit;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.commons.lang3.StringUtils;

/**
 * Parses the command-line arguments.
 * @author Michael Angstadt
 */
public class CliArguments {
	private final OptionSet options;
	private final OptionParser parser;

	/**
	 * @param args the command-line arguments that were passed into the "main"
	 * method.
	 */
	public CliArguments(String... args) {
		parser = new OptionParser();
		parser.acceptsAll(Arrays.asList("u", "username"), "The chat bot's username.").withRequiredArg();
		parser.acceptsAll(Arrays.asList("p", "password"), "The chat bot's password.").withRequiredArg();
		parser.acceptsAll(Arrays.asList("s", "site"), "The website to connect to.  Supported sites are: " + StringUtils.join(supportedSiteValues(), ", ")).withRequiredArg();
		parser.acceptsAll(Arrays.asList("r", "room"), "The chat room ID.").withRequiredArg();
		parser.accepts("javadoc-dir", "The directory where the Javadoc ZIP files are held.  Defaults to \"javadocs\".").withRequiredArg();
		parser.acceptsAll(Arrays.asList("?", "h", "help"), "Prints this help message.");

		options = parser.parse(args);
	}

	/**
	 * Gets the chat bot's username.
	 * @return the username
	 */
	public String username() {
		return get("username", true);
	}

	/**
	 * Gets the chat bot's password.
	 * @return the password
	 */
	public String password() {
		return get("password", true);
	}

	/**
	 * Gets the StackExchange site to connect to.
	 * @return the StackExchange site
	 * @throws IllegalArgumentException if the given site is invalid
	 */
	public SESite site() {
		String value = get("site", true);
		SESite site = SESite.fromDir(value);
		if (site == null) {
			List<String> dirs = new ArrayList<>();
			for (SESite s : SESite.values()) {
				dirs.add(s.getDir());
			}
			throw new IllegalArgumentException("Invalid value for \"site\" argument.  Supported values are: " + StringUtils.join(supportedSiteValues(), ", "));
		}

		return site;
	}

	/**
	 * Gets the chat room ID.
	 * @return the chat room ID
	 */
	public int room() {
		String value = get("room", true);
		return Integer.parseInt(value);
	}

	/**
	 * Gets the location of the Javadoc ZIP file directory.
	 * @return the Javadoc ZIP file directory
	 */
	public Path javadocDir() {
		String value = get("javadoc-dir");
		return (value == null) ? null : Paths.get(value);
	}

	/**
	 * Gets whether the "help" parameter was specified.
	 * @return true to show help, false not to
	 */
	public boolean help() {
		return has("help");
	}

	/**
	 * Prints the help text to stdout.
	 */
	public void printHelp() {
		try {
			parser.printHelpOn(System.out);
		} catch (IOException e) {
			//should never be thrown because we're writing to stdout
			throw new RuntimeException(e);
		}
	}

	private String get(String option) {
		return get(option, false);
	}

	private String get(String option, boolean required) {
		String value = (String) options.valueOf(option);
		if (value == null && required) {
			throw new IllegalArgumentException("\"" + option + "\" argument required.");
		}
		return value;
	}

	private boolean has(String option) {
		return options.has(option);
	}

	private List<String> supportedSiteValues() {
		List<String> dirs = new ArrayList<>();
		for (SESite s : SESite.values()) {
			dirs.add(s.getDir());
		}
		return dirs;
	}
}
