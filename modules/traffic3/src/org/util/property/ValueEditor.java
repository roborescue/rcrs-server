package org.util.property;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 *
 */
public final class ValueEditor {

    private ValueEditor() {}

    /**
     * create editor for a value.
     * @param v value
     * @return editor of the value
     */
    public static JComponent createEditor(Value v) {
        Class c = v.getClass();
        if (c == org.util.property.BooleanValue.class) {
            return createBooleanEditor((BooleanValue)v);
        }
        else {
            System.err.println("cannot find editor for value" + v);
        }
        return new JLabel("???");
    }

    /**
     * create boolean editor.
     * @param v value
     * @return editor of the value
     */
    public static JComponent createBooleanEditor(final BooleanValue v) {
        Action action = new AbstractAction(v.getKey()) {
                public void actionPerformed(ActionEvent e) {
                    JCheckBox c = (JCheckBox)e.getSource();
                    v.setValue(c.isSelected());
                }
            };
        final JCheckBox check = new JCheckBox(action);
        v.addValueListener(new ValueListener() {
                public void valueChanged(Value nv) {
                    BooleanValue bv = (BooleanValue)nv;
                    if (bv.getValue() && !check.isSelected()) {
                        check.setSelected(true);
                    }
                    else if (!bv.getValue() && check.isSelected()) {
                        check.setSelected(false);
                    }
                }
            });
        check.setSelected(v.getValue());
        return check;
    }
}
