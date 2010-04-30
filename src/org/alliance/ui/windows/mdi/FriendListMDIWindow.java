package org.alliance.ui.windows.mdi;

import com.stendahls.nif.ui.mdi.MDIManager;
import com.stendahls.nif.ui.mdi.MDIWindow;
import com.stendahls.util.TextUtils;
import org.alliance.core.LanguageResource;
import org.alliance.core.node.Friend;
import org.alliance.core.node.MyNode;
import org.alliance.core.node.Node;
import org.alliance.ui.UISubsystem;
import org.alliance.ui.dialogs.OptionDialog;
import org.alliance.ui.windows.EditGroupWindow;
import org.alliance.ui.windows.ViewFoundVia;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Created by IntelliJ IDEA.
 * User: maciek
 * Date: 2005-dec-30
 * Time: 16:22:07
 */
public class FriendListMDIWindow extends AllianceMDIWindow {

    private UISubsystem ui;
    private JList list;
    private ImageIcon iconFriendDimmed, iconFriendOld;
    private JLabel statusright;
    private static String[] LEVEL_NAMES;
    private static final String[] LEVEL_ICONS = {"friend_lame", "friend", "friend_cool", "friend_king"};
    private ImageIcon[] friendIcons;
    private ImageIcon[] friendIconsAway;
    private ImageIcon groupIcon;
    private JPopupMenu popup;
    private boolean reSelectIndices = false;

    public FriendListMDIWindow() {
    }

    public FriendListMDIWindow(MDIManager manager, UISubsystem ui) throws Exception {
        super(manager, "friendlist", ui);
        this.ui = ui;
        LanguageResource.translateXUIElements(getClass(), xui.getXUIComponents());

        LEVEL_NAMES = new String[]{LanguageResource.getLocalizedString(getClass(), "rookie"),
                    LanguageResource.getLocalizedString(getClass(), "true"),
                    LanguageResource.getLocalizedString(getClass(), "exp"),
                    LanguageResource.getLocalizedString(getClass(), "king")};

        groupIcon = new ImageIcon(ui.getRl().getResource("gfx/icons/editgroup.png"));
        friendIcons = new ImageIcon[LEVEL_ICONS.length];
        friendIconsAway = new ImageIcon[LEVEL_ICONS.length];
        for (int i = 0; i < LEVEL_ICONS.length; i++) {
            friendIcons[i] = new ImageIcon(ui.getRl().getResource("gfx/icons/" + LEVEL_ICONS[i] + ".png"));
            friendIconsAway[i] = new ImageIcon(ui.getRl().getResource("gfx/icons/" + LEVEL_ICONS[i] + "_away.png"));
        }
        iconFriendDimmed = new ImageIcon(ui.getRl().getResource("gfx/icons/friend_dimmed.png"));
        iconFriendOld = new ImageIcon(ui.getRl().getResource("gfx/icons/friend_old.png"));

        setWindowType(WINDOWTYPE_NAVIGATION);

        statusright = (JLabel) xui.getComponent("statusright");

        createUI();
        setTitle(LanguageResource.getLocalizedString(getClass(), "title"));
    }

    public void update() {
        StringBuilder sb = new StringBuilder();
        sb.append(LanguageResource.getLocalizedString(getClass(), "online")).append(" ");
        sb.append(ui.getCore().getFriendManager().getNFriendsConnected()).append("/").append(ui.getCore().getFriendManager().getNFriends());
        sb.append(" (").append(TextUtils.formatByteSize(ui.getCore().getFriendManager().getTotalBytesShared())).append(")");
        statusright.setText(sb.toString());
        try {
            updateMyLevelInformation();
        } catch (IOException ex) {
        }
    }

    static {
        final SystemFlavorMap sfm = (SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap();
        final String nat = "text/plain";
        final DataFlavor df = new DataFlavor("text/plain; charset=ASCII; class=java.io.InputStream", "Plain Text");
        sfm.addUnencodedNativeForFlavor(df, nat);
        sfm.addFlavorForUnencodedNative(nat, df);
    }

    private void createUI() throws Exception {
        list = (JList) xui.getComponent("friendlist");
        list.setModel(ui.getFriendListModel());
        SystemFlavorMap.getDefaultFlavorMap();
        list.setCellRenderer(new FriendListRenderer());

        popup = (JPopupMenu) xui.getComponent("popup");
        updateMyLevelInformation();
        update();

        list.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    EVENT_viewshare(null);
                } catch (Exception ex) {
                    ui.handleErrorInEventLoop(ex);
                }
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = list.locationToIndex(e.getPoint());
                    boolean b = false;
                    for (int r : list.getSelectedIndices()) {
                        if (r == row) {
                            b = true;
                            break;
                        }
                    }
                    if (!b) {
                        list.getSelectionModel().setSelectionInterval(row, row);
                    }
                    if (list.getSelectedValue() instanceof String) {
                        return;
                    }
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        list.getModel().addListDataListener(new ListDataListener() {

            @Override
            public void intervalAdded(ListDataEvent e) {
                reSelectIndices = true;
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                reSelectIndices = true;
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                reSelectIndices = true;
            }
        });

        list.addListSelectionListener(new ListSelectionListener() {

            private Object[] lastSelected;

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!reSelectIndices) {
                    try {
                        lastSelected = list.getSelectedValues();
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        //Do nothing
                    }
                }

                if (reSelectIndices && lastSelected != null) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            ArrayList<Integer> selectionList = new ArrayList<Integer>();
                            for (int i = 0; i < list.getModel().getSize(); i++) {
                                if (selectionList.size() == lastSelected.length) {
                                    break;
                                }
                                for (Object selection : lastSelected) {
                                    if (list.getModel().getElementAt(i).equals(selection)) {
                                        selectionList.add(i);
                                        break;
                                    }
                                }
                            }

                            int[] selectedIndices = new int[selectionList.size()];
                            for (int i = 0; i < selectedIndices.length; i++) {
                                selectedIndices[i] = selectionList.get(i);
                            }
                            list.setSelectedIndices(selectedIndices);
                        }
                    });
                }
                reSelectIndices = false;
            }
        });

        postInit();
    }

    public void updateMyLevelInformation() throws IOException {
        ((JLabel) xui.getComponent("myname")).setText(ui.getCore().getFriendManager().getMe().getNickname());
        ((JLabel) xui.getComponent("mylevel")).setText(getLevelName(getMyLevel()));
        ((JLabel) xui.getComponent("myicon")).setIcon(new ImageIcon(ui.getRl().getResource(getLevelIcon(getMyLevel(), true))));
        String s = "";
        switch (getMyNumberOfInvites()) {
            case 0:
                s = LanguageResource.getLocalizedString(getClass(), "invite1");
                break;
            case 1:
                s = LanguageResource.getLocalizedString(getClass(), "invite2");
                break;
            case 2:
                s = LanguageResource.getLocalizedString(getClass(), "invite1");
                break;
            default:
                s = LanguageResource.getLocalizedString(getClass(), "invite", Integer.toString(ui.getCore().getFriendManager().getNumberOfInvitesNeededToBeKing() - getMyNumberOfInvites()));
                break;
        }
        if (getMyLevel() < LEVEL_NAMES.length - 1) {
            s += " '" + getLevelName(getMyLevel() + 1) + "' (";
            ((JLabel) xui.getComponent("nextleveltext")).setText(s);
            ((JLabel) xui.getComponent("nextlevelicon")).setIcon(new ImageIcon(ui.getRl().getResource(getLevelIcon(getMyLevel() + 1, false))));
            ((JLabel) xui.getComponent("levelending")).setText(")");
        } else {
            ((JLabel) xui.getComponent("nextleveltext")).setText("");
            ((JLabel) xui.getComponent("nextlevelicon")).setText("");
            ((JLabel) xui.getComponent("nextlevelicon")).setIcon(null);
            ((JLabel) xui.getComponent("levelending")).setText("");
        }
    }

    private String getLevelIcon(int myLevel, boolean big) {
        if (myLevel < 0) {
            myLevel = 0;
        }
        if (myLevel >= LEVEL_ICONS.length) {
            myLevel = LEVEL_ICONS.length - 1;
        }
        return "gfx/icons/" + LEVEL_ICONS[myLevel] + (big ? "_big" : "") + ".png";
    }

    private String getLevelName(int myLevel) {
        if (myLevel < 0) {
            myLevel = 0;
        }
        if (myLevel >= LEVEL_NAMES.length) {
            myLevel = LEVEL_NAMES.length - 1;
        }
        return LEVEL_NAMES[myLevel];
    }

    private int getMyLevel() {
        return getLevel(getMyNumberOfInvites());
    }

    private int getLevel(int numberOfInvites) {
        switch (numberOfInvites) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 1;
            case 3:
                return 2;
            default:
                if (numberOfInvites >= ui.getCore().getFriendManager().getNumberOfInvitesNeededToBeKing()) {
                    return 3;
                }
                return 2;
        }
    }

    private int getMyNumberOfInvites() {
        return ui.getCore().getSettings().getMy().getInvitations();
    }

    @Override
    public void save() throws Exception {
    }

    @Override
    public String getIdentifier() {
        return "friendlist";
    }

    @Override
    public void revert() throws Exception {
        ui.getCore().invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    ui.getCore().refreshFriendInfo();
                } catch (IOException e) {
                    ui.handleErrorInEventLoop(e);
                }
            }
        });
    }

    @Override
    public void serialize(ObjectOutputStream out) throws IOException {
    }

    @Override
    public MDIWindow deserialize(ObjectInputStream in) throws IOException {
        return null;
    }

    private class FriendListRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            //Groups painting
            if (value instanceof String) {
                setIcon(groupIcon);
                setFont(new Font(this.getFont().getFontName(), Font.BOLD, 12));
                setText(value.toString());
                setBackground(this.getBackground().darker());
                return this;
            }

            Node n = (Node) value;

            String groupname = "";
            String trusted = "";
            String sharesize = TextUtils.formatByteSize(n.getShareSize());
            if (n instanceof Friend) {
                Friend f = (Friend) n;
                groupname = f.getUGroupName();
                if (groupname.length() == 0) {
                    groupname = LanguageResource.getLocalizedString(getClass().getEnclosingClass(), "nogroup");
                }
                if (f.getTrusted() == 1) {
                    trusted = "(T) ";
                }
            } else {
                groupname = LanguageResource.getLocalizedString(getClass().getEnclosingClass(), "wronggroup");
            }

            if (n.isConnected()) {
                if (!n.isAway()) {
                    setIcon(friendIcons[getLevel(n.getNumberOfInvitedFriends())]);
                } else {
                    setIcon(friendIconsAway[getLevel(n.getNumberOfInvitedFriends())]);
                }
                if (isSelected) {
                    setForeground(Color.white);
                } else {
                    setForeground(Color.black);
                }
                String s = "";
                if (n instanceof Friend) {
                    s = trusted + nickname(n.getGuid());
                } else {
                    s = LanguageResource.getLocalizedString(getClass().getEnclosingClass(), "myself") + " - ";
                    s += n.getNickname();
                }
                s += " (" + sharesize + ")";
                setText(s);
            } else if (n.hasNotBeenOnlineForLongTime()) {
                setIcon(iconFriendOld);
                setForeground(Color.lightGray);
                if (n.getLastSeenOnlineAt() != 0) {
                    setText(trusted + LanguageResource.getLocalizedString(getClass().getEnclosingClass(), "myself",
                            nickname(n.getGuid()), Long.toString((System.currentTimeMillis() - n.getLastSeenOnlineAt()) / 1000 / 60 / 60 / 24)));
                } else {
                    setText(trusted + nickname(n.getGuid()));
                }
            } else {
                setIcon(iconFriendDimmed);
                setForeground(Color.lightGray);
                setText(trusted + nickname(n.getGuid()));
            }

            String cp = FriendListMDIWindow.this.ui.getCore().getFriendManager().contactPath(n.getGuid());
            StringBuilder sb = new StringBuilder("<html>");
            if (cp.trim().length() > 0) {
                sb.append(LanguageResource.getLocalizedString(getClass().getEnclosingClass(), "subfriends", cp)).append("<br>");
            }
            sb.append(LanguageResource.getLocalizedString(getClass().getEnclosingClass(), "share", sharesize,
                    Integer.toString(n.getNumberOfFilesShared()))).append("<br>");
            sb.append(LanguageResource.getLocalizedString(getClass().getEnclosingClass(), "invites", Integer.toString(n.getNumberOfInvitedFriends()))).append("<br>");
            sb.append(LanguageResource.getLocalizedString(getClass().getEnclosingClass(), "upspeed", TextUtils.formatByteSize((long) n.getHighestOutgoingCPS()))).append("<br>");
            sb.append(LanguageResource.getLocalizedString(getClass().getEnclosingClass(), "downspeed", TextUtils.formatByteSize((long) n.getHighestIncomingCPS()))).append("<br>");
            sb.append(LanguageResource.getLocalizedString(getClass().getEnclosingClass(), "uptotal", TextUtils.formatByteSize(n.getTotalBytesSent()))).append("<br>");
            sb.append(LanguageResource.getLocalizedString(getClass().getEnclosingClass(), "downtotal", TextUtils.formatByteSize(n.getTotalBytesReceived()))).append("<br>");
            sb.append(LanguageResource.getLocalizedString(getClass().getEnclosingClass(), "ratio", n.calculateRatio())).append("<br>");
            sb.append(LanguageResource.getLocalizedString(getClass().getEnclosingClass(), "group", groupname)).append("</html>");
            setToolTipText(sb.toString());
            return this;
        }
    }

    private String nickname(int guid) {
        return ui.getCore().getFriendManager().nickname(guid);
    }

    public void EVENT_editname(ActionEvent e) {
        if (list.getSelectedValue() instanceof MyNode) {
            OptionDialog.showInformationDialog(ui.getMainWindow(), LanguageResource.getLocalizedString(getClass(), "ownname"));
        } else if (list.getSelectedValue() instanceof Friend) {
            Friend f = (Friend) list.getSelectedValue();
            if (f != null) {
                String pi = JOptionPane.showInputDialog(LanguageResource.getLocalizedString(getClass(), "editname", nickname(f.getGuid())), nickname(f.getGuid()));
                if (pi != null) {
                    f.setNicknameToShowInUI(pi);
                }
                ui.getFriendListModel().signalFriendChanged();
            }
        } else {
            return;
        }
    }

    public void EVENT_chat(ActionEvent e) throws Exception {
        if (list.getSelectedValue() instanceof Friend) {
            Friend f = (Friend) list.getSelectedValue();
            if (f != null) {
                ui.getMainWindow().chatMessage(f.getGuid(), null, 0, false);
            }
        } else {
            return;
        }
    }

    public void EVENT_reconnect(ActionEvent e) throws Exception {
        if (list.getSelectedValue() instanceof Friend) {
            final Friend f = (Friend) list.getSelectedValue();
            if (f.isConnected()) {
                f.reconnect();
            }
        } else {
            return;
        }
    }

    public void EVENT_viewshare(ActionEvent e) throws Exception {
        if (list.getSelectedValue() instanceof MyNode) {
            ui.getMainWindow().EVENT_myshare(null);
        } else if (list.getSelectedValue() instanceof Friend) {
            Friend f = (Friend) list.getSelectedValue();
            if (f != null) {
                if (!f.isConnected()) {
                } else {
                    ui.getMainWindow().viewShare(f);
                }
            }
        } else {
            return;
        }
    }

    public void EVENT_addfriendwizard(ActionEvent e) throws Exception {
        ui.getMainWindow().EVENT_addfriendwizard(e);
    }

    public void EVENT_removefriend(ActionEvent e) throws Exception {
        if (list.getSelectedValue() instanceof Friend) {
            Object[] friends = list.getSelectedValues();
            if (friends != null && friends.length > 0) {
                Boolean delete = OptionDialog.showQuestionDialog(ui.getMainWindow(), LanguageResource.getLocalizedString(getClass(), "delete", Integer.toString(friends.length)));
                if (delete == null) {
                    return;
                }
                if (delete) {
                    for (Object friend : friends) {
                        if (friend instanceof Friend) {
                            Node f = (Node) friend;
                            if (f != null && f instanceof Friend) {
                                ui.getCore().getFriendManager().permanentlyRemove((Friend) f);
                            }
                        }
                    }
                    revert();
                    ui.getFriendListModel().signalFriendChanged();
                }
            } else {
                return;
            }
        }
    }

    /**
     * Changes the hostname of a friend you have in your friendlist via the GUI. Can
     * be used to configure a hostname instead of the IP that's set via an invitation.
     * @param e
     * @author jpluebbert
     */
    public void EVENT_edithostname(ActionEvent e) {
        if (list.getSelectedValue() instanceof Friend) {
            Friend friend = (Friend) list.getSelectedValue();
            if (friend != null) {
                String hostname = friend.getLastKnownHost();

                if (hostname == null) {
                    hostname = "";
                }

                String input = JOptionPane.showInputDialog(LanguageResource.getLocalizedString(getClass(), "edithost", nickname(friend.getGuid())), hostname);
                if (input != null && !hostname.equalsIgnoreCase(input)) {
                    if (input.length() == 0) {
                        OptionDialog.showErrorDialog(ui.getMainWindow(), LanguageResource.getLocalizedString(getClass(), "wronghost"));
                    } else {
                        friend.setLastKnownHost(input);
                        try {
                            if (friend.isConnected()) {
                                friend.reconnect();
                            } else {
                                friend.connect();
                            }
                        } catch (IOException e1) {
                        }
                    }
                }
            } else {
                return;
            }
        }
    }

    public void EVENT_edittrusted(ActionEvent e) {
        if (list.getSelectedValue() instanceof Friend) {
            Object[] friends = list.getSelectedValues();
            if (friends != null && friends.length > 0) {
                for (Object friend : friends) {
                    if (friend instanceof Friend) {
                        Friend f = (Friend) friend;
                        if (f != null) {
                            if (f.getTrusted() == 0) {
                                f.setTrusted(1);
                            } else {
                                f.setTrusted(0);
                            }
                        }
                    }
                }
                try {
                    ui.getCore().saveSettings();
                } catch (Exception ex) {
                }
            }
        } else {
            return;
        }
    }

    public void EVENT_editgroupname(ActionEvent e) throws Exception {
        if (list.getSelectedValue() instanceof Friend) {
            Object[] friends = list.getSelectedValues();
            if (friends != null && friends.length > 0) {
                Friend fr = (Friend) friends[0];
                EditGroupWindow editWindow = new EditGroupWindow(ui, fr.getUGroupName());
                String groupString = editWindow.getGroupString();
                if (groupString == null) {
                    return;
                }
                for (Object friend : friends) {
                    if (friend instanceof Friend) {
                        Friend f = (Friend) friend;
                        if (f != null) {
                            f.setUGroupName(groupString);
                        }
                    }
                }
                ui.getFriendListModel().signalFriendChanged();
                ui.getCore().saveSettings();
            }
        } else {
            return;
        }
    }

    public void EVENT_viewvia(ActionEvent e) {
        if (list.getSelectedValue() instanceof Friend) {
            Friend friend = (Friend) list.getSelectedValue();
            try {
                new ViewFoundVia(ui, friend);
            } catch (Exception ex) {
            }
        } else {
            return;
        }
    }

    public void EVENT_searchfriend(ActionEvent e) {
        String text = ((JTextField) xui.getComponent("searchfield")).getText();
        int nhit = 0;
        for (int i = 0; i < list.getModel().getSize(); i++) {
            Object friend = list.getModel().getElementAt(i);
            if (friend instanceof Friend) {
                for (int x = 1; x <= text.length(); x++) {
                    if (((Friend) friend).getNickname().toLowerCase().startsWith(text.substring(0, x).toLowerCase()) && x > nhit) {
                        list.setSelectedValue(friend, true);
                        nhit++;
                    }
                }
            }
        }
    }
}
