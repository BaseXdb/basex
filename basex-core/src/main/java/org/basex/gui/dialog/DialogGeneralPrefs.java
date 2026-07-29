package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * General preferences.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DialogGeneralPrefs extends BaseXBack {
  /** Main window reference. */
  private final GUI gui;

  /** Directory path. */
  private final BaseXTextField dbPath;
  /** Repository path. */
  private final BaseXTextField repoPath;
  /** XML Suffixes. */
  private final BaseXTextField xmlSuffixes;

  /** Browse database path. */
  private final BaseXButton dbButton;
  /** Browse repository path. */
  private final BaseXButton repoButton;

  /**
   * Default constructor.
   * @param dialog dialog reference
   */
  DialogGeneralPrefs(final BaseXDialog dialog) {
    border(8).setLayout(new RowLayout());
    gui = dialog.gui();

    final StaticOptions opts = gui.context.soptions;
    dbPath = new BaseXTextField(dialog, opts.get(StaticOptions.DBPATH));
    repoPath = new BaseXTextField(dialog, opts.get(StaticOptions.REPOPATH));
    xmlSuffixes = new BaseXTextField(dialog, GUIOptions.XMLSUFFIXES, gui.gopts);
    xmlSuffixes.hint(GUIOptions.XMLSUFFIXES.value());

    dbButton = new BaseXButton(dialog, BROWSE_D);
    dbButton.addActionListener(e -> {
      final String path = dbPath.getText();
      final IOFile dir = new BaseXFileChooser(dialog, CHOOSE_DIR, path).select(Mode.DOPEN);
      if(dir != null) dbPath.setText(dir.path());
    });

    repoButton = new BaseXButton(dialog, BROWSE_D);
    repoButton.addActionListener(e -> {
      final String path = repoPath.getText();
      final IOFile dir = new BaseXFileChooser(dialog, CHOOSE_DIR, path).select(Mode.DOPEN);
      if(dir != null) repoPath.setText(dir.path());
    });

    add(new BaseXLabel(DATABASE_PATH + COL, true, true));
    BaseXBack p = new BaseXBack(new ColumnLayout(8));
    p.add(dbPath);
    p.add(dbButton);
    add(p);

    add(new BaseXLabel(REPOSITORY_PATH + COL, true, true));
    p = new BaseXBack(new ColumnLayout(8));
    p.add(repoPath);
    p.add(repoButton);
    add(p);

    add(new BaseXLabel(Util.info(FILE_SUFFIXES_X, "XML") + COL, true, true));
    p = new BaseXBack(new ColumnLayout(8));
    p.add(xmlSuffixes);
    add(p);
  }

  /**
   * Reacts on user input.
   * @param source source
   * @return success flag
   */
  boolean action(final Object source) {
    // new database path: close opened database
    final StaticOptions opts = gui.context.soptions;
    if(source == dbPath || source == dbButton) {
      final String dbpath = dbPath.getText();
      if(!opts.get(StaticOptions.DBPATH).equals(dbpath) && gui.context.data() != null) {
        Close.close(gui.context);
        gui.notify.init();
      }
      opts.set(StaticOptions.DBPATH, dbpath);
    } else if(source == repoPath || source == repoButton) {
      gui.context.repo.reset();
      opts.set(StaticOptions.REPOPATH, repoPath.getText());
    }
    xmlSuffixes.assign();
    return true;
  }
}
