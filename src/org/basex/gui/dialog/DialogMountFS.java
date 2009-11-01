package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.basex.core.Context;
import org.basex.core.proc.InfoDB;
import org.basex.core.proc.List;
import org.basex.data.MetaData;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
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

/**
 * Open database dialog.
 * 
 * @author Workgroup DBIS, University of Konstanz 2009, ISC License
 * @author Alexander Holupirek
 */
public final class DialogMountFS extends Dialog {
  /** List of currently available databases. */
  private final BaseXListChooser choice;
  /** Information panel. */
  private final BaseXLabel doc;
  /** Information panel. */
  private final BaseXText detail;
  /** Browse button. */
  private final BaseXButton button;
  /** Mountpoint warning. */
  private final BaseXLabel warn;
  /** Buttons. */
  private BaseXBack buttons;

  /** Mountpoint path. */
  final BaseXTextField mountpoint;
  
  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogMountFS(final GUI main) {
    super(main, OPENMOUNTTITLE);
    final GUIProp gprop = gui.prop;

    // create database chooser
    final StringList db = List.listFS(main.context);

    choice = new BaseXListChooser(db.finish(), this);

    set(choice, BorderLayout.CENTER);
    choice.setSize(130, 420);

    final BaseXBack info = new BaseXBack();
    info.setLayout(new BorderLayout());
    info.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(10,
        10, 10, 10)));

    doc = new BaseXLabel(DIALOGINFO);
    doc.setFont(getFont().deriveFont(16f));
    doc.setBorder(0, 0, 2, 0);
    info.add(doc, BorderLayout.NORTH);

    detail = new BaseXText(false, this);
    detail.setFont(getFont().deriveFont(10f));
    detail.setBorder(new EmptyBorder(5, 5, 5, 5));
    BaseXLayout.setWidth(detail, 420);
    info.add(detail, BorderLayout.CENTER);

    // -- mount panel
    final BaseXBack m = new BaseXBack();
    m.setLayout(new TableLayout(3, 2, 0, 0));
    m.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5,
        5, 5)));
    final BaseXLabel lab = new BaseXLabel("Using mount point: ", false, true);
    lab.setBorder(new EmptyBorder(5, 5, 5, 0));
    m.add(lab);
    m.add(new BaseXLabel(""));
    mountpoint = new BaseXTextField(gprop.get(GUIProp.FSMOUNT), this);
    mountpoint.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        action(null);
      }
    });
    BaseXLayout.setWidth(mountpoint, 300);
    m.add(mountpoint);
    button = new BaseXButton(BUTTONBROWSE, this);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final IO file = new BaseXFileChooser(DIALOGFC, mountpoint.getText(),
            main).select(BaseXFileChooser.Mode.DOPEN);
         if(file != null) {
          mountpoint.setText(file.path());
        }
      }
    });
    m.add(button);
    warn = new BaseXLabel(" ");
    warn.setBorder(5, 5, 0, 0);
    m.add(warn);
    info.add(m, BorderLayout.SOUTH);

    final BaseXBack pp = new BaseXBack();
    pp.setBorder(new EmptyBorder(0, 12, 0, 0));
    pp.setLayout(new BorderLayout());
    pp.add(info, BorderLayout.CENTER);

    // create buttons
    buttons = newButtons(this, true, new String[] { BUTTONMOUNT, BUTTONCANCEL});
    final BaseXBack p = new BaseXBack();
    p.setLayout(new BorderLayout());
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
   * Returns the mount point.
   * @return database name
   */
  public String mp() {
    final String db = mountpoint.getText().trim();
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
    final String db = choice.getValue().trim();
    final String mp = mountpoint.getText().trim();
    
    if(BUTTONMOUNT.equals(cmd)) {
//      DeepFSImpl.main(new String[] {mp, db});
      close();
    } else {
      ok = !db.isEmpty() && ctx.prop.dbpath(db).exists();
      warn.setText(" ");
      warn.setIcon(null);
      if(ok) {
        doc.setText("Mount " + db + ":");
        DataInput in = null;
        try {
          // fill detail panel with db info
          in = new DataInput(ctx.prop.dbfile(db, DATAINFO));
          final MetaData meta = new MetaData(db, in, ctx.prop);
          detail.setText(InfoDB.db(meta, true, true).finish());
          // check for valid mountpoint
          final IO file = IO.get(mp);
          final boolean mpok = !mp.isEmpty() && file.exists() &&
            file.isDir();
          if (!mpok) {
            warn.setText(NOVALIDMOUNT);
            warn.setIcon(BaseXLayout.icon("error"));
          } 
          ok &= mpok;
        } catch(final IOException ex) {
          detail.setText(Token.token(ex.getMessage()));
          ok = false;
        } finally {
          try {
            if(in != null) in.close();
          } catch(final IOException ex) { /* */}
        }
      }
      enableOK(buttons, BUTTONMOUNT, ok);
    }
  }

  @Override
  public void close() {
    if(ok) dispose();
  }
}