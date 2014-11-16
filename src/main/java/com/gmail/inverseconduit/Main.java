package com.gmail.inverseconduit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Policy;

import com.gmail.inverseconduit.bot.JavaBot;
import com.gmail.inverseconduit.commands.JavadocCommand;
import com.gmail.inverseconduit.commands.RunScriptCommand;
import com.gmail.inverseconduit.javadoc.Java8PageParser;
import com.gmail.inverseconduit.javadoc.JavadocDao;
import com.gmail.inverseconduit.javadoc.PageLoader;
import com.gmail.inverseconduit.javadoc.PageParser;
import com.gmail.inverseconduit.javadoc.ZipPageLoader;
import com.gmail.inverseconduit.security.ScriptSecurityManager;
import com.gmail.inverseconduit.security.ScriptSecurityPolicy;

public class Main {
	public static void main(String[] args) throws Exception {
		//setup security manager
		Policy.setPolicy(ScriptSecurityPolicy.getInstance());
		System.setSecurityManager(ScriptSecurityManager.getInstance());

		//get program arguments
		SESite site;
		String username, password;
		int room;
		Path javadocDir;
		if (args.length > 0) {
			CliArguments arguments = new CliArguments(args);
			if (arguments.help()) {
				arguments.printHelp();
				return;
			}

			site = arguments.site();
			username = arguments.username();
			password = arguments.password();
			room = arguments.room();
			javadocDir = arguments.javadocDir();
			if (javadocDir == null){
				javadocDir = Paths.get("javadocs");
			}
		} else {
			site = SESite.STACK_OVERFLOW;
			username = BotConfig.LOGIN_EMAIL;
			password = BotConfig.PASSWORD;
			room = 139;
			javadocDir = Paths.get("javadocs");
		}

        JavaBot javaBot = new JavaBot();
        boolean loggedIn = javaBot.login(site, username, password);
        if ( !loggedIn) {
            System.out.println("Login failed!");
            return;
        }

        javaBot.addListener(new RunScriptCommand());
        
        JavadocDao javadocDao = createJavadocDao(javadocDir);
        javaBot.addListener(new JavadocCommand(javadocDao));
        
        javaBot.joinChat(site, room);

        while (true)
            javaBot.processMessages();
    }
	
	private static JavadocDao createJavadocDao(Path dir) throws IOException {
		JavadocDao dao = new JavadocDao();

		Path java8Api = dir.resolve("java8.zip");
		if (Files.exists(java8Api)) {
			PageLoader loader = new ZipPageLoader(java8Api);
			PageParser parser = new Java8PageParser();
			dao.addJavadocApi(loader, parser);
		}

		return dao;
	}
}
