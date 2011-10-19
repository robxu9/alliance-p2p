package org.alliance.ui.windows.mdi.search;

import com.stendahls.nif.ui.mdi.MDIWindow;
import com.stendahls.nif.ui.mdi.infonodemdi.InfoNodeMDIManager;
import com.stendahls.ui.JHtmlLabel;
import com.stendahls.util.TextUtils;
import org.alliance.core.Language;
import org.alliance.core.comm.SearchHit;
import org.alliance.core.file.filedatabase.FileType;
import org.alliance.core.PacedRunner;
import org.alliance.ui.UISubsystem;
import org.alliance.ui.util.CutCopyPastePopup;
import org.alliance.ui.windows.mdi.AllianceMDIWindow;
import org.jdesktop.swingx.JXTreeTable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

/**
 * Created by IntelliJ IDEA.
 * User: maciek
 * Date: 2006-jan-05
 * Time: 13:21:55
 * To change this template use File | Settings | File Templates.
 */
public class SearchMDIWindow extends AllianceMDIWindow {
	private static final long serialVersionUID = 327451975739712776L;
	
	private JXTreeTable table;
    private SearchTreeTableModel model;
    private JComboBox type;
    private JPopupMenu popup;
    private JLabel left, right;
    private PacedRunner pacedRunner;
    private JTextField search;
    private ImageIcon[] fileTypeIcons;
    private UISubsystem ui;

    public SearchMDIWindow(final UISubsystem ui) throws Exception {
        super(ui.getMainWindow().getMDIManager(), "search", ui);
        this.ui = ui;
        Language.translateXUIElements(getClass(), xui.getXUIComponents());

        fileTypeIcons = new ImageIcon[FileType.values().length];
        for (int i = 0; i < fileTypeIcons.length; i++) {
            fileTypeIcons[i] = new ImageIcon(ui.getRl().getResource("gfx/filetypes/" + i + ".png"));
        }

        left = (JLabel) xui.getComponent("left");
        right = (JLabel) xui.getComponent("right");
        search = (JTextField) xui.getComponent("search1");
        new CutCopyPastePopup(search);

        pacedRunner = new PacedRunner(500);

        table = new JXTreeTable(model = new SearchTreeTableModel(ui.getCore(), pacedRunner));
        table.setColumnControlVisible(false);

        table.getTableHeader().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                JTableHeader h = (JTableHeader) e.getSource();
                TableColumnModel columnModel = h.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = columnModel.getColumn(viewColumn).getModelIndex();
                if (column != -1) {
                    switch (column) {
                        case 0:
                            model.getRoot().sortByName();
                            break;
                        case 1:
                            model.getRoot().sortBySize();
                            break;

                        case 3:
                            model.getRoot().sortByDaysAgo();
                            break;
                        case 4:
                            model.getRoot().sortBySources();
                            break;
                        case 5:
                            model.getRoot().sortBySpeed();
                            break;
                    }
                }
            }
        });
        table.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (table.getPathForLocation(e.getX(), e.getY()) != null) {
                    SearchTreeNode n = (SearchTreeNode) table.getPathForLocation(e.getX(), e.getY()).getLastPathComponent();
                    if (n != null) {
                        if (n instanceof FileNode) {
                            FileNode fn = (FileNode) n;
                            double d = fn.getTotalMaxCPS();
                            double r = ui.getCore().getSettings().getInternal().getRecordinspeed();
                            StringBuilder sb = new StringBuilder("<html>");
                            sb.append(fn.getListOfUsers(ui.getCore()));
                            sb.append(" (").append(Language.getLocalizedString(getClass(), "speed")).append(" <b>").append(TextUtils.formatByteSize((long) d)).append("/s</b>, ");
                            sb.append(Language.getLocalizedString(getClass(), "eta")).append(" ").append(formatETA((int) Math.round(fn.getSize() / (d > r ? r : d)))).append("</font>)</html>");
                            left.setText(sb.toString());
                            right.setText("<html><b>" + simplifyPath(fn) + "</b></html>");
                        }
                    }
                }
            }

            private String formatETA(int eta) {
                if (eta < 0) {
                    return "?";
                } else if (eta <= 60) {
                    return eta + " sec";
                } else if (eta / 60 < 60) {
                    return eta / 60 + " min";
                } else {
                    return (eta / 60 / 60) + "h " + (eta / 60 % 60) + "m";
                }
            }
        });

        table.addMouseListener(new MouseAdapter() {

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
                if (e.getClickCount() >= 2) {
                    EVENT_download(null);
                }
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = table.rowAtPoint(e.getPoint());
                    boolean b = false;
                    for (int r : table.getSelectedRows()) {
                        if (r == row) {
                            b = true;
                            break;
                        }
                    }
                    if (!b) {
                        table.getSelectionModel().setSelectionInterval(row, row);
                    }
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        table.setAutoCreateColumnsFromModel(false);
        table.setTreeCellRenderer(new NameCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new BytesizeCellRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new DaysOldCellRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new SourcesCellRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new SpeedCellRenderer());

        table.getColumnModel().getColumn(0).setPreferredWidth(500);
        setFixedColumnSize(table.getColumnModel().getColumn(1), 50);
        setFixedColumnSize(table.getColumnModel().getColumn(2), 50);
        setFixedColumnSize(table.getColumnModel().getColumn(3), 60);
        setFixedColumnSize(table.getColumnModel().getColumn(4), 25);
        setFixedColumnSize(table.getColumnModel().getColumn(5), 40);

        table.getColumnExt(2).setVisible(false); //tricky! the index points to the visible columns!

        ((JScrollPane) xui.getComponent("treepanel")).setViewportView(table);

        type = (JComboBox) xui.getComponent("type");
        popup = (JPopupMenu) xui.getComponent("popup");

        for (FileType v : FileType.values()) {
            type.addItem(v.description());
        }
        setTitle(Language.getLocalizedString(getClass(), "title"));
        postInit();
    }

    private String simplifyPath(FileNode fn) {
        String path = fn.getSh().getPath();
        String s = fn.getName(); // originalFilename
        if (fn.getParent() != null && fn.getParent() instanceof FolderNode) {
            s = ((FolderNode) fn.getParent()).getOriginalName() + "/" + s;
        }
        if (path.endsWith(s)) {
            path = path.substring(0, path.length() - s.length());
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.equalsIgnoreCase(fn.getName())) {
            path = "";
        }
        return path;
    }

    private void setFixedColumnSize(TableColumn column, int i) {
        column.setPreferredWidth(i);
        column.setMaxWidth(i);
        column.setMinWidth(i);
    }

    public void EVENT_download(ActionEvent e) {
        int selection[] = table.getSelectedRows();

        if (selection != null && selection.length > 0) {
            for (int i : selection) {
                boolean changeWindow = false;
                final String path = getPathIfPathSelected(i);
                for (final FileNode n : getFileNodesByRow(i)) {
                    if (ui.getCore().getFileManager().containsComplete(n.getSh().getRoot())) {
                        ui.getCore().getUICallback().statusMessage(Language.getLocalizedString(getClass(), "have", n.getName()));
                    } else if (ui.getCore().getNetworkManager().getDownloadManager().getDownload(n.getSh().getRoot()) != null) {
                        ui.getCore().getUICallback().statusMessage(Language.getLocalizedString(getClass(), "download", n.getName()));
                    } else {
                        ui.getCore().invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    String name = n.getName(); // originalFilename
                                    if (path.trim().length() > 0) {
                                        name = path + "/" + name;
                                    }
                                    ui.getCore().getNetworkManager().getDownloadManager().queDownload(n.getSh().getRoot(), name, n.getUserGuids());
                                } catch (IOException e1) {
                                    ui.handleErrorInEventLoop(e1);
                                }
                            }
                        });
                        changeWindow = true;
                    }
                }
                if (changeWindow) {
                    ui.getMainWindow().getMDIManager().selectWindow(ui.getMainWindow().getDownloadsWindow());
                }
            }
        }
    }

    private String getPathIfPathSelected(int row) {
        SearchTreeNode n = (SearchTreeNode) table.getPathForRow(row).getLastPathComponent();
        if (n instanceof FolderNode) {
            FolderNode fn = (FolderNode) n;
            return fn.getName();
        }
        return "";
    }

    private ArrayList<FileNode> getFileNodesByRow(int row) {
        SearchTreeNode n = (SearchTreeNode) table.getPathForRow(row).getLastPathComponent();
        ArrayList<FileNode> al = new ArrayList<FileNode>();
        if (n instanceof FileNode) {
            al.add((FileNode) n);
        } else {
            FolderNode fn = (FolderNode) n;
            for (int i = 0; i < fn.getChildCount(); i++) {
                al.add((FileNode) fn.getChildAt(i));
            }
        }
        return al;
    }

    public void searchHits(int sourceGuid, int hops, java.util.List<SearchHit> hits) {
        model.addSearchHits(sourceGuid, hops, hits);

        SearchTreeNode root = model.getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            SearchTreeNode n = (SearchTreeNode) root.getChildAt(i);
            if (n.getChildCount() == 1) {
                table.expandPath(new TreePath(new Object[]{root, n}));
            }
        }
    }

    @Override
    public void windowSelected() {
        super.windowSelected();
    }

    public void EVENT_search1(ActionEvent e) throws IOException {
        search(search.getText());
    }

    public void EVENT_search2(ActionEvent e) throws IOException {
        search(search.getText());
    }

    public void search(String text) throws IOException {
    	final FileType ft = FileType.values()[type.getSelectedIndex()];
        String s;
        if (text.trim().length() == 0) {
            s = Language.getLocalizedString(getClass(), "searchall", ft.description());
        }
        else {
            s = Language.getLocalizedString(getClass(), "searchtype", text, ft.description());
            text = searchQuery(text);
        }
        final String t = text;
        left.setText(s);
        table.setTreeTableModel(model = new SearchTreeTableModel(ui.getCore(), pacedRunner));
        ui.getCore().invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    ui.getCore().getFriendManager().getNetMan().sendSearch(t, ft);
                } catch (IOException e) {
                    ui.handleErrorInEventLoop(e);
                }
            }
        });
    }
    
    private String searchQuery(String query) {
    	query = query.toLowerCase() // case-insensitive search
    		.trim().replaceAll("\\s+", " "); // collapse whitespace
        String search = "";
        if (!query.startsWith("*")) {
        	search += "%"; // allow anything before search terms
        }
        int n = query.length();
        boolean escaped = false;
        for (int i = 0; i < n; i++) {
        	char c = query.charAt(i);
        	if (escaped) {
        		if (c == '*') {
        			search += "*"; // \* matches literal *
        		}
        		else if (c == '?') {
        			search += "?"; // \? matches literal ?
        		}
        		else if (c == '\\') {
        			search += "\\"; // \\ matches literal \
        		}
        		else if (c == ' ') {
        			search += " "; // \space  matches literal space
        		}
        		else {
        			search += "\\" + c; // bad escape sequence; treat \ literally
        		}
        		escaped = false;
        	}
        	else {
        		if (c == '*' || c == ' ') {
        			search += "%"; // * and space match any characters
        		}
        		else if (c == '%') {
        			search += "\\%"; // since SQL LIKE uses % instead of *
        		}
        		else if (c == '?') {
        			search += "_"; // ? matches any single character
        		}
        		else if (c == '_') {
        			search += "\\_"; // since SQL LIKE uses _ instead of ?
        		}
        		else if (c == '\\') {
        			escaped = true; // \ escapes the next character
        		}
        		else {
        			search += c; // literal character
        		}
        	}
        }
        if (escaped) {
        	search += "\\"; // incomplete escape sequence; treat \ literally
        }
        if (!search.endsWith("%")) {
        	search += "%"; // allow anything after search terms
        }
        return search;
    }

    @Override
    public String getIdentifier() {
        return "Search";
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

    public void EVENT_keywords(ActionEvent e) {
        search.setEnabled(true);
    }

    public void EVENT_newfiles(ActionEvent e) {
        search.setEnabled(false);
    }

    public class NameCellRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 6868290504010574493L;

		@Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof FileNode) {
                FileNode fn = (FileNode) value;
                setIcon(fileTypeIcons[FileType.getByFileName(fn.getSh().getPath()).id()]);
                if (sel) {
                    setForeground(Color.white);
                } else {
                    setForeground(fn.containedInShare(SearchMDIWindow.this.ui.getCore()) ? Color.gray : Color.black);
                }
            } else if (value instanceof FolderNode) {
                FolderNode fn = (FolderNode) value;
                if (sel) {
                    setForeground(Color.white);
                } else {
                    setForeground(fn.containedInShare(SearchMDIWindow.this.ui.getCore()) ? Color.gray : Color.black);
                }
            }
            return this;
        }
    }

    public class BytesizeCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1215936842757257676L;

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
            setText(TextUtils.formatByteSize((Long) value));
            setToolTipText(String.valueOf(value));
            return this;
        }

        @Override
        public void validate() {
        }

        @Override
        public void revalidate() {
        }

        @Override
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        }

        @Override
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        }
    }

    public class DaysOldCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 2523851970624324107L;

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);
            int val = (Integer) value;
            if (val == 255) {
                setText(Language.getLocalizedString(getClass().getEnclosingClass(), "dayold"));
            } else {
                setText(String.valueOf(val));
            }
            return this;
        }

        @Override
        public void validate() {
        }

        @Override
        public void revalidate() {
        }

        @Override
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        }

        @Override
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        }
    }

    public class SourcesCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -7492092394119241390L;

		@Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, rowIndex, vColIndex);

            double val = (Double) value;
            if (val >= 10) {
                setText("" + Math.round(val));
            } else {
                val *= 10;
                val = Math.round(val);
                String s = String.valueOf(val);
                if (s.substring(1, 2).equals("0")) {
                    setText(s.substring(0, 1));
                } else {
                    setText(s.substring(0, 1) + "." + s.substring(1, 2));

                }
            }
            return this;
        }

        @Override
        public void validate() {
        }

        @Override
        public void revalidate() {
        }

        @Override
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        }

        @Override
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        }
    }

    public class SpeedCellRenderer extends JProgressBar implements TableCellRenderer {
		private static final long serialVersionUID = 1424786310794166771L;

		public SpeedCellRenderer() {
            super(0, 100);
            setStringPainted(false);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
            if (value != null && value instanceof Double) {
                double v = (Double) value;
                setValue((int) (v * 100));
                setString(((int) (v * 100)) + "%");
                setToolTipText("" + v);
            }

            return this;
        }

        @Override
        public void validate() {
        }

        @Override
        public void revalidate() {
        }

        @Override
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        }

        @Override
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(new Color(255, 255, 255, 180));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    public void refresh(UISubsystem ui) throws Exception{
    	InfoNodeMDIManager m = (InfoNodeMDIManager) this.manager;
        m.removeWindow(this, true, true);
        m.addWindow(new SearchMDIWindow(ui));
    }
}
