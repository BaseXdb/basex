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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class DialogGeneralPrefs extends BaseXBack {
  /** Main window reference. */
  private final GUI gui;

  /** Information on available languages. */
  private static final String[][] LANGS = Lang.parse();

  /** Directory path. */
  private final BaseXTextField dbPath;
  /** Repository path. */
  private final BaseXTextField repoPath;
  /** XML Suffixes. */
  private final BaseXTextField xmlSuffixes;

  /** Language label. */
  private final BaseXLabel creds;
  /** Language combobox. */
  private final BaseXCombo lang;
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
    gui = dialog.gui;

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

    lang = new BaseXCombo(dialog, LANGS[0]);
    lang.setSelectedItem(opts.get(StaticOptions.LANG));
    creds = new BaseXLabel(" ");

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

    // checkbox for simple file dialog
    add(new BaseXLabel(LANGUAGE_RESTART + COL, true, true).border(8, 0, 8, 0));
    p = new BaseXBack(new ColumnLayout(12));
    p.add(lang);
    p.add(creds);
    add(p);
  }

  /**
   * Returns the translation credits for the specified language.
   * @param lng language
   * @return credits
   */
  static String creds(final String lng) {
    final int ll = LANGS[0].length;
    for(int l = 0; l < ll; l++) {
      if(LANGS[0][l].equals(lng)) return LANGS[1][l];
    }
    return "";
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
    opts.set(StaticOptions.LANG, lang.getSelectedItem());
    xmlSuffixes.assign();

    creds.setText(TRANSLATION + COLS + creds(lang.getSelectedItem()));
    return true;
  }
}
