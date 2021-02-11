package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Open database dialog.
 *
 * @author BaseX Team 2005-21, BSD License
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
  private final TextPanel detail;
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
  /** Combobox that lists available backups for a database. */
  private final BaseXList backups;
  /** Delete button for backups. */
  private final BaseXButton delete;
  /** Deletes all backups. */
  private final BaseXButton deleteAll;

  /** Refresh list of databases. */
  private boolean refresh;

  /**
   * Default constructor.
   * @param gui reference to the main window
   */
  public DialogManage(final GUI gui) {
    super(gui, MANAGE_DB);
    panel.setLayout(new BorderLayout(4, 0));

    // create database chooser
    final String[] dbs = gui.context.databases.list().finish();
    choice = new BaseXList(this, false, dbs);
    choice.setSize(250, 650);
    final Data data = gui.context.data();
    if(data != null) {
      data.flush(true);
      choice.setValue(data.meta.name);
    }

    doc1 = new BaseXLabel(" ").large();

    detail = new TextPanel(this, false);
    detail.setFont(panel.getFont());
    detail.setPreferredSize(new Dimension(600, 1));

    // database buttons
    rename = new BaseXButton(this, RENAME + DOTS);
    copy = new BaseXButton(this, COPY + DOTS);
    open = new BaseXButton(this, OPEN);
    drop = new BaseXButton(this, DROP + DOTS);

    // first tab
    final BaseXBack tab1 = new BaseXBack(new BorderLayout(0, 8)).border(8);
    tab1.add(doc1, BorderLayout.NORTH);
    tab1.add(new SearchEditor(gui, detail), BorderLayout.CENTER);
    tab1.add(newButtons(drop, rename, copy, open), BorderLayout.SOUTH);

    doc2 = new BaseXLabel(" ").border(0, 0, 6, 0);
    doc2.setFont(doc1.getFont());

    backups = new BaseXList(this);
    backups.setSize(600, 470);

    // backup buttons
    backup = new BaseXButton(this, BACKUP);
    restore = new BaseXButton(this, RESTORE);
    delete = new BaseXButton(this, DELETE);
    deleteAll = new BaseXButton(this, DELETE_ALL + DOTS);

    // second tab
    final BaseXBack tab2 = new BaseXBack(new BorderLayout(0, 8)).border(8);
    tab2.add(doc2, BorderLayout.NORTH);
    tab2.add(backups, BorderLayout.CENTER);
    tab2.add(newButtons(backup, restore, delete, deleteAll), BorderLayout.SOUTH);

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(INFORMATION, tab1);
    tabs.addTab(BACKUPS, tab2);

    set(choice, BorderLayout.CENTER);
    set(tabs, BorderLayout.EAST);

    action(null);
    if(dbs.length != 0) finish();
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
      choice.setData(ctx.databases.list().finish());
      choice.requestFocusInWindow();
      refresh = false;
    }

    final StringList dbs = choice.getValues();
    final String db = choice.getValue();
    final ArrayList<Command> cmds = new ArrayList<>();

    if(cmp == open) {
      close();

    } else if(cmp == drop) {
      for(final String s : dbs) {
        if(ctx.soptions.dbExists(s)) cmds.add(new DropDB(s));
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
      if(!gui.context.soptions.dbExists(db) || BaseXDialog.confirm(gui, OVERWRITE_DB_QUESTION))
        cmds.add(new Restore(backups.getValue()));

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

    } else {
      final String title = dbs.size() == 1 ? db : dbs.size() + " " + DATABASES;
      doc1.setChoppedText(title, 600);
      doc2.setChoppedText(BACKUPS + COLS + title, 600);

      boolean active = ctx.soptions.dbExists(db);
      String info = "";
      if(active) {
        // refresh info view
        final MetaData meta = new MetaData(db, ctx.options, ctx.soptions);
        try {
          meta.read();
          info = InfoDB.db(meta, true, true);
        } catch(final IOException ex) {
          info = Util.message(ex);
        }
      } else if(dbs.size() == 1) {
        info = ONLY_BACKUP;
      }
      detail.setText(info);

      // enable or disable buttons
      rename.setEnabled(active);
      copy.setEnabled(active);
      open.setEnabled(active);
      restore.setEnabled(active);

      active = false;
      for(final String d : dbs) active |= ctx.soptions.dbExists(d);
      drop.setEnabled(active);
      backup.setEnabled(active);

      // enable/disable backup buttons
      final String[] names = ctx.databases.backups(db).finish();
      active = names.length != 0;
      backups.setData(names);
      backups.setEnabled(active);

      restore.setEnabled(active);
      delete.setEnabled(active);
      deleteAll.setEnabled(active);
    }

    // run all commands
    if(!cmds.isEmpty()) {
      DialogProgress.execute(this, cmds.toArray(new Command[0]));
    }
  }

  @Override
  public void close() {
    final String db = choice.getValue();
    if(gui.context.soptions.dbExists(db)) {
      DialogProgress.execute(this, new Open(db));
      dispose();
    }
  }
}