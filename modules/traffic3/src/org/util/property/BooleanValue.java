package org.util.property;

import javax.swing.JComponent;
import javax.swing.JCheckBox;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * boolean value.
 */
public class BooleanValue extends Value<Boolean> {

    /**
     * Constructor.
     * @param key key
     * @param init initial value
     */
    public BooleanValue(String key, Boolean init) {
        super(key, init);
    }

    /**
     * return editor of this boolean value.
     * @return  editor
     */
    public JComponent getEditor() {
        JCheckBox check = new JCheckBox(new AbstractAction(getKey()) {
                public void actionPerformed(ActionEvent e) {
                    JCheckBox cb = (JCheckBox)e.getSource();
                    setValue(cb.isSelected());
                }
            });
        return check;
    }
}