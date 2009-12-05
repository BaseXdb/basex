package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXListChooser;
import org.basex.gui.layout.TableLayout;

/**
 * Dialog window for changing the used fonts.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogFontChooser extends Dialog {
  /** Predefined font sizes. */
  private static final String[] FTSZ =
    { "8", "10", "12", "14", "16", "18", "20", "22", "24", "32" };

  /** Font name chooser. */
  private final BaseXListChooser font;
  /** Font name chooser. */
  private final BaseXListChooser font2;
  /** Font type chooser. */
  private final BaseXListChooser type;
  /** Font size chooser. */
  private final BaseXListChooser size;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogFontChooser(final GUI main) {
    super(main, FONTTITLE, false);

    final String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().
      getAvailableFontFamilyNames();

    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(1, 4, 6, 6));

    font = new BaseXListChooser(fonts, this);
    font.setSize(150, 112);
    p.add(font);
    font2 = new BaseXListChooser(fonts, this);
    font2.setSize(150, 112);
    p.add(font2);
    type = new BaseXListChooser(FONTTYPES, this);
    type.setSize(80, 112);
    p.add(type);
    size = new BaseXListChooser(FTSZ, this);
    size.setSize(50, 112);
    p.add(size);

    final GUIProp gprop = gui.prop;
    font.setValue(gprop.get(GUIProp.FONT));
    font2.setValue(gprop.get(GUIProp.MONOFONT));
    type.setValue(FONTTYPES[gprop.num(GUIProp.FONTTYPE)]);
    size.setValue(Integer.toString(gui.prop.num(GUIProp.FONTSIZE)));

    set(p, BorderLayout.CENTER);

    finish(gprop.nums(GUIProp.FONTSLOC));
    font.requestFocusInWindow();
  }

  @Override
  public void action(final Object cmp) {
    final GUIProp gprop = gui.prop;
    gprop.set(GUIProp.FONT, font.getValue());
    gprop.set(GUIProp.MONOFONT, font2.getValue());
    gprop.set(GUIProp.FONTTYPE, type.getIndex());
    gprop.set(GUIProp.FONTSIZE, size.getNum());
    GUIConstants.initFonts(gprop);
    gui.notify.layout();
  }
}
