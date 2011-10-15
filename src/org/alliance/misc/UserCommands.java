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
	
	ME("me") {
		private static final int MAX_STATUS_LENGTH = 140;
	
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String status = args.trim();
			if (status.length() <= MAX_STATUS_LENGTH) {
				ui.getCore().getSettings().getMy().setStatus(status);
				ui.getCore().getFriendManager().getMe().setStatus(status);
				chat.addMessage(ui.getCore().getSettings().getMy().getNickname(),
						"<i>" + status.replace("<", "&lt;").replace(">", "&gt;") + "</i>",
						System.currentTimeMillis(), false, false, false);
			}
			else {
				chat.addMessage(ui.getCore().getSettings().getMy().getNickname(),
						"<i>" + Language.getLocalizedString(getClass(), "overstatus", Integer.toString(status.length() - MAX_STATUS_LENGTH)) + "</i>",
						System.currentTimeMillis(), false, false, false);
			}
			return "";
		}
	},
	HELP("help"){
		private StringBuilder s = new StringBuilder();
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			s.append("<br><b>" + Language.getLocalizedString(getClass(), "help") + "</b>");
			for(UserCommands cmd : UserCommands.values()){
				s.append("<br>" + cmd.getName() + " - " + Language.getLocalizedString(getClass(), cmd.getName()));
			}
			chat.addMessage(ui.getCore().getSettings().getMy().getNickname(), s.toString(),
					System.currentTimeMillis(), false, false, false);
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
			if(message.endsWith("/"+command.getName())){
				message = message.substring(0, message.length() - command.getName().length() - 1);
			}
			else{
			message = message.substring(command.getName().length() + 1);
			}
			message = command.execute(message, ui, chat);
		}
		return message;
	}
}
