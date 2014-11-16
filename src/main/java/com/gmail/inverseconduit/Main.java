package com.gmail.inverseconduit;

import com.gmail.inverseconduit.bot.JavaBot;
import com.gmail.inverseconduit.commands.RunScriptCommand;
import com.gmail.inverseconduit.security.ScriptSecurityManager;
import com.gmail.inverseconduit.security.ScriptSecurityPolicy;

import java.security.Policy;

public class Main {

	private static JavaBot javaBot;

	public static void main(String[] args) {
		//setup security manager
		Policy.setPolicy(ScriptSecurityPolicy.getInstance());
		System.setSecurityManager(ScriptSecurityManager.getInstance());

		//get program arguments
		SESite site;
		String username, password;
		int room;
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
		} else {
			site = SESite.STACK_OVERFLOW;
			username = BotConfig.LOGIN_EMAIL;
			password = BotConfig.PASSWORD;
			room = 139;
		}

        javaBot = new JavaBot();
        boolean loggedIn = javaBot.login(site, username, password);
        if ( !loggedIn) {
            System.out.println("Login failed!");
            return;
        }

        javaBot.addListener(new RunScriptCommand());
        try {
            javaBot.joinChat(site, room);
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        while (true)
            javaBot.processMessages();
    }
}
