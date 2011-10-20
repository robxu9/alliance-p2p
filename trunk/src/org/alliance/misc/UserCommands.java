package org.alliance.misc;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.alliance.core.Language;
import org.alliance.core.node.Friend;
import org.alliance.core.node.MyNode;
import org.alliance.core.node.Node;
import org.alliance.ui.UISubsystem;
import org.alliance.ui.dialogs.OptionDialog;
import org.alliance.ui.windows.mdi.chat.AbstractChatMessageMDIWindow;
import org.alliance.ui.windows.mdi.chat.PrivateChatMessageMDIWindow;
import org.alliance.ui.windows.mdi.search.SearchMDIWindow;

public enum UserCommands {
	HELP("help") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			StringBuilder s = new StringBuilder();
			s.append("<b>" + Language.getLocalizedString(getClass(), "helpabout") + "</b>");
			for (UserCommands cmd : UserCommands.values()) {
				if (cmd.isAdminOnly() && !ui.getCore().getFriendManager().getMe().isAdmin())
					continue;
				s.append("<br>&nbsp;&bull; " + cmd.getName() + " &mdash; " + Language.getLocalizedString(getClass(), cmd.getName()));
			}
			chat.addSystemMessage(s.toString());
			return "";
		}
	},
	
	NICK("nick") {
		// TODO: fix bug
		// if you change your nickname with /nick and then use /me, /me doesn't
		// recognize the name change.
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String nickname = args.trim();
			if (ui.getCore().getFriendManager().getMe().canNickname(nickname)) {
				// TODO: refactor code so we don't need two separate setNickname()s
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
	
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String status = args.trim();
			if (status.length() <= MAX_STATUS_LENGTH) {
				// TODO: refactor code so we don't need two separate setStatus()s
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
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			chat.chatClear();
			return "";
		}
	},
	
	CLEARLOG("clearlog") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			chat.chatClear();
			ui.getCore().getPublicChatHistory().clearHistory();
			return "";
		}
	},
	
	RECONNECT("reconnect") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
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
			return "";
		}
	},
	
	RECONNECTALL("reconnectall") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "reconnectingall"));
			Collection<Friend> friends = ui.getCore().getFriendManager().friends();
			for (Friend friend : friends) {
				try {
					friend.reconnect();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			return "";
		}
	},
	
	REHASH("rehash") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			ui.getCore().getShareManager().getShareScanner().startScan(true);
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "rehashing"));
			return "";
		}
	},
	
	SEARCH("search") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String query = args.trim();
			try {
				SearchMDIWindow searchWindow = new SearchMDIWindow(ui);
				searchWindow.refresh(ui);
				searchWindow.search(query);
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "searching", query));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return "";			
		}
	},
	
	MESSAGE("msg") {	
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			String message = null;
			int split = name.indexOf(">"); // > is not allowed in usernames
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
	
	MESSAGEALL("msgall", true) {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String message = args.trim();
			Collection<Friend> friends = ui.getCore().getFriendManager().friends();
			for (Friend friend : friends) {
				try {
					new PrivateChatMessageMDIWindow(ui, friend.getGuid()).sendMessage(AbstractChatMessageMDIWindow.ADMIN_COMMAND + message);
				}
				catch (Exception e) {
					chat.addSystemMessage(Language.getLocalizedString(getClass(), "msg_invalid", friend.getNickname()));
					e.printStackTrace();
				}
			}
			return "";
		}
	},
	
	SYSTEM("system", true) {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			// On 1.3.0, system messages should be displayed with a * instead of
			// a username, and should be in the system color. Like:
			// * message goes here
			// This won't be possible on 1.2.2, so the only option would be
			// something like:
			// Spiderman: * SYSTEM: message goes here
			return AbstractChatMessageMDIWindow.ADMIN_COMMAND + args.trim();
		}
	},
	
	ME("me") {		
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			return AbstractChatMessageMDIWindow.USER_ACTION + args.trim();
		}
	},
	
	WHOIS("whois") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
            String name = args.trim();
			Friend friend = ui.getCore().getFriendManager().getFriend(name);
			if (friend == null) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
			}
			else {
				OptionDialog.showInformationDialog(ui.getMainWindow(), friend.getInfoString());
			}
			return "";
		}
		
	},
	
	IGNORELIST("ignorelist") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			StringBuilder s = new StringBuilder();
			Set<Integer> ignoreList = ui.getCore().getSettings().getMy().getIgnoreList();
			if (ignoreList.isEmpty()) {
				s.append(Language.getLocalizedString(getClass(), "ignorelistempty"));
			}
			else {
				s.append("<b>" + Language.getLocalizedString(getClass(), "ignorelistabout") + "</b>");
				for (Integer guid : ignoreList) {
					Friend friend = ui.getCore().getFriendManager().getFriend(guid);
					s.append("<br>&nbsp;&bull; " + friend.getNickname());
				}
			}
			chat.addSystemMessage(s.toString());
			return "";
		}
	},
	
	IGNORE("ignore") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			Friend friend = ui.getCore().getFriendManager().getFriend(name);
			if (friend == null) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
			}
			else if (friend.isAdmin()) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "ignore_admin", name));
			}
			else {
				// TODO why does starting Alliance with a nonempty ignore list
				// corrupt the settings?
				ui.getCore().getSettings().getMy().addIgnore(friend.getGuid());
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "ignored", name));
            }
			return "";
		}
	},
	
	UNIGNORE("unignore") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			Friend friend = ui.getCore().getFriendManager().getFriend(name);
			if (friend == null) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
			}
			else if (!ui.getCore().getSettings().getMy().removeIgnore(friend.getGuid())) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "unignore_invalid", name));
			}
			else {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "unignored", name));
			}
			return "";
		}
	},
	
	UNIGNOREALL("unignoreall") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			int n = ui.getCore().getSettings().getMy().getIgnoreList().size();
			ui.getCore().getSettings().getMy().getIgnoreList().clear();
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "unignoredall", Integer.toString(n)));
			return "";
		}
	},
	
	SILENCE("silence", true) {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			Friend friend = ui.getCore().getFriendManager().getFriend(name);
			if (friend == null) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
			}
			else{
				return AbstractChatMessageMDIWindow.SILENCE_COMMAND + name;
			}
			return "";
		}
	},
	
	UNSILENCE("unsilence", true) {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			Friend friend = ui.getCore().getFriendManager().getFriend(name);
			if (friend == null) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
			}
			else{
				return AbstractChatMessageMDIWindow.UNSILENCE_COMMAND + name;
			}
			return "";
		}
	},
	
	BAN("ban", true) {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			Friend friend = ui.getCore().getFriendManager().getFriend(name);
			if (friend == null) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
			}
			else {
				return AbstractChatMessageMDIWindow.BAN_COMMAND + name;
			}
			return "";
		}
	},
	
	// TODO unban
	
	EXIT("exit") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "exiting"));
			ui.getCore().shutdown();
	        System.exit(0);
			return "";
		}
	};
	
	private final String name;
	private final boolean adminOnly;
	
	private UserCommands(String name) {
		this(name, false);
	}
	
	private UserCommands(String name, boolean adminOnly) {
		this.name = name;
		this.adminOnly = adminOnly;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isAdminOnly() {
		return adminOnly;
	}
	
	protected abstract String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat);
	
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
			if (command.isAdminOnly() && !ui.getCore().getFriendManager().getMe().isAdmin()) {
				chat.addSystemMessage(Language.getLocalizedString(UserCommands.class, "admin_only"));
				return "";
			}
			message = message.trim().substring(command.getName().length() + 1);
			message = command.execute(message, ui, chat);
		}
		return message;
	}
}
