package org.alliance.misc;

import org.alliance.core.Language;
import org.alliance.ui.UISubsystem;
import org.alliance.ui.windows.mdi.chat.AbstractChatMessageMDIWindow;

public enum UserCommands {
	HELP("help") {
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			StringBuilder s = new StringBuilder();
			s.append("<b>" + Language.getLocalizedString(getClass(), "helpabout") + "</b>");
			for (UserCommands cmd : UserCommands.values()){
				s.append("<br>&nbsp;&bull; " + cmd.getName() + " &mdash; " + Language.getLocalizedString(getClass(), cmd.getName()));
			}
			chat.addSystemMessage(s.toString());
			return "";
		}
	},
	
	NICK("nick") {
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			// TODO: length limit? Also prevent people from naming themselves the
			// same as admins. Not just here, but also via the GUI. Probably need
			// to edit the setNickname() methods. (It seems redundant to have two.)
			String nickname = args.trim();
			ui.getCore().getSettings().getMy().setNickname(nickname);
			ui.getCore().getFriendManager().getMe().setNickname(nickname);
			return "";
		}
	},
	
	STATUS("status") {
		private static final int MAX_STATUS_LENGTH = 140;
	
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String status = args.trim();
			if (status.length() <= MAX_STATUS_LENGTH) {
				ui.getCore().getSettings().getMy().setStatus(status);
				ui.getCore().getFriendManager().getMe().setStatus(status);
			}
			else {
				chat.addSystemMessage("<i>" + Language.getLocalizedString(getClass(), "overstatus", Integer.toString(status.length() - MAX_STATUS_LENGTH)) + "</i>");
			}
			return "";
		}
	},
	
	CLEAR("clear") {
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			chat.chatClear();
			return "";
		}
	},
	
	CLEARLOG("clearlog") {
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			chat.chatClear();
			ui.getCore().getPublicChatHistory().clearHistory();
			return "";
		}
	};
	
	/**
	 * TODO:
	 * rehash - rehashes your files
	 * reconnect USER - reconnects to USER (with no USER, reconnects to everyone?)
	 * search TERMS - searches for TERMS
	 * exit - exits Alliance
	 * whois USER - shows USER's tooltip data
	 * me MESSAGE - performs an action. Here's the difference:
	 *    (Type "I'm downloading that movie.")
	 *    [12:17] Rangi: I'm downloading that movie.
	 *    (Type "/me is downloading that movie.")
	 *    [12:17] * Rangi is downloading that movie.
	 * We'd have to change send() to be able to send a system message, since it
	 * escapes all HTML right now. Also system messages don't yet get saved to
	 * the chat history.
	 * system MESSAGE - admin-only command. Sends a system message to everyone.
	 *     (Type "/system Stop spamming!")
	 *     [12:17] * Stop spamming!
	 */
	
	private final String name;
	
	private UserCommands(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat);
	
	public static UserCommands getCommand(String message) {
		message = message.trim().toLowerCase();
		for (UserCommands cmd : UserCommands.values()) {
			if (message.startsWith("/" + cmd.getName() + " ") || message.endsWith("/" + cmd.getName())) {
				return cmd;
			}
		}
		return null;
	}
	
	public static String handleCommand(String message, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
		UserCommands command = getCommand(message);
		if (command != null) {
			message = message.trim().substring(command.getName().length() + 1);
			message = command.execute(message, ui, chat);
		}
		return message;
	}
}
