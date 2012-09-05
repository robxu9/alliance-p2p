package org.alliance.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.alliance.core.comm.siteupdater.SiteUpdate;
import org.alliance.core.Language;
import org.alliance.core.node.Friend;
import org.alliance.core.node.MyNode;
import org.alliance.core.node.Node;
import org.alliance.ui.UISubsystem;
import org.alliance.ui.dialogs.OptionDialog;
import org.alliance.ui.windows.mdi.chat.AbstractChatMessageMDIWindow;
import org.alliance.ui.windows.mdi.chat.PrivateChatMessageMDIWindow;

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

		protected Command executeCommand(Command command) {
			return null;
		}
	},
	
	NICK("nick") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String nickname = args.trim();
			if (ui.getCore().getFriendManager().getMe().canNickname(nickname)) {
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

		protected Command executeCommand(Command command) {
			return null;
		}
	},
	
	STATUS("status") {
		private static final int MAX_STATUS_LENGTH = 140;
	
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String status = args.trim();
			if (status.length() <= MAX_STATUS_LENGTH) {
				ui.getCore().getFriendManager().getMe().setStatus(status);
			}
			else {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "status_invalid", Integer.toString(status.length() - MAX_STATUS_LENGTH)));
			}
			return "";
		}

		protected Command executeCommand(Command command) {
			return null;
		}
	},
	
	CLEAR("clear") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			chat.chatClear();
			return "";
		}

		protected Command executeCommand(Command command) {
			return null;
		}
	},
	
	CLEARLOG("clearlog") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			chat.chatClear();
			ui.getCore().getPublicChatHistory().clearHistory();
			return "";
		}

		protected Command executeCommand(Command command) {
			return null;
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

		protected Command executeCommand(Command command) {
			return null;
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

		protected Command executeCommand(Command command) {
			Collection<Friend> friends = command.ui.getCore().getFriendManager().friends();
			for (Friend friend : friends) {
				try {
					friend.reconnect();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	},
	
	REHASH("rehash") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			ui.getCore().getShareManager().getShareScanner().startScan(true);
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "rehashing"));
			return "";
		}

		protected Command executeCommand(Command command) {
			return null;
		}
	},
	
	SEARCH("search") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String query = args.trim();
			try {
				ui.getMainWindow().getSearchWindow().refresh(ui);
				ui.getMainWindow().getSearchWindow().search(query);
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "searching", query));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return "";			
		}

		protected Command executeCommand(Command command) {
			return null;
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

		protected Command executeCommand(Command command) {
			return null;
		}
	},
	
	MESSAGEALL("msgall", true, null) {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String message = args.trim();
			Collection<Friend> friends = ui.getCore().getFriendManager().friends();
			for (Friend friend : friends) {
				try {
					new PrivateChatMessageMDIWindow(ui, friend.getGuid()).sendMessage(SYSTEM.getKey() + message);
				}
				catch (Exception e) {
					chat.addSystemMessage(Language.getLocalizedString(getClass(), "msg_invalid", friend.getNickname()));
					e.printStackTrace();
				}
			}
			return "";
		}

		protected Command executeCommand(Command command) {
			return null;
		}
	},
	
	SYSTEM("system", true, "* SYSTEM: ") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			chat.addSystemMessage(args.trim());
			return getKey() + args.trim();
		}

		protected Command executeCommand(Command command) {
			command.message = command.message.substring(command.cmd.getKey().length());
			command.isSystem = true;
			return command;
		}
	},
	
	ME("me", false, "/me ") {		
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			return getKey() + args.trim();
		}

		protected Command executeCommand(Command command) {
			return null;
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
				String level = null;
				if(friend.isAdmin()) {
					level = "Admin";
				}
				OptionDialog.showInformationDialog(ui.getMainWindow(), friend.getInfoString(level));
			}
			return "";
		}

		protected Command executeCommand(Command command) {
			return null;
		}
		
	},
	
	IGNORELIST("ignorelist") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			StringBuilder s = new StringBuilder();
			ArrayList<org.alliance.core.settings.Friend> friendlist = ui.getCore().getSettings().getFriendlist();
			ArrayList<Friend> ignoreList = new ArrayList<Friend>();
			for(org.alliance.core.settings.Friend f : friendlist) {
				Friend friend = ui.getCore().getFriendManager().getFriend(f.getGuid());
				if(friend != null && friend.isIgnored()) {
	    			ignoreList.add(friend);
	    		}
	    	}
			if (ignoreList.isEmpty()) {
				s.append(Language.getLocalizedString(getClass(), "ignorelistempty"));
			}
			else {
				s.append("<b>" + Language.getLocalizedString(getClass(), "ignorelistabout") + "</b>");
				for (Friend friend : ignoreList) {
					s.append("<br>&nbsp;&bull; " + friend.getNickname());
				}
			}
			chat.addSystemMessage(s.toString());
			return "";
		}

		protected Command executeCommand(Command command) {
			return null;
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
				friend.ignoreFriend();
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "ignored", name));
            }
			return "";
		}
		
		protected Command executeCommand(Command command) {
			return null;
		}
	},
	
	UNIGNORE("unignore") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			Friend friend = ui.getCore().getFriendManager().getFriend(name);
			if (friend == null) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
			}
			else if (!friend.unignoreFriend()) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "unignore_invalid", name));
			}
			else {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "unignored", name));
			}
			return "";
		}

		protected Command executeCommand(Command command) {
			return null;
		}
	},
	
	UNIGNOREALL("unignoreall") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			int n = 0;
			ArrayList<org.alliance.core.settings.Friend> friendlist = ui.getCore().getSettings().getFriendlist();
			for(org.alliance.core.settings.Friend f : friendlist) {
				Friend friend = ui.getCore().getFriendManager().getFriend(f.getGuid());
	    		if(friend != null && friend.isIgnored()) {
	    			friend.unignoreFriend();
	    			n++;
	    		}
	    	}
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "unignoredall", Integer.toString(n)));
			return "";
		}

		protected Command executeCommand(Command command) {
			return null;
		}
	},
	
	BLOCK("block") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
		String name = args.trim();
		Friend friend = ui.getCore().getFriendManager().getFriend(name);
		if(friend == null) {
			return "";
		}
		Boolean delete = OptionDialog.showQuestionDialog(ui.getMainWindow(), Language.getLocalizedString(getClass(), "block_friend", friend.getNickname()));
        if (delete == null) {
            return "";
        }
        if (delete) {
                if (friend instanceof Friend) {
                    Node f = (Node) friend;
                    if (f != null && f instanceof Friend) {
                    	try {
                    		String friendIP = (((Friend) f).getFriendConnection().getSocketAddress()).toString().substring(1);
							ui.getCore().getSettings().getRulelist().add("DENY    " + friendIP + "/32");
						} catch (Exception e) {
							e.printStackTrace();
						}
                        ui.getCore().getFriendManager().permanentlyRemove((Friend) f);
                    }
                }
            }
        	return "";
		}

		protected Command executeCommand(Command command) {
			return null;
		}
	},
	
	SILENCE("silence", true, "* SILENCE: ") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			Friend friend = ui.getCore().getFriendManager().getFriend(name);
			if (friend == null) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
			}
			else{
				return getKey() + name;
			}
			return "";
		}

		protected Command executeCommand(Command command) {
				// you were silenced
				if (command.isDirectedAtMe()) {
					command.ui.getCore().getFriendManager().getMe().setSilenced(1);
					command.message = Language.getLocalizedString(getClass(), "silenced");
					command.isSystem = true;
				}
				// your friend was silenced
				else {
					if (command.directedAt == null) {
						command.ignored = true;
					}
					else {
						command.directedAt.ignoreFriend();
						command.message = Language.getLocalizedString(getClass(), "silenced_friend", command.directedAt.getNickname());
						command.isSystem = true;
					}
	             }
				return command;
			}
	},
	
	UNSILENCE("unsilence", true, "* UNSILENCE: ") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			Friend friend = ui.getCore().getFriendManager().getFriend(name);
			if (friend == null) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
			}
			else{
				return getKey() + name;
			}
			return "";
		}

		protected Command executeCommand(Command command) {
			// you were unsilenced
			if (command.isDirectedAtMe()){
				command.ui.getCore().getFriendManager().getMe().setSilenced(0);
				command.message = Language.getLocalizedString(getClass(), "unsilenced");
				command.isSystem = true;
			}
			// your friend was unsilenced
			else {
				if (command.directedAt == null) {
					command.ignored = true;
				}
				else {
					command.directedAt.unignoreFriend();
					command.message = Language.getLocalizedString(getClass(), "unsilenced_friend", command.directedAt.getNickname());
					command.isSystem = true;
				}
             }
			return command;
		}
	},
	
	BAN("ban", true, "* BAN: ") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			Friend friend = ui.getCore().getFriendManager().getFriend(name);
			if (friend == null) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
			}
			else {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "banned_friend", friend.getNickname()));
				return getKey() + name;
			}
			return "";
		}

		protected Command executeCommand(Command command) {
			// you were banned
			if (command.isDirectedAtMe()) {
				command.message = Language.getLocalizedString(getClass(), "banned");
				command.isSystem = true;
				UserCommands.RECONNECTALL.executeCommand(command);
			}
			// your friend was banned
			else {
				if (command.directedAt == null) {
					command.ignored = true;
				}
				else {
					try {
						String s = "DENY    " + command.directedAt.getFriendConnection().getSocketAddress().toString().substring(1) + "/32";
						command.ui.getCore().getSettings().getRulelist().add(s);
						command.directedAt.reconnect();
						command.ui.getCore().getSettings().getInternal().setEnableiprules(1);
					} catch (Exception e) {
						//If you can't add to blacklist, remove as friend.
						command.ui.getCore().getFriendManager().permanentlyRemove(command.directedAt);
						e.printStackTrace();
					}
					command.message = Language.getLocalizedString(getClass(), "banned_friend", command.directedAt.getNickname());
					command.isSystem = true;
				}
				}
			return command;
			}
	},
	
	UNBAN("unban", true, "* UNBAN: ") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			String name = args.trim();
			Friend friend = ui.getCore().getFriendManager().getFriend(name);
			if (friend == null) {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "no_such_friend", name));
			}
			else {
				chat.addSystemMessage(Language.getLocalizedString(getClass(), "unbanned_friend", friend.getNickname()));
				return getKey() + name;
			}
			return "";
		}

		protected Command executeCommand(Command command) {
			// you were unbanned
			if (command.isDirectedAtMe()) {
				command.message = Language.getLocalizedString(getClass(), "unbanned");
				command.isSystem = true;
			}
			// your friend was unbanned
			else {
				if (command.directedAt == null) {
					command.ignored = true;
				}
				else {
					//Removes unbanned users IP from the Blacklist
					try {
						for(int i = 0; i < command.ui.getCore().getSettings().getRulelist().size(); i++) {
							if(command.ui.getCore().getSettings().getRulelist().get(i).toString().equals("DENY    " + command.directedAt.getLastKnownHost() + "/32")) {
								command.ui.getCore().getSettings().getRulelist().remove(i);
								break;
							}
						}
						command.directedAt.reconnect();
					} catch (Exception e) {
						//Do nothing as it wasn't this person's friend
					}
					command.message = Language.getLocalizedString(getClass(), "unbanned_friend", command.directedAt.getNickname());
					command.isSystem = true;
				}
				}
			return command;
			}
	},
	PLEASEUPDATE("pleaseupdate", true, null){
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			Collection<Friend> friends = ui.getCore().getFriendManager().friends();
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "pleaseupdate", ""+SiteUpdate.getSiteBuild()));
			for (Friend friend : friends) {
				if(friend.getAllianceBuildNumber() < SiteUpdate.getSiteBuild()){
				try {
					new PrivateChatMessageMDIWindow(ui, friend.getGuid()).sendMessage(SYSTEM.getKey() + Language.getLocalizedString(getClass(), "pleaseupdate", ""+SiteUpdate.getSiteBuild()));
				}
				catch (Exception e) {
					chat.addSystemMessage(Language.getLocalizedString(getClass(), "msg_invalid", friend.getNickname()));
					e.printStackTrace();
				}
				}
		}
			return "";
		}
		protected Command executeCommand(Command command) {
			return null;
		}
		
	},
	
	EXIT("exit") {
		protected String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
			chat.addSystemMessage(Language.getLocalizedString(getClass(), "exiting"));
			ui.getCore().shutdown();
	        System.exit(0);
			return "";
		}

		protected Command executeCommand(Command command) {
			return null;
		}
	};
	
	private final String name;
	private final boolean adminOnly;
	private final String commandKey;
	
	private UserCommands(String name) {
		this(name, false, null);
	}
	
	private UserCommands(String name, boolean adminOnly, String commandKey) {
		this.name = name;
		this.adminOnly = adminOnly;
		this.commandKey = commandKey;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isAdminOnly() {
		return adminOnly;
	}
	
	public String getKey() {
		return commandKey;
	}
	
	protected abstract String execute(String args, UISubsystem ui, AbstractChatMessageMDIWindow chat);
	
	protected abstract Command executeCommand(Command command);
	
	public static UserCommands getCommand(String message) {
		message = message.trim().toLowerCase();
		for (UserCommands cmd : UserCommands.values()) {
			if (message.startsWith("/" + cmd.getName() + " ") || message.endsWith("/" + cmd.getName())) {
				return cmd;
			}
			else if (message.startsWith("/" + "quit" + " ") || message.endsWith("/" + "quit")) {
				return UserCommands.EXIT;
			}
			else if (message.startsWith("/" + "chat" + " ") || message.endsWith("/" + "chat")) {
				return UserCommands.MESSAGE;
			}
		}
		return null;
	}
	
	public static String handleOutCommand(String message, UISubsystem ui, AbstractChatMessageMDIWindow chat) {
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
	
	public static class Command {
		private int guid;
		private String message, name = "";
		private Friend from, directedAt;
		private UISubsystem ui;
		private boolean ignored = false, isSystem = false;
		private UserCommands cmd = null;
		public Command(int guid, String message, UISubsystem ui){
			this.guid = guid;
			this.message = message;
			this.ui = ui;
			this.from = ui.getCore().getFriendManager().getFriend(guid);
			checkIgnored();
		}

		private boolean isAdminCommand() {
			if(!(from.isAdmin())) {
				return false;
			}
			else {
				for(UserCommands c : UserCommands.values()){
					if(c.getKey() != null)
					{
						if(c.isAdminOnly() && message.startsWith(c.getKey())){
							name = message.substring(c.getKey().length()).trim();
							directedAt = ui.getCore().getFriendManager().getFriend(name);
							guid = 0;
							cmd = c;
							return true;
						}
					}
				}
			}
			
			return false;
		}
		
		private boolean isDirectedAtMe(){
			return ui.getCore().getFriendManager().getMe().getNickname().equals(name) 
				|| ui.getCore().getSettings().getMy().getNickname().equals(name);
		}
		
		private void checkIgnored() {
			if (from != null) {
				this.ignored = from.isIgnored();
		    }
		}
		    
		public boolean isIgnored() {
			return ignored;
		}
		
		public Command execute(){
			if(isAdminCommand()){
				return cmd.executeCommand(this);
			}
			return this;
		}
		
		public boolean isSystem(){
			return isSystem;
		}
		
		public int getGuid() {
			return guid;
		}

		public String getMessage() {
			return message;
		}
	}
}
