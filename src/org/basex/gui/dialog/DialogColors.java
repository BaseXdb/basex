package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXSlider;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for changing the used colors.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogColors extends Dialog {
  /** Maximum color range. */
  private static final int MAXCOLOR = 32;
  /** Slider reference. */
  private BaseXSlider sliderRed;
  /** Slider reference. */
  private BaseXSlider sliderGreen;
  /** Slider reference. */
  private BaseXSlider sliderBlue;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogColors(final GUI main) {
    super(main, SCHEMATITLE, false);

    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(3, 2, 16, 8));

    p.add(new BaseXLabel(SCHEMARED));
    sliderRed = newSlider(GUIProp.colorred);
    p.add(sliderRed);

    p.add(new BaseXLabel(SCHEMAGREEN));
    sliderGreen = newSlider(GUIProp.colorgreen);
    p.add(sliderGreen);

    p.add(new BaseXLabel(SCHEMABLUE));
    sliderBlue = newSlider(GUIProp.colorblue);
    p.add(sliderBlue);

    set(p, BorderLayout.CENTER);
    finish(GUIProp.colorsloc);
  }

  /**
   * Creates a slider.
   * @param v initial value
   * @return slider reference
   */
  private BaseXSlider newSlider(final int v) {
    final BaseXSlider slider = new BaseXSlider(0, MAXCOLOR,
        MAXCOLOR - v, HELPCOLORS, this);
    BaseXLayout.setWidth(slider, 150);
    return slider;
  }

  @Override
  public void action(final String cmd) {
    GUIProp.colorred = MAXCOLOR - sliderRed.value();
    GUIProp.colorgreen = MAXCOLOR - sliderGreen.value();
    GUIProp.colorblue = MAXCOLOR - sliderBlue.value();
    gui.updateLayout();
  }
}
