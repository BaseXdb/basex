package org.basex.gui.dialog;

import java.awt.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.editor.*;
import org.basex.gui.layout.*;
import org.basex.util.*;

/**
 * Dialog window for messages.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogMessage extends BaseXDialog {
  /** Ok/yes button. */
  final BaseXButton yes;

  /** This flag indicates if the dialog was canceled. */
  private boolean canceled = true;
  /** No button. */
  private BaseXButton no;

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param txt message text
   * @param ic message type
   */
  public DialogMessage(final GUI main, final String txt, final Msg ic) {
    super(main, ic == Msg.ERROR ? Text.ERROR : Text.INFORMATION);

    panel.setLayout(new BorderLayout());

    final BaseXBack back = new BaseXBack(new TableLayout(1, 2, 12, 0));
    final BaseXLabel b = new BaseXLabel();
    b.setIcon(ic.large);
    back.add(b);

    final Editor text = new Editor(false, this, Token.token(txt));
    text.setFont(b.getFont());
    back.add(text);

    set(back, BorderLayout.NORTH);

    final BaseXBack buttons;
    if(ic == Msg.QUESTION || ic == Msg.YESNOCANCEL) {
      yes = new BaseXButton(Text.B_YES, this);
      no = new BaseXButton(Text.B_NO, this);
      if(ic == Msg.QUESTION) {
        buttons = newButtons(yes, no);
      } else {
        buttons = newButtons(yes, no, new BaseXButton(Text.B_CANCEL, this));
      }
    } else {
      yes = new BaseXButton(Text.B_OK, this);
      buttons = newButtons(yes);
    }
    set(buttons, BorderLayout.SOUTH);

    SwingUtilities.invokeLater(new Runnable() {
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
