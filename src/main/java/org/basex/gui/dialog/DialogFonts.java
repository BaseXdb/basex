package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;

import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXList;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for changing the used fonts.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogFonts extends Dialog {
  /** Predefined font sizes. */
  private static final String[] FTSZ =
    { "8", "10", "12", "14", "16", "18", "20", "22", "24", "32" };

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
    super(main, FONTTITLE, false);

    final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().
      getAvailableFontFamilyNames();

    final BaseXBack p = new BaseXBack(new TableLayout(1, 4, 8, 0));
    font = new BaseXList(fonts, this);
    p.add(font);
    font2 = new BaseXList(fonts, this);
    p.add(font2);
    type = new BaseXList(FONTTYPES, this);
    type.setWidth(90);
    p.add(type);
    size = new BaseXList(FTSZ, this);
    size.setWidth(50);
    p.add(size);

    final GUIProp gprop = gui.gprop;
    font.setValue(gprop.get(GUIProp.FONT));
    font2.setValue(gprop.get(GUIProp.MONOFONT));
    type.setValue(FONTTYPES[gprop.num(GUIProp.FONTTYPE)]);
    size.setValue(Integer.toString(gui.gprop.num(GUIProp.FONTSIZE)));

    set(p, BorderLayout.CENTER);
    finish(gprop.nums(GUIProp.FONTSLOC));
  }

  @Override
  public void action(final Object cmp) {
    final GUIProp gprop = gui.gprop;
    gprop.set(GUIProp.FONT, font.getValue());
    gprop.set(GUIProp.MONOFONT, font2.getValue());
    gprop.set(GUIProp.FONTTYPE, type.getIndex());
    gprop.set(GUIProp.FONTSIZE, size.getNum());
    font.setFont(font.getValue(), type.getIndex());
    font2.setFont(font2.getValue(), type.getIndex());
    gui.updateLayout();
  }
}
