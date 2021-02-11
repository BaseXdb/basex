package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.util.*;
import java.util.Timer;

import javax.swing.*;

import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.util.*;

/**
 * Dialog with a single text field.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DialogMem extends BaseXDialog {
  /** Dialog. */
  private static Dialog dialog;

  /** Info text. */
  private final TextPanel text;
  /** GC Button. */
  private final BaseXButton gc;

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
    addTimer();
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
    return TOTAL_MEM_C + Performance.format(max) + NL
        + RESERVED_MEM_C + Performance.format(total) + NL + MEMUSED_C
        + Performance.format(used) + NL + NL + H_USED_MEM;
  }

  /**
   * Add timer for updating display of memory consumption.
   */
  private void addTimer() {
    new Timer(true).scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        if(isVisible() && !text.selected()) text.setText(info());
      }
    }, 0, 500);
  }
}
