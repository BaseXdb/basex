package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.proc.Close;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.InfoDB;
import org.basex.core.proc.List;
import org.basex.data.MetaData;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXListChooser;
import org.basex.gui.layout.BaseXText;
import org.basex.io.DataInput;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * Open database dialog.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogOpen extends Dialog {
  /** List of currently available databases. */
  private final BaseXListChooser choice;
  /** Information panel. */
  private final BaseXLabel doc;
  /** Information panel. */
  private final BaseXText detail;
  /** Buttons. */
  private BaseXBack buttons;

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param drop show drop dialog
   */
  public DialogOpen(final GUI main, final boolean drop) {
    super(main, drop ? DROPTITLE : OPENTITLE);

    // create database chooser
    final StringList db = List.list(main.context);

    choice = new BaseXListChooser(db.finish(), HELPOPEN, this);
    set(choice, BorderLayout.CENTER);
    choice.setSize(130, 420);

    final BaseXBack info = new BaseXBack();
    info.setLayout(new BorderLayout());
    info.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(10, 10, 10, 10)));

    doc = new BaseXLabel(DIALOGINFO);
    doc.setFont(getFont().deriveFont(18f));
    doc.setBorder(0, 0, 5, 0);
    info.add(doc, BorderLayout.NORTH);

    detail = new BaseXText(HELPOPENINFO, false, this);
    detail.setFont(getFont());
    detail.setBorder(new EmptyBorder(5, 5, 5, 5));

    BaseXLayout.setWidth(detail, 420);
    info.add(detail, BorderLayout.CENTER);

    final BaseXBack pp = new BaseXBack();
    pp.setBorder(new EmptyBorder(0, 12, 0, 0));
    pp.setLayout(new BorderLayout());
    pp.add(info, BorderLayout.CENTER);

    // create buttons
    final BaseXBack p = new BaseXBack();
    p.setLayout(new BorderLayout());

    if(drop) {
      buttons = newButtons(this, true,
          new String[] { BUTTONDROP, BUTTONCANCEL },
          new byte[][] { HELPDROP, HELPCANCEL });
    } else {
      buttons = newButtons(this, true,
          new String[] { BUTTONRENAME, BUTTONOPEN, BUTTONCANCEL },
          new byte[][] { HELPRENAMEDB, HELPOPENDB, HELPCANCEL });
    }
    p.add(buttons, BorderLayout.EAST);
    pp.add(p, BorderLayout.SOUTH);

    set(pp, BorderLayout.EAST);
    action(null);
    if(db.size() == 0) return;

    finish(null);
  }

  /**
   * Returns the database name.
   * @return database name
   */
  public String db() {
    final String db = choice.getValue();
    return ok && db.length() > 0 ? db : null;
  }

  /**
   * Returns if no databases have been found.
   * @return result of check
   */
  public boolean nodb() {
    return choice.getIndex() == -1;
  }

  @Override
  public void action(final String cmd) {
    final Context ctx = gui.context;

    if(BUTTONRENAME.equals(cmd)) {
      new DialogRename(gui, choice.getValue());
      choice.setData(List.list(ctx).finish());
    } else if(BUTTONOPEN.equals(cmd)) {
      close();
    } else if(BUTTONDROP.equals(cmd)) {
      final String db = choice.getValue();
      if(db.length() == 0) return;
      if(Dialog.confirm(this, Main.info(DROPCONF, db))) {
        if(ctx.data() != null && ctx.data().meta.name.equals(db)) {
          new Close().execute(gui.context);
          gui.notify.init();
        }
        DropDB.drop(db, ctx.prop);
        choice.setData(List.list(ctx).finish());
        choice.requestFocusInWindow();
      }
    } else {
      final String db = choice.getValue().trim();
      ok = db.length() != 0 && ctx.prop.dbpath(db).exists();
      if(ok) doc.setText(db);
      enableOK(buttons, BUTTONDROP, ok);

      if(ok) {
        DataInput in = null;
        try {
          in = new DataInput(ctx.prop.dbfile(db, DATAINFO));
          final MetaData meta = new MetaData(db, in, ctx.prop);
          detail.setText(InfoDB.db(meta, true, true).finish());
        } catch(final IOException ex) {
          detail.setText(Token.token(ex.getMessage()));
          ok = false;
        } finally {
          try { if(in != null) in.close(); } catch(final IOException ex) { }
        }
      }
      enableOK(buttons, BUTTONOPEN, ok);
      enableOK(buttons, BUTTONRENAME, ok);
    }
  }

  @Override
  public void close() {
    if(ok) dispose();
  }
}
