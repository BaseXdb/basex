package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.basex.core.Command;
import org.basex.core.cmd.List;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.util.StringList;
import org.basex.util.Util;

/**
 * Rename database/drop documents dialog.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DialogInput extends Dialog {
  /** User input. */
  private final BaseXTextField input;
  /** Old input. */
  private final String old;
  /** Buttons. */
  private final BaseXBack buttons;
  /** Info label. */
  private final BaseXLabel info;
  /** Available databases. */
  private final StringList db;
  /** Rename or delete dialog. */
  private final boolean rename;

  /**
   * Default constructor.
   * @param o old input
   * @param tit title string
   * @param main reference to the main window
   * @param fs file system flag
   * @param ren type of dialog (rename database/drop documents)
   */
  public DialogInput(final String o, final String tit, final GUI main,
      final boolean fs, final boolean ren) {
    super(main, tit);
    old = o;
    db = fs ? List.listFS(main.context) : List.list(main.context);
    rename = ren;

    set(new BaseXLabel(rename ? CREATENAME : CREATETARGET, false, true).border(
        0, 0, 4, 0), BorderLayout.NORTH);

    input = new BaseXTextField(o, this);
    input.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        if(!modifier(e)) action(ENTER.is(e) ? e.getSource() : null);
      }
    });
    info = new BaseXLabel(" ");

    final BaseXBack p = new BaseXBack(new BorderLayout(0, 8));
    p.add(input, BorderLayout.NORTH);
    p.add(info, BorderLayout.CENTER);
    set(p, BorderLayout.CENTER);

    buttons = newButtons(this, BUTTONOK, BUTTONCANCEL);
    set(buttons, BorderLayout.SOUTH);
    action(null);
    finish(null);
  }

  /**
   * Returns the user input.
   * @return input
   */
  public String input() {
    return input.getText().trim();
  }

  @Override
  public void action(final Object cmp) {
    final String in = input();
    String msg = null;
    if(rename) {
      ok = !db.contains(in) || in.equals(old);
      if(ok) msg = Util.info(DBEXISTS, in);
      if(ok) {
        ok = Command.validName(in);
        if(!ok) msg = in.isEmpty() ? DBWHICH : Util.info(INVALID, EDITNAME);
      }
    } else {
      final int docs = in.isEmpty() ? 0 : gui.context.data.doc(in).length;
      msg = Util.info(DELETEPATH, docs);
      ok = docs != 0;
    }
    info.setText(msg, rename ? Msg.ERROR : Msg.WARN);
    enableOK(buttons, BUTTONOK, ok);
  }

  @Override
  public void close() {
    if(ok) super.close();
  }
}
