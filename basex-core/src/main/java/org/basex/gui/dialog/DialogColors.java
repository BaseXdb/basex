package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Dialog window for changing the used colors.
 *
 * @author BaseX Team 2005-12, BSD License
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
    sliderRed = newSlider(gopts.num(GUIOptions.COLORRED));
    p.add(sliderRed);

    p.add(new BaseXLabel(GREEN));
    sliderGreen = newSlider(gopts.num(GUIOptions.COLORGREEN));
    p.add(sliderGreen);

    p.add(new BaseXLabel(BLUE));
    sliderBlue = newSlider(gopts.num(GUIOptions.COLORBLUE));
    p.add(sliderBlue);

    set(p, BorderLayout.CENTER);
    set(newButtons(RESET), BorderLayout.SOUTH);

    finish(gopts.nums(GUIOptions.COLORSLOC));
  }

  /**
   * Creates a slider.
   * @param v initial value
   * @return slider reference
   */
  private BaseXSlider newSlider(final int v) {
    final BaseXSlider slider = new BaseXSlider(0, MAXCOLOR, MAXCOLOR - v, this);
    BaseXLayout.setWidth(slider, 150);
    return slider;
  }

  @Override
  public void action(final Object comp) {
    final GUIOptions gopts = gui.gopts;
    if(comp instanceof BaseXButton) {
      sliderRed.value(MAXCOLOR - (Integer) GUIOptions.COLORRED[1]);
      sliderGreen.value(MAXCOLOR - (Integer) GUIOptions.COLORGREEN[1]);
      sliderBlue.value(MAXCOLOR - (Integer) GUIOptions.COLORBLUE[1]);
    }

    gopts.set(GUIOptions.COLORRED, MAXCOLOR - sliderRed.value());
    gopts.set(GUIOptions.COLORGREEN, MAXCOLOR - sliderGreen.value());
    gopts.set(GUIOptions.COLORBLUE, MAXCOLOR - sliderBlue.value());
    gui.updateLayout();
  }
}
