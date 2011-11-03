package org.alliance.ui.windows.mdi.chat;

/**
 * TODO: (Organize Code)
 * I feel like this class is disorganized. Code for handling input and output
 * messages is scattered in various methods. Filtering code to prevent HTML
 * injection is duplicated, sometimes incompletely. It needs refactoring.
 * After we're done implementing user commands and this file is stable,
 * I'll try and understand how its methods call each other and how they
 * interact with the extending classes (since this is abstract).
 * -- Rangi
 * P.S. This is probably overly OCD of me, but we could also run an automated
 * code formatter on all the source files. It would get rid of irrelevant
 * comments like the "Created by IntelliJ IDEA" below, make indentation
 * and brace placement consistent, etc. They're the sort of things that I'm
 * compelled to change anyway when I open a file to code in it, so might as
 * well get a machine to do it for me. I'll hold off on that, though, because
 * it would play hell with the SVN diffs.
 */

import com.stendahls.nif.ui.mdi.MDIManager;
import com.stendahls.nif.ui.mdi.MDIWindow;
import org.alliance.core.file.hash.Hash;
import org.alliance.core.Language;
import org.alliance.misc.UserCommands;
import org.alliance.ui.T;
import org.alliance.ui.UISubsystem;
import org.alliance.ui.util.CutCopyPastePopup;
import org.alliance.ui.dialogs.OptionDialog;
import org.alliance.ui.windows.mdi.AllianceMDIWindow;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * Created by IntelliJ IDEA.
 * User: maciek
 * Date: 2006-jan-05
 * Time: 13:21:55
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractChatMessageMDIWindow extends AllianceMDIWindow implements Runnable {
	private static final long serialVersionUID = -562921870993210155L;
	
	// TODO Too many similar constants should probably become an enum, which
	// would also let us move functionality into enum methods
	
    protected static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    protected static final DateFormat SHORT_FORMAT = new SimpleDateFormat("HH:mm");
    
    protected static final int MAX_NAME_DISPLAY_LENGTH = 20;
    
    protected static final Color DATE_COLOR = new Color(0x9F9F9F); // light gray
    protected static final Color SYSTEM_COLOR = new Color(0x708090); // slate gray
    protected static final Color ADMIN_COLOR = new Color(0xD81818); // red
    protected static final Color OWN_TEXT_COLOR = new Color(0x000000); // black
    protected static final Color COLORS[] = {
    	new Color(0xD87818), // orange
    	new Color(0x984808), // dark orange/brown
    	new Color(0xB88828), // tan
    	new Color(0x784818), // dark tan
    	new Color(0xD8D818), // yellow
    	new Color(0x989808), // dark yellow/olive
    	new Color(0x18D818), // green/lime
    	new Color(0x089808), // dark green
    	new Color(0x98D818), // yellow-green
    	new Color(0x589808), // dark yellow-green
    	new Color(0x18D8D8), // cyan
    	new Color(0x089898), // dark cyan/teal
    	new Color(0x1868F8), // bright blue
    	new Color(0x083898), // deep blue
    	new Color(0x1818D8), // blue
    	new Color(0x080898), // dark blue/navy
    	new Color(0x8818F8), // grape
    	new Color(0x480898), // dark grape
    	new Color(0xD818D8), // fuchsia
    	new Color(0x980898), // purple
    	new Color(0x888888), // gray
    	new Color(0x484848) // dark gray
    };
    
    protected JEditorPane textarea;
    protected JTextField chat;
    protected String html = "";
    protected TreeSet<ChatLine> chatLines;
    protected boolean needToUpdateHtml;
    protected ChatLine previousChatLine = null;
    private boolean alive = true;

    protected AbstractChatMessageMDIWindow(MDIManager manager, String mdiWindowIdentifier, UISubsystem ui) throws Exception {
        super(manager, mdiWindowIdentifier, ui);
        this.ui = ui;
        chatLines = new TreeSet<ChatLine>(new Comparator<ChatLine>() {

            @Override
            public int compare(ChatLine o1, ChatLine o2) {
                long diff = o1.tick - o2.tick;
                if (diff <= Integer.MIN_VALUE) {
                    diff = Integer.MIN_VALUE + 1;
                }
                if (diff >= Integer.MAX_VALUE) {
                    diff = Integer.MAX_VALUE - 1;
                }
                return (int) diff;
            }
        });
    }

    protected abstract void send(final String text) throws IOException, Exception;

    @Override
    public abstract String getIdentifier();

    @Override
    protected void postInit() {
        textarea = new JEditorPane("text/html", "");
        if (ui.getCore().getSettings().getInternal().getChatfont() != null && !ui.getCore().getSettings().getInternal().getChatfont().isEmpty()) {
            textarea.setFont(new Font(ui.getCore().getSettings().getInternal().getChatfont(), Font.PLAIN,
                    ui.getCore().getSettings().getInternal().getChatsize()));
        }
        new CutCopyPastePopup(textarea);

        JScrollPane sp = (JScrollPane) xui.getComponent("scrollpanel");
        sp.setViewportView(textarea);

        chat = (JTextField) xui.getComponent("chat");
        chat.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                chat.selectAll();
            }
        });

        new CutCopyPastePopup(chat);

        textarea.setEditable(false);
        textarea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

        textarea.setBackground(Color.white);
        textarea.addHyperlinkListener(new HyperlinkListener() {

            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        String link = e.getDescription();
                        if (link.startsWith("https://") || link.startsWith("http://") || link.startsWith("ftp://")) {
                            String allowedChars = "abcdefghijklmnopqrstuvwxyz\u00e5\u00e4\u00f60123456789-%.;/?:@&=+$_.!~*'()#";
                            for (int i = 0; i < link.length(); i++) {
                                if (allowedChars.indexOf(link.toLowerCase().charAt(i)) == -1) {
                                    OptionDialog.showInformationDialog(ui.getMainWindow(), Language.getLocalizedString(getClass(), "invalid", Character.toString(link.charAt(i))));
                                    return;
                                }
                            }
                            ui.openURL(link);
                        } else if (link.contains("|")) {
                            String[] hashes = link.split("\\|");
                            for (String s : hashes) {
                                if (T.t) {
                                    T.trace("Part: " + s);
                                }
                                if (s.length() < 2) {
                                    OptionDialog.showInformationDialog(ui.getMainWindow(), Language.getLocalizedString(getClass(), "notallowed"));
                                    return;
                                }
                            }
                            int guid = Integer.parseInt(hashes[0]);
                            if (OptionDialog.showQuestionDialog(ui.getMainWindow(), Language.getLocalizedString(getClass(), "addlink", Integer.toString(hashes.length - 1)))) {
                                ArrayList<Integer> al = new ArrayList<Integer>();
                                al.add(guid);
                                for (int i = 1; i < hashes.length; i++) {
                                    ui.getCore().getNetworkManager().getDownloadManager().queDownload(new Hash(hashes[i]), Language.getLocalizedString(getClass(), "linkfromchat"), al);
                                }
                                ui.getMainWindow().getMDIManager().selectWindow(ui.getMainWindow().getDownloadsWindow());
                            }
                        } else {
                            OptionDialog.showInformationDialog(ui.getMainWindow(), Language.getLocalizedString(getClass(), "notallowed"));
                            return;
                        }
                    } catch (IOException e1) {
                        ui.getCore().reportError(e1, this);
                    } catch (NumberFormatException e1) {
                        OptionDialog.showInformationDialog(ui.getMainWindow(), Language.getLocalizedString(getClass(), "notallowed"));
                        return;
                    }
                }
            }
        });

        Thread t = new Thread(this);
        t.start();

        super.postInit();
    }

    public void EVENT_send(ActionEvent e) throws Exception {
        chatMessage();
    }

    public void EVENT_chat(ActionEvent e) throws Exception {
        chatMessage();
    }

    public void chatClear() {
        chatLines.clear();
        regenerateHtml();
        needToUpdateHtml = true;
        chat.setText("");
    }

    private void chatMessage() throws Exception {
    	String message = chat.getText();
    	message = UserCommands.handleOutCommand(message, ui, this);
    	if (message != null && !message.trim().equals("")) {
    		if (message.startsWith(UserCommands.ME.getKey())) {
    			message = "* " + ui.getCore().getFriendManager().getMe().getNickname() + " " + message.substring(UserCommands.ME.getKey().length()).trim(); 
    		}
    		// Escape HTML tags, but allow HTML entities like &eacute; or &#x123;
    		send(message.replace("<", "&lt;").replace(">", "&gt;"));
    	}
        chat.setText("");
    }

    private String checkLinks(String text, String pString) {
        int i = 0;
        while ((i = text.indexOf(pString, i)) != -1) {
            int end = text.indexOf(' ', i);
            if (end == -1) {
                end = text.length();
            }
            String displayText =  text.substring(i, end);
            //Shorten long links as to not create side scrolling chat.
            if(displayText.length() > 75) {
            	displayText = displayText.substring(i, 75) + "&hellip;";
            }
            String s = text.substring(0, i);
            s += "<a href=\"" + text.substring(i, end) + "\">" + displayText + "</a>";
            i = s.length();
            if (end < text.length() - 1) {
                s += text.substring(end);
            }
            text = s;
        }
        return text;
    }

    private String escapeHTML(String text) { //Https/ftp support;
        text = text.replace("<", "&lt;"); //unHTML
        text = text.replace(">", "&gt;");
        text = text.replace("&lt;a href=\"", "");

        if (text.endsWith(" files)") && text.contains("|") && text.contains(" in ")) {
            text = text.replace("\"&gt;", " ");
            text = text.replace("&lt;/a&gt;", "");
            String link = text.substring(0, text.indexOf(" "));
            String link2 = text.substring(text.indexOf(" ") + 1, text.lastIndexOf(" ("));
            text = text.replace(link, "<a href=\"" + link + "\">");
            text = text.replace(" " + link2, link2 + "</a>");
        } else {
            text = text.replaceAll("\"&gt;\\S*&lt;/a&gt;", "");
            text = checkLinks(text, "https://");
            text = checkLinks(text, "http://");
            text = checkLinks(text, "ftp://");
        }
        text = text.trim();
        return text;
    }
    
    protected ChatLine createChatLine(String from, String message, long tick, boolean escape) {
        Color c = null;
        if (ui.getCore().getFriendManager().isAdmin(from)) {
        	c = ADMIN_COLOR;
        }
        else if (ui.getCore().getFriendManager().isSystem(from)) {
        	c = SYSTEM_COLOR;
        }
        else {
        	int n = from.hashCode();
        	if (n < 0) {
        		n = -n;
        	}
        	c = COLORS[n % COLORS.length];
        }
        return new ChatLine(from, message, tick, c, escape);
    }
    
    public void addSystemMessage(String message) {
    	// from=null for system messages
    	// system messages are local 
    	addMessage(null, "<i>" + message + "</i>", System.currentTimeMillis(), false, false, false);
    }
    
    public void addMessage(String from, String message, long tick, boolean messageHasBeenQueuedAwayForAWhile) {
        addMessage(from, message, tick, messageHasBeenQueuedAwayForAWhile, true);
    }
    
    public void addMessage(String from, String message, long tick, boolean messageHasBeenQueuedAwayForAWhile, boolean saveToHistory) {
    	addMessage(from, message, tick, messageHasBeenQueuedAwayForAWhile, saveToHistory, true);
    }

    public void addMessage(String from, String message, long tick, boolean messageHasBeenQueuedAwayForAWhile, boolean saveToHistory, boolean escape) {
        if (chatLines != null && chatLines.size() > 0) {
            if (!messageHasBeenQueuedAwayForAWhile) {
                //A message gets queued away when a user is offline and cannot receive the message.
                //If this message has NOT been queued then it should always be displayed as the last message received 
                //in the chat
                tick = System.currentTimeMillis();
            }
        }
        if (tick > System.currentTimeMillis()) {
            //this can happen when another user has an incorrectly set clock. We dont't allow timestamps in the future.
            tick = System.currentTimeMillis();
        }
        
        ChatLine cl = createChatLine(from, message, tick, escape);
    	while (chatLinesContainTick(tick)) {
    		tick++;
    	}
        chatLines.add(cl);
        
        int maxLines = ui.getCore().getSettings().getInternal().getChatmaxlines();
        boolean removeFirstLine = maxLines > 0 && chatLines.size() > maxLines;
        if (removeFirstLine) {
            chatLines.remove(chatLines.first());
        }
        if (chatLines.last() == cl && !removeFirstLine) {
        	html += createHtmlChatLine(cl);
        } else {
            regenerateHtml();
        }

        needToUpdateHtml = true;

        if (saveToHistory) {
            String chatID = this.toString().replaceAll(".*\\{|\\}.*|Private chat with ", "");
            ui.getCore().addToHistory(chatID, from, message, tick);
        }
    }

    private boolean chatLinesContainTick(long tick) {
        for (ChatLine cl : chatLines) {
            if (cl.tick == tick) {
                return true;
            }
        }
        return false;
    }

    protected void regenerateHtml() {
        if (T.t) {
            T.info("Regenerating entire html for chat");
        }
        html = "";
        for (ChatLine chatLine : chatLines) {
            html += createHtmlChatLine(chatLine);
        }
    }

    @Override
    public void run() {
        while (alive) {
            if (needToUpdateHtml) {
                needToUpdateHtml = false;
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        textarea.setText(html);
                    }
                });
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }

	private String createHtmlChatLine(ChatLine cl) {
		// This would read more cleanly if we used some sort of FormattedStringBuilder.
		// Pseudocode:
		// Color color = [lots of if-else logic to determine color];
		// Date date = [date-fetching code];
		// s.addFormatted("<font color='%s'>%s</font>", color, name);
		// Kind of like the <%PARAMETERS%> in the language files.
		StringBuilder s = new StringBuilder();
		// date
		s.append("<font color=\"" + toHexColor(DATE_COLOR) + "\">");
		DateFormat f = new SimpleDateFormat("yyyy-MM-dd");
		if (previousChatLine != null &&
				f.format(new Date(cl.tick)).equals(
				f.format(new Date(previousChatLine.tick)))) {
			s.append("[" + SHORT_FORMAT.format(new Date(cl.tick)) + "]");
		}
		else {
			s.append("<b>[" + FORMAT.format(new Date(cl.tick)) + "]</b>");
		}
		s.append("</font> ");
		// name
		String name = cl.from;
		if (name != null && name.length() > MAX_NAME_DISPLAY_LENGTH) {
			name = name.substring(0, MAX_NAME_DISPLAY_LENGTH) + "&hellip;";
		}
		boolean italic = false;
		if (cl.from == null) {
			// * System message or admin command
			s.append("<font color=\"" + toHexColor(SYSTEM_COLOR) + "\"><i>* ");
			italic = true;
		}
		else if (isUserAction(cl)) {
			// * Username is actioning.
			s.append("<font color=\"" + toHexColor(cl.color) + "\"><i>");
			italic = true;
		}
		else if (cl.from.equals(ui.getCore().getFriendManager().getMe().getNickname())) {
			// Your own messages appear specially
			s.append("<font color=\"" + toHexColor(cl.color) + "\"><b>" + name +
					":</b></font> <font color=\"" + toHexColor(OWN_TEXT_COLOR) + "\">");
		}
		else {
			s.append("<font color=\"" + toHexColor(cl.color) + "\">" + name +
					":</font> <font color=\"" + toHexColor(cl.color.darker()) + "\">");
		}
		if(cl.message.contains("@" + ui.getCore().getFriendManager().getMe().getNickname())) {
			cl.message = cl.message.replace("@" + ui.getCore().getFriendManager().getMe().getNickname(),
					"<SPAN style=\"BACKGROUND-COLOR: #ffff00\">" + "@" + ui.getCore().getFriendManager().getMe().getNickname() + "</SPAN>");
		} 
		s.append(cl.message);
		// conclude
		if (italic) {
			s.append("</i>");
		}
		s.append("</font><br>");
		previousChatLine = cl;
		return s.toString();
	}

	private boolean isUserAction(ChatLine cl) {
		// These can be done with /me or entered manually.
		// So if Joe types "* Joe is hungry.", it will be interpreted as a
		// user action. Not a big deal, and unavoidable given that we need
		// to stay backwards-compatible with 1.2.2, so we can't change the
		// message protocol to add flags for system or action messages.
		return cl.message.startsWith("* " + cl.from + " ");
	}
	
	protected String toHexColor(Color color) {
        return "#" + Integer.toHexString(color.getRGB() & 0xFFFFFF);
    }

    public class ChatLine {

        String from;
		String message;
        long tick;
        Color color;

        public ChatLine(String from, String message, long tick, Color color) {
            this(from, message, tick, color, true);
        }
        
        public ChatLine(String from, String message, long tick, Color color, boolean escape) {
            this.from = from;
            this.message = escape ? escapeHTML(message) : message;
            this.tick = tick;
            this.color = color;
        }
    }

    @Override
    public void windowClosed() {
        super.windowClosed();
        alive = false;
    }

    @Override
    public void save() throws Exception {
    }

    @Override
    public void revert() throws Exception {
    }

    @Override
    public void serialize(ObjectOutputStream out) throws IOException {
    }

    @Override
    public MDIWindow deserialize(ObjectInputStream in) throws IOException {
        return null;
    }

    public void EVENT_cleanup(ActionEvent a) throws Exception {
        chatClear();
        ui.getCore().getPublicChatHistory().clearHistory();
    }

    public void EVENT_cleanscreen(ActionEvent a) throws Exception {
        chatClear();
    }
}
