package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.options.*;

/**
 * Dialog window for changing the used colors.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class DialogColors extends BaseXDialog {
  /** Maximum color range. */
  private static final int MAXCOLOR = 32;
  /** Slider reference. */
  private final BaseXSlider sliderRed;
  /** Slider reference. */
  private final BaseXSlider sliderGreen;
  /** Slider reference. */
  private final BaseXSlider sliderBlue;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogColors(final GUI main) {
    super(main, COLOR_SCHEMA, false);

    final GUIOptions gopts = gui.gopts;
    final BaseXBack p = new BaseXBack(new TableLayout(3, 2, 16, 8));

    p.add(new BaseXLabel(RED));
    sliderRed = newSlider(GUIOptions.COLORRED);
    p.add(sliderRed);

    p.add(new BaseXLabel(GREEN));
    sliderGreen = newSlider(GUIOptions.COLORGREEN);
    p.add(sliderGreen);

    p.add(new BaseXLabel(BLUE));
    sliderBlue = newSlider(GUIOptions.COLORBLUE);
    p.add(sliderBlue);

    set(p, BorderLayout.CENTER);
    set(newButtons(RESET), BorderLayout.SOUTH);

    finish(gopts.get(GUIOptions.COLORSLOC));
  }

  /**
   * Creates a slider.
   * @param option option
   * @return slider reference
   */
  private BaseXSlider newSlider(final NumberOption option) {
    final BaseXSlider slider = new BaseXSlider(0, MAXCOLOR, option, gui.gopts, this);
    BaseXLayout.setWidth(slider, 150);
    return slider;
  }

  @Override
  public void action(final Object comp) {
    if(comp instanceof BaseXButton) {
      // reset default values
      sliderRed.setValue(GUIOptions.COLORRED.value);
      sliderGreen.setValue(GUIOptions.COLORGREEN.value);
      sliderBlue.setValue(GUIOptions.COLORBLUE.value);
    }
    sliderRed.assign();
    sliderGreen.assign();
    sliderBlue.assign();
    gui.updateLayout();
  }
}
