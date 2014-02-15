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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class DialogMem extends BaseXDialog {
  /** Info text. */
  private final TextPanel text;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogMem(final GUI main) {
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

  @Override
  public void setVisible(final boolean v) {
    final boolean vis = isVisible();
    if(vis == v) return;

    super.setVisible(v);
    if(vis) return;

    final Thread t = new Thread() {
      @Override
      public void run() {
        while(isVisible()) {
          if(!text.selected()) text.setText(info());
          Performance.sleep(500);
        }
      }
    };
    t.setDaemon(true);
    t.start();
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
  private String info() {
    final Runtime rt = Runtime.getRuntime();
    final long max = rt.maxMemory();
    final long total = rt.totalMemory();
    final long used = total - rt.freeMemory();
    return TOTAL_MEM_C + Performance.format(max, true) + NL
        + RESERVED_MEM_C + Performance.format(total, true) + NL + MEMUSED_C
        + Performance.format(used, true) + NL + NL + H_USED_MEM;
  }
}
