package org.basex.gui.dialog;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;

/**
 * Dialog window for messages.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DialogMessage extends BaseXDialog {
  /** Taken action. */
  private String action;

  /**
   * Default constructor.
   * @param gui reference to the main window
   * @param txt message text
   * @param ic message type
   * @param buttons additional buttons
   */
  public DialogMessage(final GUI gui, final String txt, final Msg ic, final String... buttons) {
    super(gui, ic == Msg.ERROR ? Text.ERROR : Text.INFORMATION);

    panel.setLayout(new BorderLayout());

    final BaseXBack back = new BaseXBack(new ColumnLayout(12));
    final BaseXLabel label = new BaseXLabel();
    label.setIcon(ic.large);
    back.add(label);

    // break longer texts
    final TextPanel tp = new TextPanel(this, txt.replaceAll("(.{1,160})", "$1").trim(), false);
    tp.setFont(label.getFont());
    back.add(tp);

    set(back, BorderLayout.NORTH);

    final ArrayList<Object> list = new ArrayList<>();
    if(ic == Msg.QUESTION || ic == Msg.YESNOCANCEL) {
      list.add(Text.B_YES);
      list.add(Text.B_NO);
      Collections.addAll(list, buttons);
      if(ic == Msg.YESNOCANCEL) list.add(Text.B_CANCEL);
    } else {
      Collections.addAll(list, buttons);
      list.add(Text.B_OK);
    }
    final BaseXBack bttns = newButtons(list.toArray(new Object[0]));
    set(bttns, BorderLayout.SOUTH);

    SwingUtilities.invokeLater(((Container) bttns.getComponent(0)).getComponent(0)::
      requestFocusInWindow);
    finish();
  }

  @Override
  public void action(final Object cmp) {
    final BaseXButton button = (BaseXButton) cmp;
    final String text = button.getText();
    if(text.equals(Text.B_CANCEL)) cancel();

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
