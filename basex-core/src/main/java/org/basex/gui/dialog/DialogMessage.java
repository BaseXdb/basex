package org.basex.gui.dialog;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.util.*;

/**
 * Dialog window for messages.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class DialogMessage extends BaseXDialog {
  /** Taken action. */
  private String action;

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param txt message text
   * @param ic message type
   * @param buttons additional buttons
   */
  public DialogMessage(final GUI main, final String txt, final Msg ic, final String... buttons) {
    super(main, ic == Msg.ERROR ? Text.ERROR : Text.INFORMATION);

    panel.setLayout(new BorderLayout());

    final BaseXBack back = new BaseXBack(new TableLayout(1, 2, 12, 0));
    final BaseXLabel label = new BaseXLabel();
    label.setIcon(ic.large);
    back.add(label);

    final TextPanel text = new TextPanel(Token.token(txt), false, this);
    text.setFont(label.getFont());
    back.add(text);

    set(back, BorderLayout.NORTH);

    final ArrayList<Object> list = new ArrayList<>();
    if(ic == Msg.QUESTION || ic == Msg.YESNOCANCEL) {
      list.add(Text.B_YES);
      list.add(Text.B_NO);
      Collections.addAll(list, buttons);
      if(ic == Msg.YESNOCANCEL) list.add(Text.CANCEL);
    } else {
      Collections.addAll(list, buttons);
      list.add(Text.B_OK);
    }
    final BaseXBack bttns = newButtons(list.toArray(new Object[list.size()]));
    set(bttns, BorderLayout.SOUTH);

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        ((Container) bttns.getComponent(0)).getComponent(0).requestFocusInWindow();
      }
    });

    finish(null);
  }

  @Override
  public void action(final Object cmp) {
    final BaseXButton button = (BaseXButton) cmp;
    final String text = button.getText();
    if(text.equals(Text.CANCEL)) cancel();

    action = text;
    if(text.equals(Text.NO)) {
      cancel();
    } else {
      close();
    }
  }

  /**
   * Returns the chosen action.
   * @return action
   */
  public String action() {
    return action;
  }
}
