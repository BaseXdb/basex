package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.list.*;

/**
 * Dialog window for changing the used fonts.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DialogFonts extends BaseXDialog implements Runnable {
  /** Predefined font sizes. */
  private static final String[] FTSZ =
    { "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
      "22", "24", "26", "28", "30", "33", "36", "40" };

  /** Dialog. */
  private static Dialog dialog;

  /** Fonts. */
  private final String[] fonts;
  /** Monospace fonts. */
  private String[] monoFonts;

  /** Font name chooser. */
  private final BaseXList font;
  /** Font name chooser. */
  private final BaseXList font2;
  /** Font type chooser. */
  private final BaseXList type;
  /** Font size chooser. */
  private final BaseXList size;
  /** Only display monospace fonts. */
  private final BaseXCheckBox onlyMono;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  private DialogFonts(final GUI main) {
    super(main, CHOOSE_FONT, false);

    final GUIOptions gopts = gui.gopts;
    fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    final BaseXBack p = new BaseXBack(new TableLayout(2, 4, 8, 0));
    font = new BaseXList(fonts, this);
    font.setWidth(200);
    p.add(font);
    font2 = new BaseXList(fonts, this);
    font2.setWidth(200);
    p.add(font2);
    type = new BaseXList(FONT_TYPES, this);
    type.setWidth(90);
    p.add(type);
    size = new BaseXList(FTSZ, this);
    size.setWidth(50);
    p.add(size);

    font.setValue(gopts.get(GUIOptions.FONT));
    font2.setValue(gopts.get(GUIOptions.MONOFONT));
    font2.setEnabled(false);
    type.setValue(FONT_TYPES[gopts.get(GUIOptions.FONTTYPE)]);
    size.setValue(Integer.toString(gui.gopts.get(GUIOptions.FONTSIZE)));
    font.setValue(gopts.get(GUIOptions.FONT));

    p.add(new BaseXBack());
    onlyMono = new BaseXCheckBox("Monospace", GUIOptions.ONLYMONO, gopts, this);
    p.add(onlyMono);

    set(p, BorderLayout.CENTER);
    finish(gopts.get(GUIOptions.FONTSLOC));
    SwingUtilities.invokeLater(this);
    action(onlyMono);
  }

  /**
   * Activates the dialog window.
   * @param main reference to the main window
   */
  public static void show(final GUI main) {
    if(dialog == null) dialog = new DialogFonts(main);
    dialog.setVisible(true);
  }

  @Override
  public void action(final Object cmp) {
    final GUIOptions gopts = gui.gopts;
    gopts.set(GUIOptions.FONT, font.getValue());
    if(font2.isEnabled()) gopts.set(GUIOptions.MONOFONT, font2.getValue());
    gopts.set(GUIOptions.FONTTYPE, type.getIndex());
    gopts.set(GUIOptions.FONTSIZE, size.getNum());
    gopts.set(GUIOptions.ONLYMONO, onlyMono.isSelected());

    if(cmp == onlyMono) {
      if(onlyMono.isSelected()) {
        final boolean ready = monoFonts != null;
        font2.setEnabled(ready);
        font2.setData(ready ? monoFonts : new String[] { PLEASE_WAIT_D });
      } else {
        font2.setEnabled(true);
        font2.setData(fonts);
      }
      font2.setValue(gopts.get(GUIOptions.MONOFONT));
    } else {
      font.setFont(font.getValue(), type.getIndex());
      font2.setFont(font2.getValue(), type.getIndex());
      gui.updateLayout();
    }
  }

  @Override
  public void run() {
    final Thread t = new Thread() {
      @Override
      public void run() {
        final Graphics g = getGraphics();
        final StringList monos = new StringList();
        for(final String name : fonts) {
          final FontMetrics fm = g.getFontMetrics(new Font(name, Font.PLAIN, 128));
          if(fm.charWidth(' ') == fm.charWidth('M')) monos.add(name);
        }
        monoFonts = monos.finish();
        if(gui.gopts.get(GUIOptions.ONLYMONO)) action(onlyMono);
      }
    };
    t.setDaemon(true);
    t.start();
  }
}
