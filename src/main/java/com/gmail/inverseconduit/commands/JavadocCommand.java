package com.gmail.inverseconduit.commands;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.inverseconduit.BotConfig;
import com.gmail.inverseconduit.bot.AbstractBot;
import com.gmail.inverseconduit.bot.JavaBot;
import com.gmail.inverseconduit.chat.MessageListener;
import com.gmail.inverseconduit.datatype.ChatMessage;
import com.gmail.inverseconduit.javadoc.ClassInfo;
import com.gmail.inverseconduit.javadoc.JavadocDao;
import com.gmail.inverseconduit.javadoc.MultipleClassesFoundException;

/**
 * Processes Javadoc commands.
 * @author Michael Angstadt
 */
public class JavadocCommand implements MessageListener {
	private final Pattern messageRegex = Pattern.compile("^" + Pattern.quote(BotConfig.TRIGGER) + "javadoc:(.*)");
	private final JavadocDao dao;

	public JavadocCommand(JavadocDao dao) {
		this.dao = dao;
	}

	@Override
	public void onMessage(AbstractBot bot, ChatMessage chatMessage) {
		String message = chatMessage.getMessage();
		Matcher messageMatcher = messageRegex.matcher(message);
		if (!messageMatcher.find()) {
			return;
		}

		String commandText = messageMatcher.group(1).trim();
		String response;
		try {
			ClassInfo info;
			try {
				info = dao.getClassInfo(commandText);
			} catch (IOException e) {
				throw new RuntimeException("Problem getting Javadoc info.", e);
			}

			if (info == null) {
				response = "Sorry, I never heard of that class. :(";
			} else {
				response = info.getDescription();
				int pos = response.indexOf("\n");
				if (pos >= 0) {
					//just display the first paragraph
					response = response.substring(0, pos);
				}
			}
		} catch (MultipleClassesFoundException e) {
			StringBuilder sb = new StringBuilder("Which one do you mean?");
			for (String name : e.getClasses()) {
				sb.append("\n* ").append(name);
			}
			response = sb.toString();
		}

		response = "@" + chatMessage.getUsername() + " " + response;
		JavaBot jBot = (JavaBot) bot;
		jBot.sendMessage(chatMessage.getSite(), chatMessage.getRoomId(), response);
	}
}
