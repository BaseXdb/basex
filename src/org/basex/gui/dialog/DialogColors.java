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
    super(main, SCHEMATITLE, false);

    final GUIProp gprop = gui.prop;
    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(3, 2, 16, 8));

    p.add(new BaseXLabel(SCHEMARED));
    sliderRed = newSlider(gprop.num(GUIProp.COLORRED));
    p.add(sliderRed);

    p.add(new BaseXLabel(SCHEMAGREEN));
    sliderGreen = newSlider(gprop.num(GUIProp.COLORGREEN));
    p.add(sliderGreen);

    p.add(new BaseXLabel(SCHEMABLUE));
    sliderBlue = newSlider(gprop.num(GUIProp.COLORBLUE));
    p.add(sliderBlue);

    set(p, BorderLayout.CENTER);
    finish(gprop.nums(GUIProp.COLORSLOC));
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
    final GUIProp gprop = gui.prop;
    gprop.set(GUIProp.COLORRED, MAXCOLOR - sliderRed.value());
    gprop.set(GUIProp.COLORGREEN, MAXCOLOR - sliderGreen.value());
    gprop.set(GUIProp.COLORBLUE, MAXCOLOR - sliderBlue.value());
    gui.updateLayout();
  }
}
