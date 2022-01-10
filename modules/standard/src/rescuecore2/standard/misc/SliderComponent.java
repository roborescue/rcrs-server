package rescuecore2.standard.misc;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A JComponent that has a JSlider and a JTextField. Dragging the slider will
 * update the text field and vice versa.
 */
public class SliderComponent extends JPanel {

  private JSlider slider;
  private JTextField text;

  /**
   * Create a vertical SliderComponent with range bounds.
   *
   * @param min
   *   The minimum value of the slider.
   * @param max
   *   The maximum value of the slider.
   * @param value
   *   The current value of the slider.
   */
  public SliderComponent(int min, int max, int value) {
    this(min, max, value, SwingConstants.VERTICAL);
  }


  /**
   * Create a SliderComponent with range bounds.
   *
   * @param min
   *   The minimum value of the slider.
   * @param max
   *   The maximum value of the slider.
   * @param value
   *   The current value of the slider.
   * @param orientation
   *   The orientation of the slider. Must be either
   *   {@link javax.swing.SwingConstants#VERTICAL} or
   *   {@link javax.swing.SwingConstants#HORIZONTAL}
   */
  public SliderComponent(int min, int max, int value, int orientation) {
    this(new JSlider(orientation, min, max, value),
        new JTextField(String.valueOf(value)));
  }


  /**
   * Create a SliderComponent with a given slider. If the slider has vertical
   * orientation then the text field will be displayed below it, otherwise it
   * will
   * be to the right.
   *
   * @param slider
   *   The JSlider to display.
   */
  public SliderComponent(JSlider slider) {
    this(slider, new JTextField(String.valueOf(slider.getValue())));
  }


  /**
   * Create a SliderComponent with a given slider and text field. If the slider
   * has vertical orientation then the text field will be displayed below it,
   * otherwise it will be to the right.
   *
   * @param slider
   *   The JSlider to display.
   * @param text
   *   The JTextField to display.
   */
  public SliderComponent(JSlider slider, JTextField text) {
    super(new BorderLayout());
    setup(slider, text);
  }


  /**
   * Get the JSlider component. This is useful if you want to manually set the
   * tick marks etc.
   *
   * @return The JSlider component.
   */
  public JSlider getSlider() {
    return slider;
  }


  /**
   * Get the JTextField component.
   *
   * @return The JTextField component.
   */
  public JTextField getTextField() {
    return text;
  }


  private void setup(final JSlider s, final JTextField t) {
    this.slider = s;
    this.text = t;
    add(slider, BorderLayout.CENTER);
    text.setText(String.valueOf(slider.getValue()));
    if (slider.getOrientation() == SwingConstants.VERTICAL) {
      add(text, BorderLayout.SOUTH);
    } else {
      add(text, BorderLayout.EAST);
    }
    text.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          int i = Integer.parseInt(text.getText());
          slider.setValue(i);
        } catch (NumberFormatException ex) {
          // Ignore
          ex.printStackTrace();
        }
      }
    });
    slider.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        text.setText(String.valueOf(slider.getValue()));
      }
    });
  }
}