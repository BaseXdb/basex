package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import org.basex.core.Context;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.Close;
import org.basex.core.proc.DropDB;
import org.basex.core.proc.InfoDB;
import org.basex.core.proc.List;
import org.basex.data.MetaData;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
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
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
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
  private final BaseXBack buttons;
  /** Rename button. */
  private final Object rename;
  /** Drop button. */
  private final Object drop;
  /** Open button. */
  private final Object open;
  /** File system flag. */
  private final boolean fsInstance;

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param dr show drop dialog
   * @param fs file system flag
   */
  public DialogOpen(final GUI main, final boolean dr, final boolean fs) {
    super(main, dr ? DROPTITLE : fs ? OPENDQETITLE : OPENTITLE);

    // create database chooser
    final StringList db = fs ? List.listFS(main.context) :
      List.list(main.context);
    fsInstance = fs;

    choice = new BaseXListChooser(db.finish(), this);
    set(choice, BorderLayout.CENTER);
    choice.setSize(130, 440);

    final BaseXBack info = new BaseXBack();
    info.setLayout(new BorderLayout());
    info.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(10, 10, 10, 10)));

    final Font f = choice.getFont();
    doc = new BaseXLabel(DIALOGINFO);
    doc.setFont(f.deriveFont(f.getSize2D() + 7f));
    doc.setBorder(0, 0, 5, 0);
    info.add(doc, BorderLayout.NORTH);

    detail = new BaseXText(false, this);
    detail.setFont(f);
    detail.setBorder(new EmptyBorder(5, 5, 5, 5));

    BaseXLayout.setWidth(detail, 400);
    info.add(detail, BorderLayout.CENTER);

    final BaseXBack pp = new BaseXBack();
    pp.setBorder(new EmptyBorder(0, 12, 0, 0));
    pp.setLayout(new BorderLayout());
    pp.add(info, BorderLayout.CENTER);

    // create buttons
    final BaseXBack p = new BaseXBack();
    p.setLayout(new BorderLayout());

    rename = new BaseXButton(BUTTONRENAME, this);
    open = new BaseXButton(BUTTONOPEN, this);
    drop = new BaseXButton(BUTTONDROP + DOTS, this);
    buttons = dr ? newButtons(this, drop, BUTTONCANCEL) :
      newButtons(this, rename, open, BUTTONCANCEL);
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
  public void action(final Object cmp) {
    final Context ctx = gui.context;

    if(cmp == rename) {
      final String old = choice.getValue();
      final DialogRename dr = new DialogRename(old, gui, fsInstance);
      if(dr.ok()) {
        final Prop prop = gui.context.prop;
        prop.dbpath(old).renameTo(prop.dbpath(dr.name.getText()));
        choice.setData(fsInstance ? List.listFS(ctx).finish() :
          List.list(ctx).finish());
      }
    } else if(cmp == open) {
      close();
    } else if(cmp == drop) {
      final String db = choice.getValue();
      if(db.isEmpty()) return;
      if(Dialog.confirm(this, Main.info(DROPCONF, db))) {
        if(ctx.data != null && ctx.data.meta.name.equals(db)) {
          new Close().exec(gui.context);
          gui.notify.init();
        }
        DropDB.drop(db, ctx.prop);
        choice.setData(List.list(ctx).finish());
        choice.requestFocusInWindow();
      }
    } else {
      final String db = choice.getValue().trim();
      ok = !db.isEmpty() && ctx.prop.dbexists(db);
      enableOK(buttons, BUTTONDROP + DOTS, ok);

      if(ok) {
        doc.setText(db);
        DataInput in = null;
        final MetaData meta = new MetaData(db, ctx.prop);
        try {
          in = new DataInput(meta.file(DATAINFO));
          meta.read(in);
          detail.setText(InfoDB.db(meta, true, true, true));
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
