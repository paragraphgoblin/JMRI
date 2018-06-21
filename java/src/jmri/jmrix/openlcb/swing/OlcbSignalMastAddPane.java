package jmri.jmrix.openlcb.swing;

import jmri.*;
import jmri.jmrit.beantable.signalmast.SignalMastAddPane;
import jmri.jmrix.SystemConnectionMemo;

import jmri.jmrix.openlcb.*;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.annotation.Nonnull;

import org.openide.util.lookup.ServiceProvider;
import org.openlcb.swing.EventIdTextField;

/**
 * A pane for configuring OlcbSignalMast objects
 * <P>
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
public class OlcbSignalMastAddPane extends SignalMastAddPane {

    public OlcbSignalMastAddPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // lit/unlit controls
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("AllowUnLitLabel") + ": "));
        p.add(allowUnLit);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(p);
        
        // aspects controls
        TitledBorder disableborder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        disableborder.setTitle(Bundle.getMessage("EnterAspectsLabel"));
        JScrollPane disabledAspectsScroll = new JScrollPane(disabledAspectsPanel);
        disabledAspectsScroll.setBorder(disableborder);
        add(disabledAspectsScroll);

    }

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return "OlcbSignalMast";
    }


    JCheckBox allowUnLit = new JCheckBox();

    LinkedHashMap<String, JCheckBox> disabledAspects = new LinkedHashMap<>(14);
    LinkedHashMap<String, EventIdTextField> aspectEventIDs = new LinkedHashMap<>(14);
    JPanel disabledAspectsPanel = new JPanel();
    
    OlcbSignalMast currentMast = null;

    /** {@inheritDoc} */
    @Override
    public void setAspectNames(@Nonnull Enumeration<String> aspects) {
        // update immediately
        disabledAspects = new LinkedHashMap<>(10);
        disabledAspectsPanel.removeAll();
        while (aspects.hasMoreElements()) {
            String aspect = aspects.nextElement();
            JCheckBox disabled = new JCheckBox(aspect);
            disabledAspects.put(aspect, disabled);
            EventIdTextField eventID = new EventIdTextField();
            eventID.setText("00.00.00.00.00.00.00.00");
            aspectEventIDs.put(aspect, eventID);
        }
        disabledAspectsPanel.setLayout(new BoxLayout(disabledAspectsPanel, BoxLayout.Y_AXIS));
        for (String aspect : disabledAspects.keySet()) {
            JPanel p1 = new JPanel();
            TitledBorder p1border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
            p1border.setTitle(aspect);
            p1.setBorder(p1border);
            p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
            p1.add(aspectEventIDs.get(aspect));
            p1.add(disabledAspects.get(aspect));
            disabledAspects.get(aspect).setName(aspect);
            disabledAspects.get(aspect).setText(Bundle.getMessage("DisableAspect"));
            disabledAspectsPanel.add(p1);
        }

        disabledAspectsPanel.revalidate();
    }

    /** {@inheritDoc} */
    @Override
    public boolean canHandleMast(@Nonnull SignalMast mast) {
        return mast instanceof OlcbSignalMast;
    }

    /** {@inheritDoc} */
    @Override
    public void setMast(SignalMast mast) { 
        if (mast == null) { 
            currentMast = null; 
            return; 
        }
        
        if (! (mast instanceof OlcbSignalMast) ) {
            log.error("mast was wrong type: {} {}", mast.getSystemName(), mast.getClass().getName());
            return;
        }

        currentMast = (OlcbSignalMast) mast;
        List<String> disabled = currentMast.getDisabledAspects();
        if (disabled != null) {
            for (String aspect : disabled) {
                if (disabledAspects.containsKey(aspect)) {
                    disabledAspects.get(aspect).setSelected(true);
                }
            }
            //+ do we need to clear non-disabled aspects?
         }
        for (String aspect : currentMast.getAllKnownAspects()) {
            if (aspectEventIDs.get(aspect) == null) {
                EventIdTextField eventID = new EventIdTextField();
                eventID.setText("00.00.00.00.00.00.00.00");
                aspectEventIDs.put(aspect, eventID);
            }
            if (currentMast.isOutputConfigured(aspect)) {
                aspectEventIDs.get(aspect).setText(new OlcbAddress(currentMast.getOutputForAppearance(aspect)).toDottedString());
            } else {
                aspectEventIDs.get(aspect).setText("00.00.00.00.00.00.00.00");
            }
        }
    }

    DecimalFormat paddedNumber = new DecimalFormat("0000");

    /** {@inheritDoc} */
    @Override
    public void createMast(@Nonnull String sigsysname, @Nonnull String mastname, @Nonnull String username) {
        if (currentMast == null) {
            // create a mast
            String name = "MF$olm:"
                    + sigsysname
                    + ":" + mastname.substring(11, mastname.length() - 4);
            name += "(" + (paddedNumber.format(OlcbSignalMast.getLastRef() + 1)) + ")";
            currentMast = new OlcbSignalMast(name);
            if (!username.equals("")) {
                currentMast.setUserName(username);
            }
            InstanceManager.getDefault(jmri.SignalMastManager.class).register(currentMast);
        }
        
        // load a new or existing mast
        for (String aspect : disabledAspects.keySet()) {
            if (disabledAspects.get(aspect).isSelected()) {
                currentMast.setAspectDisabled(aspect);
            } else {
                currentMast.setAspectEnabled(aspect);
            }
            currentMast.setOutputForAppearance(aspect, aspectEventIDs.get(aspect).getText());
        }
        currentMast.setAllowUnLit(allowUnLit.isSelected());
    }


    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {

        /**
         * {@inheritDoc}
         * Requires a valid OpenLCB connection
         */
        @Override
        public boolean isAvailable() {
            for (SystemConnectionMemo memo : InstanceManager.getList(SystemConnectionMemo.class)) {
                if (memo instanceof jmri.jmrix.can.CanSystemConnectionMemo) {
                    return true;
                }
            }
            return false;
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return "OlcbSignalMast";
        }

        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new OlcbSignalMastAddPane();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbSignalMastAddPane.class);

}