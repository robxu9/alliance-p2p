package org.alliance.misc;

import java.io.IOException;
import java.util.Collection;

import org.alliance.core.Language;
import org.alliance.core.file.filedatabase.FileType;
import org.alliance.core.node.Friend;
import org.alliance.core.node.MyNode;
import org.alliance.ui.UISubsystem;
import org.alliance.ui.dialogs.OptionDialog;
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
			String nickname = args.trim();
			if (ui.getCore().getFriendManager().getMe().canNickname(nickname)){
				ui.getCore().getSettings().getMy().setNickname(nickname);
				ui.getCore().getFriendManager().getMe().setNickname(nickname);
			}
			else {
				int over = nickname.length() - MyNode.MAX_NICKNAME_LENGTH;
				if (over > 0) { // too long
					chat.addSystemMessage(Language.getLocalizedString(getClass(), "nick_invalid1", Integer.toString(over)));
				}
				else { // reserved
					chat.addSystemMessage(Language.getLocalizedString(getClass(), "nick_invalid2"));
				}
			}
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
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "status_invalid", Integer.toString(status.length() - MAX_STATUS_LENGTH)));
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
	
	RECONNECT("reconnect") {
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			if (name.equals("*")) { // reconnect to all friends
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "reconnecting_all"));
				Collection<Friend> friends = ui.getCore().getFriendManager().friends();
				for (Friend friend : friends) {
					try {
						friend.reconnect();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			else { // reconnect to named friend
				Friend friend = ui.getCore().getFriendManager().getFriend(name);
				if (friend == null) {
					chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
				}
				else {
					chat.addSystemMessage(Language.getLocalizedString(getClass(), "reconnecting", name));
					try {
						friend.reconnect();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return "";
		}
	},
	
	REHASH("rehash") {
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			ui.getCore().getShareManager().getShareScanner().startScan(true);
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "rehashing"));
			return "";
		}
	},
	
	SEARCH("search") {
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String query = args.trim();
			try {
				ui.getCore().getFriendManager().getNetMan().sendSearch(query, FileType.EVERYTHING);
				//TODO Have this moved to front instead of displaying this message
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "searching", query));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			return "";
		}
	},
	
	MESSAGE("msg") {
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			String message = null;
			int split = name.indexOf(">");
			if (split > -1) {
				message = name.substring(split).trim();
				name = name.substring(0, split).trim();
			}
			Friend friend = ui.getCore().getFriendManager().getFriend(name);
			if (friend == null) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
			}
			else {
                try {
                	ui.getMainWindow().chatMessage(friend.getGuid(), message, System.currentTimeMillis(), false);
				}
                catch (Exception e) {
					e.printStackTrace();
					chat.addSystemMessage(Language.getLocalizedString(getClass(), "msg_invalid", name));
				}
            }
			return "";
		}
	},
	
	EXIT("exit") {
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "exiting"));
			ui.getCore().shutdown();
	        System.exit(0);
			return "";
		}
	};
	
	/**
	 * TODO:
	 * whois USER - Shows USER's tooltip data in chat, like the help info.
	 * me ACTION - Sends a user action to everyone.
	 *     (Type "/me is rehashing.")
	 *     [12:00] * Rangi is rehashing.
	 *     Older versions will see this:
	 *     [12:00] Rangi: Rangi is rehashing.
	 * system MESSAGE - Sends a system message to everyone. (Admins only.)
	 *     (Type "/system Stop spamming!")
	 *     [12:00] * Stop spamming!
	 *     Older versions will see this:
	 *     [12:00] Alliance: Stop spamming!
	 * We need a mechanism to send system messages, and to reserve the nickname
	 * "Alliance". Also system messages don't yet get saved to the chat history.
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
