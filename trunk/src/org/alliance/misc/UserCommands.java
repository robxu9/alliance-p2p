package org.alliance.misc;

import org.alliance.core.Language;
import org.alliance.ui.UISubsystem;
import org.alliance.ui.windows.mdi.chat.AbstractChatMessageMDIWindow;

public enum UserCommands {
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
	};
	
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
