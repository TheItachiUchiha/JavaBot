package com.gmail.inverseconduit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import joptsimple.OptionException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Michael Angstadt
 */
public class CliArgumentsTest {
	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@Test
	public void username() {
		CliArguments args;

		args = new CliArguments();
		thrown.expect(IllegalArgumentException.class);
		args.username();

		thrown.expect(OptionException.class);
		args = new CliArguments("--username");

		args = new CliArguments("--username", "foobar");
		assertEquals("foobar", args.username());
	}

	@Test
	public void password() {
		CliArguments args;

		args = new CliArguments();
		thrown.expect(IllegalArgumentException.class);
		args.username();

		thrown.expect(OptionException.class);
		args = new CliArguments("--password");

		args = new CliArguments("--password", "foobar");
		assertEquals("foobar", args.username());
	}

	@Test
	public void site() {
		CliArguments args;

		args = new CliArguments();
		thrown.expect(IllegalArgumentException.class);
		args.site();

		thrown.expect(OptionException.class);
		args = new CliArguments("--site");

		args = new CliArguments("--site", "foobar");
		thrown.expect(IllegalArgumentException.class);
		args.site();

		args = new CliArguments("-site", "stackoverflow");
		assertEquals(SESite.STACK_OVERFLOW, args.site());
	}

	@Test
	public void room() {
		CliArguments args;

		args = new CliArguments();
		thrown.expect(IllegalArgumentException.class);
		args.room();

		thrown.expect(OptionException.class);
		args = new CliArguments("--room");
		args.room();

		args = new CliArguments("--room", "foobar");
		thrown.expect(NumberFormatException.class);
		args.room();

		args = new CliArguments("--room", "42");
		assertEquals(42, args.room());
	}

	@Test
	public void javadocDir() {
		CliArguments args;

		args = new CliArguments();
		assertEquals(Paths.get("javadocs"), args.javadocDir());

		thrown.expect(OptionException.class);
		args = new CliArguments("--javadoc-dir");
		args.javadocDir();

		args = new CliArguments("--javadoc-dir", "foobar");
		assertEquals(Paths.get("foobar"), args.javadocDir());
	}

	@Test
	public void help() {
		CliArguments args;

		args = new CliArguments();
		assertFalse(args.help());

		args = new CliArguments("--help");
		assertTrue(args.help());
	}
}
