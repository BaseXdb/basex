package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXText;
import org.basex.util.Token;

/**
 * Dialog window for messages.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class DialogMessage extends Dialog {
  /**
   * Default constructor.
   * @param main reference to the main window
   * @param txt message text
   * @param ic message type
   */
  DialogMessage(final GUI main, final String txt, final Msg ic) {
    super(main, ic == Msg.ERROR ? DIALOGERR : DIALOGINFO, true);

    final BaseXBack p = new BaseXBack();
    p.setLayout(new BorderLayout());
    p.setBorder(0, 0, 0, 16);
    p.setOpaque(false);
    final BaseXLabel b = new BaseXLabel();
    b.setIcon(ic.large);
    p.add(b, BorderLayout.NORTH);
    set(p, BorderLayout.WEST);

    final BaseXText text = new BaseXText(false, this);
    text.setFont(p.getFont());
    text.setText(Token.token(txt));
    set(text, BorderLayout.CENTER);
    final BaseXButton button = new BaseXButton(BUTTONOK, this);
    set(newButtons(this, button), BorderLayout.SOUTH);

    SwingUtilities.invokeLater(new Thread() {
      @Override
      public void run() {
        button.requestFocusInWindow();
      }
    });
    finish(null);
  }
}
