package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.util.list.*;

/**
 * Dialog window for changing the used fonts.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DialogFonts extends BaseXDialog {
  /** Predefined font sizes. */
  private static final String[] SIZES =
    { "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
      "22", "24", "26", "28", "30", "33", "36", "40", "48", "64", "96" };

  /** Dialog. */
  private static DialogFonts dialog;

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
   * @param gui reference to the main window
   */
  private DialogFonts(final GUI gui) {
    super(gui, CHOOSE_FONT, false);

    final GUIOptions gopts = gui.gopts;
    fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    final BaseXBack p = new BaseXBack(new TableLayout(2, 4, 8, 0));
    font = new BaseXList(this, fonts);
    font.setWidth(200);
    p.add(font);
    font2 = new BaseXList(this, fonts);
    font2.setWidth(200);
    p.add(font2);
    type = new BaseXList(this, FONT_TYPES);
    type.setWidth(90);
    p.add(type);
    size = new BaseXList(this, SIZES);
    size.setWidth(50);
    p.add(size);

    font.setValue(gopts.get(GUIOptions.FONT));
    font2.setValue(gopts.get(GUIOptions.MONOFONT));
    font2.setEnabled(false);
    type.setValue(FONT_TYPES[gopts.get(GUIOptions.FONTTYPE)]);
    font.setValue(gopts.get(GUIOptions.FONT));

    p.add(new BaseXBack());
    onlyMono = new BaseXCheckBox(this, "Monospace", GUIOptions.ONLYMONO, gopts);
    p.add(onlyMono);

    set(p, BorderLayout.CENTER);
    finish();

    monoFonts();
    action(onlyMono);
  }

  /**
   * Activates the dialog window.
   * @param gui reference to the main window
   */
  public static void show(final GUI gui) {
    if(dialog == null) dialog = new DialogFonts(gui);
    dialog.setVisible(true);
    dialog.size.setValue(Integer.toString(gui.gopts.get(GUIOptions.FONTSIZE)));
  }

  @Override
  public void action(final Object cmp) {
    final GUIOptions gopts = gui.gopts;
    if(cmp == onlyMono) {
      final boolean selected = onlyMono.isSelected();
      gopts.set(GUIOptions.ONLYMONO, selected);
      if(selected) {
        final boolean ready = monoFonts != null;
        font2.setEnabled(ready);
        font2.setData(ready ? monoFonts : new String[] { PLEASE_WAIT_D });
      } else {
        font2.setEnabled(true);
        font2.setData(fonts);
      }
      font2.setValue(gopts.get(GUIOptions.MONOFONT));
    } else {
      boolean changed = false;
      if(cmp == font) {
        final String name = font.getValue();
        if(!name.isEmpty()) {
          gopts.set(GUIOptions.FONT, name);
          changed = true;
        }
      } else if(cmp == font2) {
        final String name = font2.getValue();
        if(!name.isEmpty()) {
          gopts.set(GUIOptions.MONOFONT, name);
          changed = true;
        }
      } else if(cmp == size) {
        final int num = size.getNum();
        if(num > 0) {
          gopts.set(GUIOptions.FONTSIZE, num);
          changed = true;
        }
      } else if(cmp == type) {
        final int num = type.getIndex();
        if(num >= 0) {
          gopts.set(GUIOptions.FONTTYPE, num);
          changed = true;
        }
      }

      if(changed) {
        final int t = gopts.get(GUIOptions.FONTTYPE);
        font.setFont(gopts.get(GUIOptions.FONT), t);
        font2.setFont(gopts.get(GUIOptions.MONOFONT), t);
        gui.updateLayout();
      }
    }
  }

  /**
   * Creates a list of mono-spaced fonts in a separate thread.
   */
  private void monoFonts() {
    new GUIWorker<Boolean>() {
      @Override
      protected Boolean doInBackground() {
        final Graphics g = getGraphics();
        final StringList monos = new StringList();
        for(final String name : fonts) {
          final FontMetrics fm = g.getFontMetrics(new Font(name, Font.PLAIN, 128));
          if(fm.charWidth(' ') == fm.charWidth('M')) monos.add(name);
        }
        monoFonts = monos.finish();
        return gui.gopts.get(GUIOptions.ONLYMONO);
      }
      @Override
      protected void done(final Boolean mono) {
        if(mono) action(onlyMono);
      }
    }.execute();
  }
}
