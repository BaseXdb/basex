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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DialogMem extends BaseXDialog {
  /** Dialog. */
  private static Dialog dialog;

  /** Info text. */
  private final TextPanel text;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  private DialogMem(final GUI main) {
    super(main, USED_MEM, false);
    panel.setLayout(new BorderLayout());

    text = new TextPanel(Token.token(info()), false, this);
    text.setFont(panel.getFont());
    set(text, BorderLayout.CENTER);

    final BaseXButton gc = new BaseXButton("GC", this);
    final BaseXBack buttons = newButtons(gc);
    set(buttons, BorderLayout.SOUTH);

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        gc.requestFocusInWindow();
      }
    });
    finish(null);
  }

  /**
   * Activates the dialog window.
   * @param main reference to the main window
   */
  public static void show(final GUI main) {
    if(dialog == null) dialog = new DialogMem(main);
    dialog.setVisible(true);
  }

  @Override
  public void setVisible(final boolean v) {
    final boolean vis = isVisible();
    if(vis == v) return;

    super.setVisible(v);
    if(vis) return;

    // regularly refresh panel
    final Timer timer = new Timer(true);
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        while(isVisible()) {
          if(!text.selected()) text.setText(info());
        }
      }
    }, 0, 500);
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
    return TOTAL_MEM_C + Performance.format(max, true) + NL
        + RESERVED_MEM_C + Performance.format(total, true) + NL + MEMUSED_C
        + Performance.format(used, true) + NL + NL + H_USED_MEM;
  }
}
