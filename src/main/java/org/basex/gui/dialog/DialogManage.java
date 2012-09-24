package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.data.DataText.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.editor.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Open database dialog.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogManage extends BaseXDialog {
  /** List of currently available databases. */
  private final BaseXList choice;
  /** Name of current database. */
  private final BaseXLabel doc1;
  /** Name of current database. */
  private final BaseXLabel doc2;
  /** Information panel. */
  private final Editor detail;
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
  private final BaseXList backups;
  /** Delete button for backups. */
  private final BaseXButton delete;
  /** Deletes all backups. */
  private final BaseXButton deleteAll;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogManage(final GUI main) {
    super(main, MANAGE_DB);
    panel.setLayout(new BorderLayout(8, 0));

    // create database chooser
    final StringList dbs = main.context.databases().list();
    choice = new BaseXList(dbs.toArray(), this, false);
    choice.setSize(200, 500);

    doc1 = new BaseXLabel(" ").large();
    doc1.setSize(420, doc1.getHeight());

    detail = new Editor(false, this);
    detail.border(5).setFont(panel.getFont());

    // database buttons
    rename = new BaseXButton(RENAME + DOTS, this);
    copy = new BaseXButton(COPY + DOTS, this);
    open = new BaseXButton(OPEN, this);
    drop = new BaseXButton(DROP + DOTS, this);

    // first tab
    final BaseXBack tab1 = new BaseXBack(new BorderLayout(0, 8)).border(8);
    tab1.add(doc1, BorderLayout.NORTH);
    tab1.add(detail, BorderLayout.CENTER);
    tab1.add(newButtons(drop, rename, copy, open), BorderLayout.SOUTH);

    doc2 = new BaseXLabel(" ").border(0, 0, 6, 0);
    doc2.setFont(doc1.getFont());

    backups = new BaseXList(new String[] { }, this);
    backups.setSize(400, 380);

    // backup buttons
    backup = new BaseXButton(BACKUP, this);
    restore = new BaseXButton(RESTORE, this);
    delete = new BaseXButton(DELETE, this);
    deleteAll = new BaseXButton(DELETE_ALL + DOTS, this);

    // second tab
    final BaseXBack tab2 = new BaseXBack(new BorderLayout(0, 8)).border(8);
    tab2.add(doc2, BorderLayout.NORTH);
    tab2.add(backups, BorderLayout.CENTER);
    tab2.add(newButtons(backup, restore, delete, deleteAll), BorderLayout.SOUTH);

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(INFORMATION, tab1);
    tabs.addTab(BACKUPS, tab2);

    BaseXLayout.setWidth(detail, 400);
    BaseXLayout.setWidth(doc1, 400);
    BaseXLayout.setWidth(doc2, 400);
    set(choice, BorderLayout.CENTER);
    set(tabs, BorderLayout.EAST);

    action(null);
    if(!dbs.isEmpty()) finish(null);
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
      choice.setData(ctx.databases().list().toArray());
      choice.requestFocusInWindow();
      refresh = false;
    }

    final StringList dbs = choice.getValues();
    final String db = choice.getValue();
    final ArrayList<Command> cmds = new ArrayList<Command>();
    boolean active = dbs.size() > 0;

    if(cmp == open) {
      close();

    } else if(cmp == drop) {
      for(final String s : dbs) {
        if(ctx.mprop.dbexists(s)) cmds.add(new DropDB(s));
      }
      if(!BaseXDialog.confirm(gui, Util.info(DROPPING_DB_X, cmds.size()))) return;
      refresh = true;

    } else if(cmp == rename) {
      final DialogInput dr = new DialogInput(db, RENAME_DB, this, 1);
      if(!dr.ok() || dr.input().equals(db)) return;
      cmds.add(new AlterDB(db, dr.input()));
      refresh = true;

    } else if(cmp == copy) {
      final DialogInput dc = new DialogInput(db, COPY_DB, this, 2);
      if(!dc.ok() || dc.input().equals(db)) return;
      cmds.add(new Copy(db, dc.input()));
      refresh = true;

    } else if(cmp == backup) {
      for(final String s : dbs) cmds.add(new CreateBackup(s));

    } else if(cmp == restore) {
      // show warning if existing database would be overwritten
      if(!gui.context.mprop.dbexists(db) ||
          BaseXDialog.confirm(gui, OVERWRITE_DB_QUESTION)) cmds.add(new Restore(db));

    } else if(cmp == backups) {
      // don't reset the combo box after selecting an item
      // no direct consequences if backup selection changes

    } else if(cmp == delete) {
      cmds.add(new DropBackup(backups.getValue()));
      refresh = backups.getList().length == 1;
      backups.requestFocusInWindow();

    } else if(cmp == deleteAll) {
      final String[] back = backups.getList();
      if(!BaseXDialog.confirm(gui, Util.info(DROP_BACKUPS_X, back.length))) return;
      for(final String b : back) cmds.add(new DropBackup(b));
      refresh = true;

    } else if(cmp != backups) {
      final String title = dbs.size() == 1 ? db : dbs.size() + " " + DATABASES;
      doc1.setText(title);
      doc2.setText(BACKUPS + COLS + title);

      active = ctx.mprop.dbexists(db);
      if(active) {
        // refresh info view
        DataInput in = null;
        final MetaData meta = new MetaData(db, ctx);
        try {
          in = new DataInput(meta.dbfile(DATAINF));
          meta.read(in);
          detail.setText(Token.token(InfoDB.db(meta, true, true, true)));
        } catch(final IOException ex) {
          detail.setText(Token.token(ex.getMessage()));
        } finally {
          if(in != null) try { in.close(); } catch(final IOException ex) { }
        }
      } else {
        detail.setText(dbs.size() == 1 ? Token.token(ONLY_BACKUP) : Token.EMPTY);
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
      final String[] back = Databases.backupPaths(db, ctx).toArray();
      for(int b = 0; b < back.length; b++) {
        final String n = new IOFile(back[b]).name();
        back[b] = n.substring(0, n.lastIndexOf('.'));
      }

      active = back.length > 0;
      backups.setData(back);
      backups.setEnabled(active);

      restore.setEnabled(active);
      delete.setEnabled(active);
      deleteAll.setEnabled(active);
    }

    // run all commands
    if(!cmds.isEmpty()) {
      DialogProgress.execute(this, cmds.toArray(new Command[cmds.size()]));
    }
  }

  @Override
  public void close() {
    final String db = choice.getValue();
    if(gui.context.mprop.dbexists(db)) {
      DialogProgress.execute(this, new Open(db));
      dispose();
    }
  }
}