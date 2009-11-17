package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
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
  /** Write through flag. */
  private final BaseXCheckBox wth;

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param drop show drop dialog
   * @param fs file system flag
   */
  public DialogOpen(final GUI main, final boolean drop, final boolean fs) {
    super(main, drop ? DROPTITLE : fs ? OPENDQETITLE : OPENTITLE);

    // create database chooser
    final StringList db = fs ? List.listFS(main.context) :
      List.list(main.context);

    choice = new BaseXListChooser(db.finish(), this);
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

    detail = new BaseXText(false, this);
    detail.setFont(getFont());
    detail.setBorder(new EmptyBorder(5, 5, 5, 5));

    BaseXLayout.setWidth(detail, 420);
    info.add(detail, BorderLayout.CENTER);

    final BaseXBack pp = new BaseXBack();
    pp.setBorder(new EmptyBorder(0, 12, 0, 0));
    pp.setLayout(new BorderLayout());
    pp.add(info, BorderLayout.CENTER);

    wth = new BaseXCheckBox(WTHROUGH, false, this);
    wth.setBorder(new EmptyBorder(4, 4, 0, 0));
    wth.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        action(null);
      }
    });

    // create buttons
    final BaseXBack p = new BaseXBack();
    p.setLayout(new BorderLayout());
    if(fs) p.add(wth, BorderLayout.WEST);

    if(drop) {
      buttons = newButtons(this, true,
          new String[] { BUTTONDROP, BUTTONCANCEL });
    } else {
      buttons = newButtons(this, true,
          new String[] { BUTTONRENAME, BUTTONOPEN, BUTTONCANCEL });
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
      if(db.isEmpty()) return;
      if(Dialog.confirm(this, Main.info(DROPCONF, db))) {
        if(ctx.data != null && ctx.data.meta.name.equals(db)) {
          new Close().execute(gui.context);
          gui.notify.init();
        }
        DropDB.drop(db, ctx.prop);
        choice.setData(List.list(ctx).finish());
        choice.requestFocusInWindow();
      }
    } else {
      final String db = choice.getValue().trim();
      ok = !db.isEmpty() && ctx.prop.dbexists(db);
      enableOK(buttons, BUTTONDROP, ok);

      if(ok) {
        doc.setText(db);
        DataInput in = null;
        try {
          in = new DataInput(ctx.prop.dbfile(db, DATAINFO));
          final MetaData meta = new MetaData(db, in, ctx.prop);
          detail.setText(InfoDB.db(meta, true, true).finish());
          if(WTHROUGH.equals(cmd) && wth.isSelected()) {
            final boolean dec = Dialog.confirm(this,
                Main.info(WTHROUGHOK, meta.name, meta.backing));
            ctx.prop.set(Prop.WTHROUGH, dec);
            gui.prop.set(GUIProp.FSWTHROUGH, dec);
            wth.setSelected(dec);
          }
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
