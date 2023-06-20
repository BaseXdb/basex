package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Dialog window for changing the used fonts.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class DialogFonts extends BaseXDialog {
  /** Predefined font sizes. */
  private static final String[] SIZES =
    { "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
      "22", "24", "26", "28", "30", "33", "36", "40", "48", "64", "96" };

  /** Dialog. */
  private static DialogFonts dialog;

  /** Monospace fonts. */
  private String[] monoFonts;

  /** Font name chooser. */
  private final BaseXList font;
  /** Font name chooser. */
  private final BaseXList font2;
  /** Font size chooser. */
  private final BaseXList size;
  /** Only display monospace fonts. */
  private final BaseXCheckBox onlyMono;
  /** Anti-aliasing. */
  private final BaseXCombo antiAlias;

  /**
   * Default constructor.
   * @param gui reference to the main window
   */
  private DialogFonts(final GUI gui) {
    super(gui, CHOOSE_FONT, false);

    final GUIOptions gopts = gui.gopts;

    antiAlias = new BaseXCombo(this, "System", "GASP", "On", "Off");
    antiAlias.setSelectedItem(gopts.get(GUIOptions.ANTIALIAS));

    final BaseXBack p = new BaseXBack(new TableLayout(2, 3, 8, 8));
    final String[] fonts = GUIConstants.fonts();
    font = new BaseXList(this, fonts);
    font.setWidth(200);
    p.add(font);
    font2 = new BaseXList(this, fonts);
    font2.setWidth(200);
    p.add(font2);
    size = new BaseXList(this, SIZES);
    size.setWidth(50);
    p.add(size);

    font.setValue(gopts.get(GUIOptions.FONT));
    font2.setValue(gopts.get(GUIOptions.MONOFONT));
    font2.setEnabled(false);
    font.setValue(gopts.get(GUIOptions.FONT));

    final BaseXBack pp = new BaseXBack(new TableLayout(1, 2, 8, 8));
    pp.add(new BaseXLabel("Anti-Aliasing"));
    pp.add(antiAlias);
    p.add(pp);

    onlyMono = new BaseXCheckBox(this, "Monospace", GUIOptions.LISTMONO, gopts);
    p.add(onlyMono);

    set(p, BorderLayout.CENTER);
    finish();

    monoFonts = GUIConstants.monoFonts();
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
    boolean changed = false;

    final GUIOptions gopts = gui.gopts;
    if(cmp == antiAlias) {
      gopts.set(GUIOptions.ANTIALIAS, antiAlias.getSelectedItem());
      changed = true;
    } else if(cmp == onlyMono) {
      final boolean selected = onlyMono.isSelected();
      gopts.set(GUIOptions.LISTMONO, selected);
      if(selected) {
        final boolean ready = monoFonts != null;
        font2.setEnabled(ready);
        font2.setData(ready ? monoFonts : new String[] { PLEASE_WAIT_D });
      } else {
        font2.setEnabled(true);
        font2.setData(GUIConstants.fonts());
      }
      font2.setValue(gopts.get(GUIOptions.MONOFONT));
    } else {
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
      }
    }
    if(changed) {
      font.setFont(gopts.get(GUIOptions.FONT));
      font2.setFont(gopts.get(GUIOptions.MONOFONT));
      gui.updateLayout();
    }
  }
}
