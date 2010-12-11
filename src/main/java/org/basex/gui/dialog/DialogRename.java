package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;
import static org.basex.util.Token.*;
import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import org.basex.core.Command;
import org.basex.core.cmd.List;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.util.StringList;
import org.basex.util.Token;
import org.basex.util.TokenList;
import org.basex.util.Util;

/**
 * Rename database dialog.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class DialogRename extends Dialog {
  /** New name. */
  private final BaseXTextField name;
  /** Old name. */
  private final String old;
  /** Buttons. */
  private final BaseXBack buttons;
  /** Info label. */
  private final BaseXLabel info;
  /** Available databases. */
  private final StringList db;
  /** Rename or delete dialog. */
  private final boolean wd;
  /** Target of delete. */
  private String target = "";

  /**
   * Default constructor.
   * @param dbname name of database
   * @param tit title string
   * @param main reference to the main window
   * @param fs file system flag
   * @param w flag for type of dialog
   */
  public DialogRename(final String dbname, final String tit, final GUI main,
      final boolean fs, final boolean w) {
    super(main, tit);
    old = dbname;
    db = fs ? List.listFS(main.context) : List.list(main.context);
    wd = w;

    set(new BaseXLabel(wd ? CREATENAME : CREATENAME2, false, true).border(0, 0,
        4, 0), BorderLayout.NORTH);

    name = new BaseXTextField(dbname, this);
    name.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        if(!modifier(e)) action(ENTER.is(e) ? e.getSource() : null);
      }
    });
    info = new BaseXLabel();

    final BaseXBack p = new BaseXBack(new BorderLayout(0, 8));
    p.add(name, BorderLayout.NORTH);
    p.add(info, BorderLayout.CENTER);
    set(p, BorderLayout.CENTER);

    buttons = newButtons(this, BUTTONOK, BUTTONCANCEL);
    set(buttons, BorderLayout.SOUTH);
    action(null);
    finish(null);
  }

  /**
   * Returns the edited database name.
   * @return name
   */
  public String name() {
    return name.getText().trim();
  }

  @Override
  public void action(final Object cmp) {
    if(wd) {
      final String nm = name();
      ok = !db.contains(nm) || nm.equals(old);
      String msg = ok ? null : Util.info(DBEXISTS, nm);
      if(ok) {
        ok = Command.validName(nm);
        if(!ok) msg = nm.isEmpty() ? DBWHICH : Util.info(INVALID, EDITNAME);
      }
      info.setText(msg, Msg.ERROR);
      enableOK(buttons, BUTTONOK, ok && !nm.isEmpty());
    } else {
      ok = false;
      target = name();
      if(!target.isEmpty()) {
        final Data d = gui.context.data;
        final TokenList l = new TokenList();
        for(int i = 0; i < d.meta.size; i += d.size(i, Data.DOC)) {
          l.add(d.text(i, true));
        }
        int tmp = 0;
        if(l.size() > 0) {
          final byte[] tmpT = Token.token(target);
          for(int i = 0; i < l.size(); ++i) if(eq(l.get(i), tmpT)) tmp++;
        }
        info.setText(tmp + " documents will be deleted.", Msg.WARN);
        if(tmp > 0) ok = true;
      }
      enableOK(buttons, BUTTONOK, ok);
    }
  }

  @Override
  public void close() {
    if(ok) super.close();
  }
}
