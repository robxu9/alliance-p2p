package org.alliance.ui.windows.options;

import com.stendahls.XUI.XUI;
import com.stendahls.XUI.XUIDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import org.alliance.core.Language;
import org.alliance.ui.UISubsystem;
import org.alliance.ui.themes.util.SubstanceThemeHelper;

import javax.swing.JPanel;

/**
 *
 * @author Bastvera
 */
public class SecurityTab extends XUIDialog implements TabHelper {
	private static final long serialVersionUID = -3796934898880094029L;
	
	private JCheckBox allowFriends;
    private JCheckBox denyAll;
    private JPanel tab;
    private UISubsystem ui;
    private final static String[] OPTIONS = new String[]{
        "internal.disablenewuserpopup", "internal.alwaysallowfriendstoconnect",
        "internal.alwaysallowfriendsoffriendstoconnecttome", "internal.automaticallydenyallinvitations",
        "internal.alwaysautomaticallyconnecttoallfriendsoffriend", "internal.encryption"
    };

    public SecurityTab(String loading) {
        tab = new JPanel();
        tab.add(new JLabel(loading));
        tab.setName(Language.getLocalizedString(getClass(), "title"));
        tab.setToolTipText(Language.getLocalizedString(getClass(), "tooltip"));
    }

    public SecurityTab(final UISubsystem ui) throws Exception {
        init(ui.getRl(), ui.getRl().getResourceStream("xui/optionstabs/securitytab.xui.xml"));
        this.ui = ui;

        Language.translateXUIElements(getClass(), xui.getXUIComponents());
        SubstanceThemeHelper.setButtonsToGeneralArea(xui.getXUIComponents());
        tab = (JPanel) xui.getComponent("securitytab");
        tab.setName(Language.getLocalizedString(getClass(), "title"));
        tab.setToolTipText(Language.getLocalizedString(getClass(), "tooltip"));

        allowFriends = (JCheckBox) xui.getComponent("internal.alwaysallowfriendsoffriendstoconnecttome");
        denyAll = (JCheckBox) xui.getComponent("internal.automaticallydenyallinvitations");


        ActionListener al = new ActionListener() {

            private boolean actionInProgress = false;

            @Override
            public void actionPerformed(final ActionEvent e) {
                if (actionInProgress) {
                    return;
                }
                actionInProgress = true;
                checkCheckBoxStatus();
                actionInProgress = false;
            }
        };

        allowFriends.addActionListener(al);
        denyAll.addActionListener(al);
    }

    private void checkCheckBoxStatus() {
        denyAll.setEnabled(true);
        if (allowFriends.isSelected()) {
            denyAll.setSelected(false);
            denyAll.setEnabled(false);
        }
    }

    @Override
    public JPanel getTab() {
        return tab;
    }

    @Override
    public String[] getOptions() {
        return OPTIONS;
    }

    @Override
    public XUI getXUI() {
        return super.getXUI();
    }

    @Override
    public boolean isAllowedEmpty(String option) {
        return false;
    }

    @Override
    public String getOverridedSettingValue(String option, String value) {
        return value;
    }

    @Override
    public Object getOverridedComponentValue(String option, Object value) {
        if (value == null || value.toString().isEmpty()) {
            if (!isAllowedEmpty(option)) {
                return null;
            }
        }
        return value;
    }

    @Override
    public void postOperation() {
        checkCheckBoxStatus();
    }
}
