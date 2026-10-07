package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.util.*;

/**
 * Dialog with a single text field.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DialogMem extends BaseXDialog {
  /** Dialog (can be {@code null}). */
  private static Dialog dialog;

  /** Info text. */
  private final TextPanel text;
  /** GC Button. */
  private final BaseXButton gc;
  /** Timer for updating the display of memory consumption. */
  private final Timer timer;

  /**
   * Default constructor.
   * @param gui reference to the main window
   */
  private DialogMem(final GUI gui) {
    super(gui, USED_MEM, false);
    panel.setLayout(new BorderLayout());

    text = new TextPanel(this, info(), false);
    text.setFont(panel.getFont());
    set(text, BorderLayout.CENTER);

    gc = new BaseXButton(this, "GC");
    final BaseXBack buttons = newButtons(gc);
    set(buttons, BorderLayout.SOUTH);
    timer = new Timer(500, e -> {
      if(!text.selected()) text.setText(info());
    });
    finish();
  }

  /**
   * Activates the dialog window.
   * @param gui reference to the main window
   */
  public static void show(final GUI gui) {
    if(dialog == null) dialog = new DialogMem(gui);
    dialog.setVisible(true);
  }

  @Override
  public void setVisible(final boolean v) {
    super.setVisible(v);
    // focus GC button
    SwingUtilities.invokeLater(gc::requestFocusInWindow);
  }

  @Override
  public void action(final Object cmp) {
    Performance.gc(3);
    text.setText(info());
  }

  /**
   * Returns the info text.
   * @return text
   */
  private static String info() {
    final Runtime rt = Runtime.getRuntime();
    final long max = rt.maxMemory();
    final long total = rt.totalMemory();
    final long used = total - rt.freeMemory();
    return MEMUSED_C + Performance.formatHuman(used) + NL
        + RESERVED_MEM_C + Performance.formatHuman(total) + NL
        + TOTAL_MEM_C + Performance.formatHuman(max) + NL + NL + H_USED_MEM;
  }

  @Override
  public void addNotify() {
    super.addNotify();
    timer.start();
  }

  @Override
  public void removeNotify() {
    timer.stop();
    super.removeNotify();
  }
}
