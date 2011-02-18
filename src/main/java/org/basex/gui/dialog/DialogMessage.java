package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants.Fill;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXText;
import org.basex.util.Token;

/**
 * Dialog window for messages.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class DialogMessage extends Dialog {
  /** Button. */
  final BaseXButton button;

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param txt message text
   * @param ic message type
   */
  DialogMessage(final GUI main, final String txt, final Msg ic) {
    super(main, ic == Msg.ERROR ? DIALOGERR : DIALOGINFO);

    final BaseXBack p = new BaseXBack(new BorderLayout()).
      border(0, 0, 0, 16).mode(Fill.NONE);
    final BaseXLabel b = new BaseXLabel();
    b.setIcon(ic.large);
    p.add(b, BorderLayout.NORTH);
    set(p, BorderLayout.WEST);

    final BaseXText text = new BaseXText(false, this);
    text.setFont(p.getFont());
    text.setText(Token.token(txt));
    text.setFocusable(false);
    set(text, BorderLayout.CENTER);

    final boolean simple = ic != Msg.QUESTION;
    button = new BaseXButton(simple ? BUTTONOK : BUTTONYES, this);
    final BaseXBack buttons = simple ? newButtons(this, button) :
        newButtons(this, button, new BaseXButton(BUTTONNO, this));
    set(buttons, BorderLayout.SOUTH);

    SwingUtilities.invokeLater(new Thread() {
      @Override
      public void run() {
        button.requestFocusInWindow();
        text.setFocusable(true);
      }
    });
    finish(null);
  }

  @Override
  public void action(final Object cmp) {
    if(cmp == button) close();
    else cancel();
  }
}
