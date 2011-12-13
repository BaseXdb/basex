package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXEditor;
import org.basex.util.Token;

/**
 * Dialog window for messages.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class DialogMessage extends Dialog {
  /** This flag indicates if the dialog was canceled. */
  boolean canceled = true;
  /** Ok/yes button. */
  BaseXButton yes;
  /** No button. */
  BaseXButton no;
  /** Cancel button. */
  BaseXButton cancel;

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param txt message text
   * @param ic message type
   */
  DialogMessage(final GUI main, final String txt, final Msg ic) {
    super(main, ic == Msg.ERROR ? DIALOGERR : DIALOGINFO);

    panel.setLayout(new BorderLayout(12, 0));

    final BaseXLabel b = new BaseXLabel();
    b.setIcon(ic.large);
    set(b, BorderLayout.WEST);

    final BaseXEditor text = new BaseXEditor(false, this);
    text.setFont(b.getFont());
    text.setText(Token.token(txt));
    text.setFocusable(true);
    set(text, BorderLayout.CENTER);

    final BaseXBack buttons;
    if(ic == Msg.QUESTION || ic == Msg.YESNOCANCEL) {
      yes = new BaseXButton(BUTTONYES, this);
      no = new BaseXButton(BUTTONNO, this);
      if(ic == Msg.QUESTION) {
        buttons = newButtons(this, yes, no);
      } else {
        cancel = new BaseXButton(BUTTONCANCEL, this);
        buttons = newButtons(this, yes, no, cancel);
      }
    } else {
      yes = new BaseXButton(BUTTONOK, this);
      buttons = newButtons(this, yes);
    }
    set(buttons, BorderLayout.SOUTH);

    SwingUtilities.invokeLater(new Thread() {
      @Override
      public void run() {
        yes.requestFocusInWindow();
      }
    });
    finish(null);
  }

  @Override
  public void action(final Object cmp) {
    canceled = cmp != yes && cmp != no;
    if(cmp == yes) close();
    else cancel();
  }

  /**
   * States if the dialog window was canceled.
   * @return true when dialog was confirmed
   */
  public boolean canceled() {
    return canceled;
  }
}
