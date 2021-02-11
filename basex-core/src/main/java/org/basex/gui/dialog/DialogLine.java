package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.gui.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Go to line dialog.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DialogLine extends BaseXDialog {
  /** New password. */
  private final BaseXTextField line;
  /** Buttons. */
  private final BaseXBack buttons;
  /** Info label. */
  private final BaseXLabel info;

  /**
   * Default constructor.
   * @param gui reference to the main window
   * @param curr current line
   */
  public DialogLine(final GUI gui, final int curr) {
    super(gui, GO_TO_LINE);

    line = new BaseXTextField(this, Integer.toString(curr));
    line.addKeyListener(keys);
    info = new BaseXLabel(" ");

    final BaseXBack p = new BaseXBack(new BorderLayout(0, 8));
    p.add(line, BorderLayout.NORTH);
    p.add(info, BorderLayout.CENTER);
    set(p, BorderLayout.CENTER);

    buttons = newButtons(B_OK, B_CANCEL);
    set(buttons, BorderLayout.SOUTH);
    action(null);
    finish();
  }

  @Override
  public void action(final Object cmp) {
    final int l = line();
    ok = l >= 0;
    info.setText(ok || line.getText().isEmpty() ? null :
      Util.info(INVALID_X, LINE_NUMBER), Msg.ERROR);
    enableOK(buttons, B_OK, ok);
  }

  @Override
  public void close() {
    if(ok) super.close();
  }

  /**
   * Returns the entered line number.
   * @return line number, or {@code -1} or invalid values
   */
  public int line() {
    try {
      return Integer.parseInt(line.getText());
    } catch(final NumberFormatException ex) {
      Util.debug(ex);
      return -1;
    }
  }
}
