package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Dialog window for changing the used fonts.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogFonts extends BaseXDialog {
  /** Predefined font sizes. */
  private static final String[] FTSZ =
    { "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
      "22", "24", "26", "28", "30", "33", "36", "40" };

  /** Font name chooser. */
  private final BaseXList font;
  /** Font name chooser. */
  private final BaseXList font2;
  /** Font type chooser. */
  private final BaseXList type;
  /** Font size chooser. */
  private final BaseXList size;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogFonts(final GUI main) {
    super(main, CHOOSE_FONT, false);

    final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().
      getAvailableFontFamilyNames();

    final BaseXBack p = new BaseXBack(new TableLayout(1, 4, 8, 0));
    font = new BaseXList(fonts, this);
    p.add(font);
    font2 = new BaseXList(fonts, this);
    p.add(font2);
    type = new BaseXList(FONT_TYPES, this);
    type.setWidth(90);
    p.add(type);
    size = new BaseXList(FTSZ, this);
    size.setWidth(50);
    p.add(size);

    final GUIOptions gopts = gui.gopts;
    font.setValue(gopts.string(GUIOptions.FONT));
    font2.setValue(gopts.string(GUIOptions.MONOFONT));
    type.setValue(FONT_TYPES[gopts.number(GUIOptions.FONTTYPE)]);
    size.setValue(Integer.toString(gui.gopts.number(GUIOptions.FONTSIZE)));

    set(p, BorderLayout.CENTER);
    finish(gopts.numbers(GUIOptions.FONTSLOC));
  }

  @Override
  public void action(final Object cmp) {
    final GUIOptions gopts = gui.gopts;
    gopts.string(GUIOptions.FONT, font.getValue());
    gopts.string(GUIOptions.MONOFONT, font2.getValue());
    gopts.number(GUIOptions.FONTTYPE, type.getIndex());
    gopts.number(GUIOptions.FONTSIZE, size.getNum());
    font.setFont(font.getValue(), type.getIndex());
    font2.setFont(font2.getValue(), type.getIndex());
    gui.updateLayout();
  }
}
