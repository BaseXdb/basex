/*
package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import org.basex.core.Context;
import org.basex.core.cmd.InfoDB;
import org.basex.core.cmd.List;
import org.basex.data.MetaData;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXListChooser;
import org.basex.gui.layout.BaseXText;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.DataInput;
import org.basex.io.IO;
import org.basex.util.StringList;
import org.basex.util.Token;

/*
 * Open database dialog.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Alexander Holupirek
public final class DialogMountFS extends Dialog {
  /** Mountpoint path.
  final BaseXTextField mountpoint;

  /** List of currently available databases.
  private final BaseXListChooser choice;
  /** Information panel.
  private final BaseXLabel doc;
  /** Information panel.
  private final BaseXText detail;
  /** Browse button.
  private final BaseXButton browse;
  /** Mount button.
  private final Object mount;
  /** Mountpoint warning.
  private final BaseXLabel warn;
  /** Buttons.
  private final BaseXBack buttons;

  /**
   * Default constructor.
   * @param main reference to the main window

  public DialogMountFS(final GUI main) {
    super(main, OPENMOUNTTITLE);
    final GUIProp gprop = gui.gprop;

    // create database chooser
    final StringList db = List.listFS(main.context);

    choice = new BaseXListChooser(db.toArray(), this);

    set(choice, BorderLayout.CENTER);
    choice.setSize(130, 420);

    final BaseXBack info = new BaseXBack(new BorderLayout());
    info.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(10,
        10, 10, 10)));

    final Font f = choice.getFont();
    doc = new BaseXLabel(DIALOGINFO).border(0, 0, 2, 0);
    doc.setFont(f.deriveFont(f.getSize2D() + 7f));
    info.add(doc, BorderLayout.NORTH);

    detail = new BaseXText(false, this);
    detail.border(5, 5, 5, 5).setFont(f);
    BaseXLayout.setWidth(detail, 420);
    info.add(detail, BorderLayout.CENTER);

    // -- mount panel
    final BaseXBack m = new BaseXBack(new TableLayout(3, 2, 0, 0));
    m.setBorder(new CompoundBorder(new EtchedBorder(),
        new EmptyBorder(5, 5, 5, 5)));
    m.add(new BaseXLabel("Using mount point: ", false, true).border(
        5, 5, 5, 0));
    m.add(new BaseXLabel());
    mountpoint = new BaseXTextField(gprop.get(GUIProp.FSMOUNT), this);
    mountpoint.addKeyListener(keys);
    BaseXLayout.setWidth(mountpoint, 300);
    m.add(mountpoint);
    browse = new BaseXButton(BUTTONBROWSE, this);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final IO file = new BaseXFileChooser(DIALOGFC, mountpoint.getText(),
            main).select(BaseXFileChooser.Mode.DOPEN);
         if(file != null) mountpoint.setText(file.path());
      }
    });
    m.add(browse);
    warn = new BaseXLabel(" ").border(5, 5, 0, 0);
    m.add(warn);
    info.add(m, BorderLayout.SOUTH);

    final BaseXBack pp = new BaseXBack(new BorderLayout()).border(0, 12, 0, 0);
    pp.add(info, BorderLayout.CENTER);

    // create buttons
    mount = new BaseXButton(BUTTONMOUNT, this);
    buttons = newButtons(this, mount, BUTTONCANCEL);
    final BaseXBack p = new BaseXBack(new BorderLayout());
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

  public String db() {
    final String db = choice.getValue();
    return ok && db.length() > 0 ? db : null;
  }

  /**
   * Returns the mount point.
   * @return database name

  public String mp() {
    final String db = mountpoint.getText().trim();
    return ok && db.length() > 0 ? db : null;
  }

  /**
   * Tests if no databases have been found.
   * @return result of check

  public boolean nodb() {
    return choice.getIndex() == -1;
  }

  @Override
  public void action(final Object cmp) {
    final Context ctx = gui.context;
    final String db = choice.getValue().trim();
    final String mp = mountpoint.getText().trim();

    if(cmp == mount) {
      //DeepFSImpl.main(new String[] {mp, db});
      close();
    } else {
      ok = ctx.prop.dbexists(db);
      warn.setText(null, null);
      if(ok) {
        doc.setText(BUTTONMOUNT + " " + db + COL);
        DataInput in = null;
        final MetaData meta = new MetaData(db, ctx.prop);
        try {
          // fill detail panel with db info
          in = new DataInput(meta.file(DATAINFO));
          meta.read(in);
          detail.setText(InfoDB.db(meta, true, true, true));
          // check for valid mountpoint
          final IO file = IO.get(mp);
          final boolean mpok = !mp.isEmpty() && file.exists() &&
            file.isDir();
          if(!mpok) warn.setText(NOVALIDMOUNT, Msg.ERROR);
          ok &= mpok;
        } catch(final IOException ex) {
          detail.setText(Token.token(ex.getMessage()));
          ok = false;
        } finally {
          if(in != null) try { in.close(); } catch(final IOException ex) { }
        }
      }
      enableOK(buttons, BUTTONMOUNT, ok);
    }
  }

  @Override
  public void close() {
    if(ok) dispose();
  }
}*/