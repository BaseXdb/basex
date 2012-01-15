package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;

import org.basex.core.Command;
import org.basex.core.Context;
import org.basex.core.cmd.AlterDB;
import org.basex.core.cmd.Copy;
import org.basex.core.cmd.CreateBackup;
import org.basex.core.cmd.DropBackup;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.InfoDB;
import org.basex.core.cmd.List;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.Restore;
import org.basex.core.cmd.ShowBackups;
import org.basex.data.MetaData;
import org.basex.gui.GUI;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXEditor;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXList;
import org.basex.gui.layout.BaseXTabs;
import org.basex.io.in.DataInput;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.list.ObjList;
import org.basex.util.list.StringList;

/**
 * Open database dialog.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogManage extends Dialog {
  /** List of currently available databases. */
  private final BaseXList choice;
  /** Name of current database. */
  private final BaseXLabel doc1;
  /** Name of current database. */
  private final BaseXLabel doc2;
  /** Information panel. */
  private final BaseXEditor detail;
  /** Rename button. */
  private final BaseXButton rename;
  /** Drop button. */
  private final BaseXButton drop;
  /** Open button. */
  private final BaseXButton open;
  /** Backup button. */
  private final BaseXButton backup;
  /** Restore button. */
  private final BaseXButton restore;
  /** Copy button. */
  private final BaseXButton copy;
  /** Refresh list of databases. */
  private boolean refresh;
  /** Combobox that lists available backups for a database. */
  private BaseXList backups;
  /** Delete button for backups. */
  private BaseXButton delete;
  /** Deletes all backups. */
  private final BaseXButton deleteAll;

  /**
   * Default constructor.
   * @param main reference to the main window
   * @param m show manage dialog
   */
  public DialogManage(final GUI main, final boolean m) {
    super(main, m ? MANAGETITLE : OPENTITLE);

    // create database chooser
    final StringList dbs = List.list(main.context, true);
    choice = new BaseXList(dbs.toArray(), this, !m);
    choice.setSize(190, 500);

    final Font f = panel.getFont();
    doc1 = new BaseXLabel(" ").border(0, 0, 5, 0);
    doc1.setFont(f.deriveFont(f.getSize2D() + 7));

    detail = new BaseXEditor(false, this);
    detail.border(5).setFont(f);
    BaseXLayout.setWidth(detail, 400);

    // database buttons
    copy = new BaseXButton(BUTTONCOPY, this);
    rename = new BaseXButton(BUTTONRENAME, this);
    open = new BaseXButton(BUTTONOPEN, this);
    drop = new BaseXButton(BUTTONDROP, this);

    // first tab
    final BaseXBack tab1 = new BaseXBack(new BorderLayout(0, 8)).border(8);
    tab1.add(doc1, BorderLayout.NORTH);
    tab1.add(detail, BorderLayout.CENTER);
    tab1.add(newButtons(this, drop, rename, copy, open), BorderLayout.SOUTH);

    doc2 = new BaseXLabel(" ").border(0, 0, 5, 0);
    doc2.setFont(f.deriveFont(f.getSize2D() + 7));

    backups = new BaseXList(new String[] { }, this);
    backups.setSize(400, 380);
    // backup buttons
    backup = new BaseXButton(BUTTONBACKUP, this);
    restore = new BaseXButton(BUTTONRESTORE, this);
    delete = new BaseXButton(BUTTONDELETE, this);
    deleteAll = new BaseXButton(BUTTONDELALL + DOTS, this);

    // second tab
    final BaseXBack tab2 = new BaseXBack(new BorderLayout(0, 8)).border(8);
    tab2.add(doc2, BorderLayout.NORTH);
    tab2.add(backups, BorderLayout.CENTER);
    tab2.add(newButtons(this, backup, restore, delete, deleteAll),
        BorderLayout.SOUTH);

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(DIALOGINFO, tab1);
    tabs.addTab(BACKUPS, tab2);

    panel.setLayout(new BorderLayout(8, 0));

    set(choice, BorderLayout.CENTER);
    set(tabs, BorderLayout.EAST);
    action(null);
    if(dbs.size() == 0) return;

    finish(null);
  }

  /**
   * Returns the name of the selected database.
   * @return database name
   */
  public String db() {
    return choice.getValue();
  }

  /**
   * Tests if no databases have been found.
   * @return result of check
   */
  public boolean nodb() {
    return choice.getList().length == 0;
  }

  @Override
  public void action(final Object cmp) {
    final Context ctx = gui.context;
    if(refresh) {
      // rebuild databases and focus list chooser
      choice.setData(List.list(ctx, true).toArray());
      choice.requestFocusInWindow();
      refresh = false;
    }

    final StringList dbs = choice.getValues();
    final String db = choice.getValue();
    final ObjList<Command> cmds = new ObjList<Command>();
    boolean active = dbs.size() > 0;

    if(cmp == open) {
      close();

    } else if(cmp == drop) {
      for(final String s : dbs) {
        if(ctx.mprop.dbexists(s)) cmds.add(new DropDB(s));
      }
      if(!Dialog.confirm(gui, Util.info(DROPCONF, cmds.size()))) return;
      refresh = true;

    } else if(cmp == rename) {
      final DialogInput dr = new DialogInput(db, RENAMETITLE, gui, 1);
      if(!dr.ok() || dr.input().equals(db)) return;
      cmds.add(new AlterDB(db, dr.input()));
      refresh = true;

    } else if(cmp == copy) {
      final DialogInput dc = new DialogInput(db, COPYTITLE, gui, 2);
      if(!dc.ok() || dc.input().equals(db)) return;
      cmds.add(new Copy(db, dc.input()));
      refresh = true;

    } else if(cmp == backup) {
      for(final String s : dbs) cmds.add(new CreateBackup(s));

    } else if(cmp == restore) {
      // show warning if existing database would be overwritten
      if(!gui.context.mprop.dbexists(db) || Dialog.confirm(gui, OVERWRITE))
        cmds.add(new Restore(db));

    } else if(cmp == backups) {
      // don't reset the combo box after selecting an item
      // no direct consequences if backup selection changes

    } else if(cmp == delete) {
      cmds.add(new DropBackup(backups.getValue()));
      refresh = backups.getList().length == 1;
      backups.requestFocusInWindow();

    } else if(cmp == deleteAll) {
      final String[] back = backups.getList();
      if(!Dialog.confirm(gui, Util.info(DROPBACKUP, back.length))) return;
      for(final String b : back) cmds.add(new DropBackup(b));
      refresh = true;

    } else if(cmp != backups) {
      final String title = dbs.size() == 1 ? db : dbs.size() + " " + DATABASES;
      doc1.setText(title);
      doc2.setText(title);

      active = ctx.mprop.dbexists(db);
      if(active) {
        // refresh info view
        DataInput in = null;
        final MetaData meta = new MetaData(db, ctx);
        try {
          in = new DataInput(meta.dbfile(DATAINF));
          meta.read(in);
          detail.setText(InfoDB.db(meta, true, true, true));
        } catch(final IOException ex) {
          detail.setText(Token.token(ex.getMessage()));
        } finally {
          if(in != null) try { in.close(); } catch(final IOException ex) { }
        }
      } else {
        detail.setText(dbs.size() == 1 ? Token.token(ONLYBACKUP) : Token.EMPTY);
      }

      // enable or disable buttons
      rename.setEnabled(active);
      copy.setEnabled(active);
      open.setEnabled(active);
      restore.setEnabled(active);

      active = false;
      for(final String d : dbs) active |= ctx.mprop.dbexists(d);
      drop.setEnabled(active);
      backup.setEnabled(active);

      // enable/disable backup buttons
      final String[] back = ShowBackups.list(db, false, ctx).toArray();
      active = back.length > 0;
      backups.setData(back);
      backups.setEnabled(active);

      restore.setEnabled(active);
      delete.setEnabled(active);
      deleteAll.setEnabled(active);
    }

    // run all commands
    if(cmds.size() != 0) {
      DialogProgress.execute(this, "", cmds.toArray(new Command[cmds.size()]));
    }
  }

  @Override
  public void close() {
    final String db = choice.getValue();
    if(gui.context.mprop.dbexists(db)) {
      DialogProgress.execute(this, "", new Open(db));
      dispose();
    }
  }
}