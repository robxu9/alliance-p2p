package org.alliance.misc;

import java.io.IOException;

import org.alliance.core.Language;
import org.alliance.core.comm.Connection;
import org.alliance.core.comm.FriendConnection;
import org.alliance.core.file.filedatabase.FileType;
import org.alliance.core.node.Friend;
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
	
	ME("me") {
		private static final int MAX_STATUS_LENGTH = 140;
	
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			//Added this so people who haven't updated will see "USER_ACTION" and be able to understand
			String action = "USER_ACTION: " + args.trim();
			
			return action;
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
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "overstatus", Integer.toString(status.length() - MAX_STATUS_LENGTH)));
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
	},
	RECONNECT("reconnect"){
		//TODO reconnect to all if they type /reconnect *
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String a = args.trim();
			Friend f = ui.getCore().getFriendManager().getFriend(a);
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "noreconnect", "<b>" + a + "</b>"));
			if(f != null){
			try {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "validreconnect", "<b>" + a + "</b>"));
				f.reconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
			}
			
			return "";
		}
		
	},
	REHASH("rehash"){
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			ui.getCore().getShareManager().getShareScanner().startScan(true);
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "rehashing"));
			return "";
		}
		
	},
	SEARCH("search"){
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String q = args.trim();
			try {
				ui.getCore().getFriendManager().getNetMan().sendSearch(q, FileType.EVERYTHING);
				//TODO Have this moved to front instead of displaying this message
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "searching", q));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "";
		}
		
	},
	EXIT("exit"){
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "exiting"));
			ui.getCore().shutdown();
	        System.exit(0);
			return "";
		}
		
	},
	MESSAGE("msg"){
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String a = args.trim();
			Friend f = ui.getCore().getFriendManager().getFriend(a);
            if (f != null) {
                try {
					ui.getMainWindow().chatMessage(f.getGuid(), null, 0, false);
				} catch (Exception e) {
					chat.addSystemMessage(Language.getLocalizedString(getClass(), "nomessage", "<b>" + a + "</b>"));
					e.printStackTrace();
				}
            }
			return "";
		}
	};
	
	/**
	 * TODO:
	 * whois USER - shows USER's tooltip data
	 * msg - private message specific user
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
