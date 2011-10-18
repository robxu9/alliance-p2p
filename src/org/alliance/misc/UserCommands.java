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
import org.alliance.ui.windows.mdi.chat.PrivateChatMessageMDIWindow;
import org.alliance.ui.windows.mdi.search.SearchMDIWindow;

public enum UserCommands {
	HELP("help") {
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			StringBuilder s = new StringBuilder();
			s.append("<b>" + Language.getLocalizedString(getClass(), "helpabout") + "</b>");
			for (UserCommands cmd : UserCommands.values()){
				//Don't display Admin commands
				if(!cmd.getName().equals("system")||!cmd.getName().equals("masspm")){
				s.append("<br>&nbsp;&bull; " + cmd.getName() + " &mdash; " + Language.getLocalizedString(getClass(), cmd.getName()));
				}
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
		//TODO have this clear all current results before searching
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String query = args.trim();
				try {
					SearchMDIWindow searchWindow = new SearchMDIWindow(ui);
					searchWindow.search(query);
					chat.addSystemMessage(Language.getLocalizedString(getClass(), "searching", query));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return "";			
		}
	},
	
	MESSAGE("msg") {
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			String message = null;
			int split = name.indexOf(" ");
			if (split > -1) {
				message = name.substring(split+1).trim();
				name = name.substring(0, split).trim();
			}
			Friend friend = ui.getCore().getFriendManager().getFriend(name);
			if (friend == null) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
			}
			else {
                try {
                	new PrivateChatMessageMDIWindow(ui, friend.getGuid()).sendMessage(message);
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
	ME("me") {		
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {		
		//Added this so people who haven't updated will see "USER_ACTION" and be able to understand		
			String action = "USER_ACTION " + args.trim();		
			return action;		
		}		
	},
	EXIT("exit") {
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "exiting"));
			ui.getCore().shutdown();
	        System.exit(0);
			return "";
		}
	},
	SYSTEM("system"){
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			if(ui.getCore().getFriendManager().getMe().iAmAdmin()){
				return "*ADMIN: " + args.trim();
			}
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "admin_invalid"));
			return "";
		}

	},
	MASSPM("masspm"){
		public String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			if(ui.getCore().getFriendManager().getMe().iAmAdmin()){
				String message = args.trim();
				Collection<Friend> friends = ui.getCore().getFriendManager().friends();
				for (Friend friend : friends) {
					try {
						new PrivateChatMessageMDIWindow(ui, friend.getGuid()).sendMessage("*ADMIN: " + message);
					} catch (Exception e) {
						chat.addSystemMessage(Language.getLocalizedString(getClass(), "msg_invalid", friend.getNickname()));
						e.printStackTrace();
					}
				}
			}
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "admin_invalid"));
			return "";
		}
		
	};
	
	/**
	 * TODO:
	 * whois USER - Shows USER's tooltip data in chat, like the help info. (NEED TO BUILD THIS POP-UP)
	 * We need a mechanism to send system messages, and to reserve the nickname
	 * "Alliance". Also system messages don't yet get saved to the chat history.
	 * (I don't think local system messages need to be saved, its more of a local notification)
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
