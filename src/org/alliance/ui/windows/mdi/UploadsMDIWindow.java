package org.alliance.ui.windows.mdi;


import com.stendahls.nif.ui.mdi.MDIWindow;
import com.stendahls.util.TextUtils;
import org.alliance.core.Language;
import org.alliance.core.comm.Connection;
import org.alliance.core.comm.filetransfers.UploadConnection;
import org.alliance.core.file.filedatabase.FileDescriptor;
import org.alliance.ui.UISubsystem;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 * Created by IntelliJ IDEA.
 * User: maciek
 * Date: 2006-jan-05
 * Time: 13:21:55
 * To change this template use File | Settings | File Templates.
 */
public class UploadsMDIWindow extends AllianceMDIWindow {
	private static final long serialVersionUID = -761411168249457332L;
	
	private UploadsMDIWindow.UploadsTableModel model;
    private JTable table;
    private ArrayList<UploadWrapper> rows = new ArrayList<UploadsMDIWindow.UploadWrapper>();
    private int sort;
    private boolean sortUp = false;

    public UploadsMDIWindow(UISubsystem ui) throws Exception {
        super(ui.getMainWindow().getMDIManager(), "uploads", ui);
        Language.translateXUIElements(getClass(), xui.getXUIComponents());

        table = (JTable) xui.getComponent("table");
        table.setModel(model = new UploadsMDIWindow.UploadsTableModel());
        table.setAutoCreateColumnsFromModel(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(50);
        table.getColumnModel().getColumn(3).setPreferredWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(50);
        
       table.getTableHeader().addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
                JTableHeader h = (JTableHeader) e.getSource();
                TableColumnModel columnModel = h.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = columnModel.getColumn(viewColumn).getModelIndex();
                if (column != -1) {
                    sort = column;
                    sortUp = !sortUp;
                }
        	}
        }); 

        update();
        setTitle(Language.getLocalizedString(getClass(), "title"));
        postInit();
    }

    public void update() {
        boolean structureChanged = false;
        sort(sort);
        for (UploadWrapper w : rows) {
            w.speed = Language.getLocalizedString(getClass(), "complete");
        }

        ArrayList<Connection> al = new ArrayList<Connection>(ui.getCore().getNetworkManager().connections());
        for (Connection c : al) {
            if (c instanceof UploadConnection) {
                UploadConnection uc = (UploadConnection) c;
                UploadWrapper w = getWrapperFor(uc);
                if (w == null) {
                    w = new UploadWrapper(uc);
                    rows.add(w);
                    structureChanged = true;
                }
                w.update();
            }
        }

        if (structureChanged) {
            model.fireTableStructureChanged();
        } else {
            model.fireTableRowsUpdated(0, rows.size());
        }

        ((JLabel) xui.getComponent("status")).setText(Language.getLocalizedString(getClass(), "uptotal", TextUtils.formatByteSize(ui.getCore().getNetworkManager().getBandwidthOut().getTotalBytes())));
    }
    
  private void sort(int i) {
	  switch (i) {
	  	case 0:
	  		model.sortByNickname();
          break;
	  	case 1:
	  		model.sortByFileName();
          break;
	  	case 2:
	  		model.sortBySpeed();
          break;
	  	case 3:
	  		model.sortByElapsed();
          break;
	  	case 4:
	  		model.sortBySize();
          break;
	  }
    	
    }

    private UploadWrapper getWrapperFor(UploadConnection u) {
        for (UploadWrapper cw : rows) {
            if (cw.upload == u) {
                return cw;
            }
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return "uploads";
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

    private class UploadWrapper {
    	
        public UploadConnection upload;
        public String nickname, filename, speed, sent, elapsed;
        
        private Date began;

        public UploadWrapper(UploadConnection uc) {
            this.upload = uc;
            began = new Date(System.currentTimeMillis());
        }

        public void update() {
        	try {
                if (upload == null || upload.getRemoteFriend() == null) {
                    return;
                }
                nickname = upload.getRemoteFriend().getNickname();
                FileDescriptor fd = ui.getCore().getFileManager().getFd(upload.getRoot());
                if (fd != null) {
                    filename = fd.getSubPath();
                }
                elapsed = elapsedSince(began);
                speed = TextUtils.formatByteSize((long) upload.getBandwidthOut().getCPS()) + "/s";
                sent = TextUtils.formatByteSize(upload.getBytesSent());
            } catch (IOException e) {
                ui.handleErrorInEventLoop(e);
            }
        }
        
        private String elapsedSince(Date began) {
        	Date current = new Date(System.currentTimeMillis());
        	long diff = (current.getTime() - began.getTime()) / 1000;
        	int secs = 0, mins = 0, hours = 0, days = 0, weeks = 0;
        	if (diff < 60) {
        		secs = (int)diff;
        		return secs + " sec";
        	}
        	secs = (int)(diff % 60);
        	diff /= 60;
        	if (diff < 60) {
        		mins = (int)diff;
        		return mins + ":" + String.format("%02d", secs) + "mins";
        	}
        	mins = (int)(diff % 60);
        	diff /= 60;
        	if (diff < 24) {
        		hours = (int)diff;
        		return hours + "h" + mins + "m";
        	}
        	days = (int)(diff % 24);
        	diff /= 24;
        	if (diff < 7) {
        		weeks = (int)diff;
        		return days + "d" + hours + "h";
        	}
        	weeks = (int)(diff % 7);
        	return weeks + "w" + days + "d";
        }
    }

    private class UploadsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -4440907637702707612L;
		
		@Override
        public int getRowCount() {
            return rows.size();
        }
		
		public void sortBySize() {
        	int j = (rows.size()-1);
        	while(j > 0)
        		{
        			for(int i = j; i > 0; i--)				
        			{
        				if(sortUp) {
        					if(rows.get(i).upload.getBytesSent() > rows.get(i-1).upload.getBytesSent())
        					{
        						UploadWrapper temp = rows.get(i);
        						rows.set(i, rows.get(i-1));
        						rows.set(i-1, temp);
        					}
        			}
        				else {
        					if(rows.get(i).upload.getBytesSent() < rows.get(i-1).upload.getBytesSent())
        					{
        						UploadWrapper temp = rows.get(i);
        						rows.set(i, rows.get(i-1));
        						rows.set(i-1, temp);
        					}
        				}
        			}
        			j--;
        		}
		}
        
        public void sortByElapsed() {
        	int j = (rows.size()-1);
        	while(j > 0)
        		{
        			for(int i = j; i > 0; i--)				
        			{
        				if(sortUp) {
        					if(rows.get(i).began.before(rows.get(i-1).began))
        					{
        						UploadWrapper temp = rows.get(i);
        						rows.set(i, rows.get(i-1));
        						rows.set(i-1, temp);
        					}
        			}
        				else {
        					if(rows.get(i).began.after(rows.get(i-1).began))
        					{
        						UploadWrapper temp = rows.get(i);
        						rows.set(i, rows.get(i-1));
        						rows.set(i-1, temp);
        					}
        				}
        			}
        			j--;
        		}
		}
        
        public void sortBySpeed() {
        	int j = (rows.size()-1);
        	while(j > 0)
    		{
    			for(int i = j; i > 0; i--)				
    			{
    				if(sortUp) {
    					if(rows.get(i).upload.getBandwidthOut().getCPS() > rows.get(i-1).upload.getBandwidthOut().getCPS())
    					{
    						UploadWrapper temp = rows.get(i);
    						rows.set(i, rows.get(i-1));
    						rows.set(i-1, temp);
    					}
    			}
    				else {
    					if(rows.get(i).upload.getBandwidthOut().getCPS() < rows.get(i-1).upload.getBandwidthOut().getCPS())
    					{
    						UploadWrapper temp = rows.get(i);
    						rows.set(i, rows.get(i-1));
    						rows.set(i-1, temp);
    					}
    				}
    			}
    			j--;
    		}
		}
        
        public void sortByFileName() {
        	int j = (rows.size()-1);
        	while(j > 0)
    		{
    			for(int i = j; i > 0; i--)				
    			{
    				if(sortUp) {
    					if(rows.get(i).filename.compareToIgnoreCase(rows.get(i-1).filename) == -1)
    					{
    						UploadWrapper temp = rows.get(i);
    						rows.set(i, rows.get(i-1));
    						rows.set(i-1, temp);
    					}
    			}
    				else {
    					if(rows.get(i).filename.compareToIgnoreCase(rows.get(i-1).filename) == 1)
    					{
    						UploadWrapper temp = rows.get(i);
    						rows.set(i, rows.get(i-1));
    						rows.set(i-1, temp);
    					}
    				}
    			}
    			j--;
    		}
		}
        
        public void sortByNickname() {
        	int j = (rows.size()-1);
        	while(j > 0)
    		{
    			for(int i = j; i > 0; i--)				
    			{
    				if(sortUp) {
    					if(rows.get(i).nickname.compareToIgnoreCase(rows.get(i-1).nickname) == -1)
    					{
    						UploadWrapper temp = rows.get(i);
    						rows.set(i, rows.get(i-1));
    						rows.set(i-1, temp);
    					}
    			}
    				else {
    					if(rows.get(i).nickname.compareToIgnoreCase(rows.get(i-1).nickname) == 1)
    					{
    						UploadWrapper temp = rows.get(i);
    						rows.set(i, rows.get(i-1));
    						rows.set(i-1, temp);
    					}
    				}
    			}
    			j--;
    		}
		}

		@Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Language.getLocalizedString(getClass().getEnclosingClass(), "upload");
                case 1:
                    return Language.getLocalizedString(getClass().getEnclosingClass(), "file");
                case 2:
                    return Language.getLocalizedString(getClass().getEnclosingClass(), "speed");
                case 3:
                	return Language.getLocalizedString(getClass().getEnclosingClass(), "elapsed");
                case 4:
                    return Language.getLocalizedString(getClass().getEnclosingClass(), "sent");
                default:
                    return Language.getLocalizedString(getClass().getEnclosingClass(), "undefined");
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return rows.get(rowIndex).nickname;
                case 1:
                    return rows.get(rowIndex).filename;
                case 2:
                    return rows.get(rowIndex).speed;
                case 3:
                	return rows.get(rowIndex).elapsed;
                case 4:
                    return rows.get(rowIndex).sent;
                default:
                    return Language.getLocalizedString(getClass().getEnclosingClass(), "undefined");
            }
        }
    }

    public void EVENT_cleanup(ActionEvent a) {
        if (rows.size() == 0) {
            return;
        }
        int n = rows.size();
        rows.clear();
        model.fireTableRowsDeleted(0, n - 1);
    }
}
