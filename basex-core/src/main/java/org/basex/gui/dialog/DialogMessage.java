package org.basex.gui.dialog;

import java.awt.*;
import java.util.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.*;
import org.basex.gui.layout.*;

/**
 * Dialog window for messages.
 *
 * @author BaseX Team, BSD License
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
    this(gui, txt, ic, false, buttons);
  }

  /**
   * Constructor.
   * @param gui         reference to the main window
   * @param txt         message text
   * @param ic          message type
   * @param destructive focus the No button of a question
   * @param buttons     additional buttons
   */
  public DialogMessage(final GUI gui, final String txt, final Msg ic, final boolean destructive,
      final String... buttons) {
    super(gui, ic == Msg.ERROR ? Text.ERROR :
      ic == Msg.QUESTION || ic == Msg.YESNOCANCEL ? Text.QUESTION : Text.INFORMATION);

    panel.setLayout(new BorderLayout());

    final BaseXBack back = new BaseXBack(new ColumnLayout(12));
    final BaseXLabel label = new BaseXLabel();
    label.setIcon(ic.large);
    back.add(label);
    back.add(new BaseXTextArea(this, txt.trim()));
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
    setButtons(newButtons(list.toArray(Object[]::new)), destructive ? 1 : 0);
    finish();
  }

  @Override
  public void action(final Object cmp) {
    action = ((BaseXButton) cmp).getText();
    close();
  }

  /**
   * Returns the chosen action.
   * @return action
   */
  public String action() {
    return action;
  }
}
