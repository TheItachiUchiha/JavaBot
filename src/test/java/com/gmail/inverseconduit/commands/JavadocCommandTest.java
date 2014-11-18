package com.gmail.inverseconduit.commands;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.gmail.inverseconduit.SESite;
import com.gmail.inverseconduit.bot.JavaBot;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.javadoc.ClassInfo;
import com.gmail.inverseconduit.javadoc.JavadocDao;
import com.gmail.inverseconduit.javadoc.JavadocLibrary;

/**
 * @author Michael Angstadt
 */
public class JavadocCommandTest {
	private final JavadocDao dao = new JavadocDao();
	{
		try {
			dao.addJavadocApi(new JavadocLibrary(null, null) {
				@Override
				public List<String> getAllClassNames() throws IOException {
					return Arrays.asList("java.util.ArrayList", "java.util.LinkedList", "java.awt.List", "java.util.List");
				}

				@Override
				public ClassInfo getClassInfo(String className) throws IOException {
					switch (className) {
					case "java.util.ArrayList":
						return new ClassInfo(className, className + " - para 1\n" + className + " - para 2", "http://base/?java/util/ArrayList.html");
					case "java.util.LinkedList":
						return new ClassInfo(className, className + " - para 1", null);
					}
					return null;
				}
			});
		} catch (IOException e) {
			//never thrown
		}
	}

	private final JavadocCommand cmd = new JavadocCommand(dao);
	private final JavaBot bot = spy(new JavaBot());
	{
		doReturn(true).when(bot).sendMessage(any(SESite.class), anyInt(), anyString());
	}

	@Test
	public void no_command_prefix() {
		ChatMessage chatMessage = new ChatMessage(SESite.STACK_OVERFLOW, 1, "Room", "User", 1, "ignore");
		cmd.onMessage(bot, chatMessage);
		verify(bot, never()).sendMessage(any(SESite.class), anyInt(), anyString());
	}

	@Test
	public void not_javadoc_command() {
		ChatMessage chatMessage = new ChatMessage(SESite.STACK_OVERFLOW, 1, "Room", "User", 1, "!!ignore");
		cmd.onMessage(bot, chatMessage);
		verify(bot, never()).sendMessage(any(SESite.class), anyInt(), anyString());
	}

	@Test
	public void class_not_found() {
		ChatMessage chatMessage = new ChatMessage(SESite.STACK_OVERFLOW, 1, "Room", "User", 1, "!!javadoc:FooBar");
		cmd.onMessage(bot, chatMessage);
		verify(bot).sendMessage(SESite.STACK_OVERFLOW, 1, "@User Sorry, I never heard of that class. :(");
	}

	@Test
	public void one_paragraph() {
		ChatMessage chatMessage = new ChatMessage(SESite.STACK_OVERFLOW, 1, "Room", "User", 1, "!!javadoc:LinkedList");
		cmd.onMessage(bot, chatMessage);
		verify(bot).sendMessage(SESite.STACK_OVERFLOW, 1, "@User **`java.util.LinkedList`**: java.util.LinkedList - para 1");
	}

	@Test
	public void two_paragraphs() {
		ChatMessage chatMessage = new ChatMessage(SESite.STACK_OVERFLOW, 1, "Room", "User", 1, "!!javadoc:arraylist");
		cmd.onMessage(bot, chatMessage);
		verify(bot).sendMessage(SESite.STACK_OVERFLOW, 1, "@User [**`java.util.ArrayList`**](http://base/?java/util/ArrayList.html \"View the Javadocs\"): java.util.ArrayList - para 1");
	}

	@Test
	public void multiple_matches() {
		ChatMessage chatMessage = new ChatMessage(SESite.STACK_OVERFLOW, 1, "Room", "User", 1, "!!javadoc: list");
		cmd.onMessage(bot, chatMessage);
		verify(bot).sendMessage(SESite.STACK_OVERFLOW, 1, "@User Which one do you mean?\n* java.util.List\n* java.awt.List");
	}
}
