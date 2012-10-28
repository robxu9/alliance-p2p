package org.alliance.ui.windows.mdi.friends;

import com.stendahls.util.TextUtils;

import org.alliance.Version;
import org.alliance.core.Language;
import org.alliance.core.node.Friend;
import org.alliance.core.node.MyNode;
import org.alliance.core.node.Node;
import org.alliance.ui.UISubsystem;
import org.alliance.ui.themes.util.SubstanceThemeHelper;
import org.alliance.ui.themes.AllianceListCellRenderer;
import static org.alliance.ui.windows.mdi.friends.FriendListMDIWindow.LEVEL_ICONS;

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.io.IOException;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author Bastvera
 */
public class FriendListCellRenderer extends AllianceListCellRenderer {

    private ImageIcon groupIcon;
    private ImageIcon iconFriendDimmed, iconFriendOld;
    private ImageIcon[] friendIcons;
    private ImageIcon[] friendIconsAway;
    private FriendListMDIWindow friendWindow;
    private UISubsystem ui;
    private static Color GROUPS_BG;
    private static Font GROUPS_FONT;
    private static Border MARGIN_BORDER;

    FriendListCellRenderer(FriendListMDIWindow friendWindow, UISubsystem ui) throws IOException {
        super(SubstanceThemeHelper.isSubstanceInUse());
        this.friendWindow = friendWindow;
        this.ui = ui;
        groupIcon = new ImageIcon(ui.getRl().getResource("gfx/icons/editgroup.png"));
        iconFriendDimmed = new ImageIcon(ui.getRl().getResource("gfx/icons/friend_dimmed.png"));
        iconFriendOld = new ImageIcon(ui.getRl().getResource("gfx/icons/friend_old.png"));
        friendIcons = new ImageIcon[LEVEL_ICONS.length];
        friendIconsAway = new ImageIcon[LEVEL_ICONS.length];
        for (int i = 0; i < LEVEL_ICONS.length; i++) {
            friendIcons[i] = new ImageIcon(ui.getRl().getResource("gfx/icons/" + LEVEL_ICONS[i] + ".png"));
            friendIconsAway[i] = new ImageIcon(ui.getRl().getResource("gfx/icons/" + LEVEL_ICONS[i] + "_away.png"));
        }
        GROUPS_FONT = new Font(getRenderer().getFont().getFontName(), Font.BOLD, getRenderer().getFont().getSize());
        GROUPS_BG = createGroupBackground(getRenderer().getBackground().getRed(), getRenderer().getBackground().getGreen(), getRenderer().getBackground().getBlue(), 0.85);
        if (getRenderer().getBorder() instanceof EmptyBorder) {
            Insets i = ((EmptyBorder) getRenderer().getBorder()).getBorderInsets();
            MARGIN_BORDER = new EmptyBorder(i.top, 17, i.bottom, i.right);
        } else {
            MARGIN_BORDER = getRenderer().getBorder();
        }
    }

    @Override
    protected void overrideListCellRendererComponent(DefaultListCellRenderer renderer, JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (value instanceof String) {
            paintGroup(renderer, value.toString());
            return;
        }
        Node n = (Node) value;
        paintFriend(renderer, n, isSelected);
        renderer.setToolTipText(setupTooltip(n));
        if (n instanceof Friend) {
            renderer.setBorder(MARGIN_BORDER);
        }
    }

    private Color createGroupBackground(int r, int g, int b, double factor) {
        return new Color(Math.max((int) (r * factor), 0), Math.max((int) (g * factor), 0), Math.max((int) (b * factor), 0));
    }

    private void paintGroup(DefaultListCellRenderer renderer, String group) {
        renderer.setIcon(groupIcon);
        renderer.setFont(GROUPS_FONT);
        renderer.setText(group);
        renderer.setBackground(GROUPS_BG);
    }

    private void paintFriend(DefaultListCellRenderer renderer, Node n, boolean isSelected) {
        String group;
        if (n instanceof Friend) {
            Friend f = (Friend) n;
            if ((f.getUGroupName().equals("") || f.getUGroupName() == null) && !f.isConnected()) {
                group = " (No Group)";
            } else {
                group = "";
            }
        } else {
            group = "";
        }

        if (n.isConnected()) {
            if (!n.isAway()) {
                renderer.setIcon(friendIcons[friendWindow.getLevel(n.getNickname(), n.getNumberOfInvitedFriends())]);
            } else {
                renderer.setIcon(friendIconsAway[friendWindow.getLevel(n.getNickname(), n.getNumberOfInvitedFriends())]);
            }
            if (isSelected) {
                renderer.setForeground(Color.white);
            } else {
                renderer.setForeground(Color.black);
            }
            String nodeString;
            if (n instanceof Friend) {
                nodeString = friendWindow.getNickname(n.getGuid()) + group;
            } else {
                nodeString = n.getNickname();
            }
            nodeString += " (" + TextUtils.formatByteSize(n.getShareSize()) + ")";
            String status = n.getStatus();
            if (n.getGuid() == ui.getCore().getSettings().getMy().getGuid()) {
            	status = ui.getCore().getSettings().getMy().getStatus();
            }
            if (status != null) {
            	status = status.trim();
            	if (status.length() > 80) {
            		status = status.substring(0, 80) + "&hellip;";
            	}
            	if (status.length() > 0) {
            	    nodeString += ": " + status;
            	}
            }
            renderer.setText(nodeString);
        } else {
        	if (n.hasNotBeenOnlineForLongTime()){
        		renderer.setIcon(iconFriendOld);
        	}
        	else {
        		renderer.setIcon(iconFriendDimmed);
        	}
            renderer.setForeground(Color.GRAY);
            renderer.setText(friendWindow.getNickname(n.getGuid()) + group); 
        }
    }

    private String setupTooltip(Node n) {
    	  StringBuilder sb = new StringBuilder("<html>");
    	String status = "";
    	String name = "";
    	int build = 0;
        if (n instanceof Friend) {
            Friend f = (Friend) n;
            name = f.getNickname();
            status = f.getStatus();
            build = f.getAllianceBuildNumber();
        }
        else if (n instanceof MyNode) {
        	status = ui.getCore().getSettings().getMy().getStatus();
        	build = Version.BUILD_NUMBER;
        }
        
        if (build < 1477) {
        	return Language.getLocalizedString(getClass(), "needupgrade", name, "v1.3.0");
        }
        
        //If greater than 70 characters break into two lines
        if(status.length() > 70){
        	int space = status.indexOf(" ", 70);
        	String status1;
        	String status2;
        	if(space > 0){
        	status1 = status.substring(0, space);
        	status2 = status.substring(space);
        	}
        	else{
        		status1 = status.substring(0, 70);
            	status2 = status.substring(70);
        	}
        	//Indent second line
        	status2 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + status2;
        	sb.append(Language.getLocalizedString(getClass(), "currentstatus", status1 + "<br>" + status2)).append("<br>");
        }
        else{
        	sb.append(Language.getLocalizedString(getClass(), "currentstatus", status)).append("<br>");
        }
        return sb.toString();
    }
}
