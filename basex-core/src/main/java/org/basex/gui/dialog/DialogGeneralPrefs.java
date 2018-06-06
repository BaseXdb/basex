package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.text.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.io.*;
import org.basex.util.*;

/**
 * General preferences.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
final class DialogGeneralPrefs extends BaseXBack {
  /** Main window reference. */
  private final GUI gui;

  /** Value of {@link GUIOptions#MAXRESULTS}. */
  private static final int[] MAXRESULTS = {
    50000, 100000, 250000, 500000, 1000000, 2500000, Integer.MAX_VALUE
  };
  /** Value of {@link GUIOptions#MAXTEXT}. */
  private static final int[] MAXTEXT = {
    1 << 20, 1 << 21, 1 << 22, 1 << 23, 1 << 24, 1 << 25, Integer.MAX_VALUE
  };

  /** Information on available languages. */
  private static final String[][] LANGS = Lang.parse();

  /** Directory path. */
  private final BaseXTextField dbPath;
  /** Repository path. */
  private final BaseXTextField repoPath;
  /** Number of hits. */
  private final BaseXSlider maxResults;
  /** Result cache. */
  private final BaseXSlider maxText;
  /** Label for number of hits. */
  private final BaseXLabel labelResults;
  /** Label for text size. */
  private final BaseXLabel labelText;

  /** Language label. */
  private final BaseXLabel creds;
  /** Language combobox. */
  private final BaseXCombo lang;
  /** Focus checkbox. */
  private final BaseXCheckBox mousefocus;
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
    final GUIOptions gopts = gui.gopts;
    dbPath = new BaseXTextField(dialog, opts.get(StaticOptions.DBPATH));
    repoPath = new BaseXTextField(dialog, opts.get(StaticOptions.REPOPATH));

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

    mousefocus = new BaseXCheckBox(dialog, RT_FOCUS, GUIOptions.MOUSEFOCUS, gopts);

    int val = sliderIndex(gui.gopts.get(GUIOptions.MAXRESULTS), MAXRESULTS);
    maxResults = new BaseXSlider(dialog, 0, MAXRESULTS.length - 1, val);
    maxResults.addActionListener(e -> action(maxResults));
    labelResults = new BaseXLabel(" ");

    val = sliderIndex(gui.gopts.get(GUIOptions.MAXTEXT), MAXTEXT);
    maxText = new BaseXSlider(dialog, 0, MAXTEXT.length - 1, val);
    maxText.addActionListener(e -> action(maxText));
    labelText = new BaseXLabel(" ");

    lang = new BaseXCombo(dialog, LANGS[0]);
    lang.setSelectedItem(opts.get(StaticOptions.LANG));
    creds = new BaseXLabel(" ");

    add(new BaseXLabel(DATABASE_PATH + COL, true, true));
    BaseXBack p = new BaseXBack(new ColumnLayout(8)), pp, ppp;
    p.add(dbPath);
    p.add(dbButton);
    add(p);

    add(new BaseXLabel(REPOSITORY_PATH + COL, true, true));
    p = new BaseXBack(new ColumnLayout(8));
    p.add(repoPath);
    p.add(repoButton);
    add(p);

    p = new BaseXBack(new TableLayout(2, 2, 24, 0));
    p.add(new BaseXLabel(LIMITS + COL, true, true).border(8, 0, 8, 0));
    p.add(new BaseXLabel(GUI_INTERACTIONS + COL, true, true).border(8, 0, 8, 0));

    pp = new BaseXBack(new RowLayout());
    ppp = new BaseXBack(new ColumnLayout(12));
    ppp.add(maxResults);
    ppp.add(labelResults);
    pp.add(new BaseXLabel(MAX_NO_OF_HITS + COL));
    pp.add(ppp);
    ppp = new BaseXBack(new ColumnLayout(12));
    ppp.add(maxText);
    ppp.add(labelText);
    pp.add(new BaseXLabel(SIZE_TEXT_RESULTS + COL));
    pp.add(ppp);
    p.add(pp);

    pp = new BaseXBack(new ColumnLayout(12));
    pp.add(mousefocus);
    p.add(pp);
    add(p);

    // checkbox for simple file dialog
    add(new BaseXLabel(LANGUAGE_RESTART + COL, true, true).border(8, 0, 8, 0));
    p = new BaseXBack(new ColumnLayout(12));
    p.add(lang);
    p.add(creds);
    add(p);
  }

  /**
   * Returns the selected maximum number of hits as slider value.
   * @param value value to be found
   * @param values allowed values
   * @return index
   */
  private static int sliderIndex(final int value, final int[] values) {
    final int hl = values.length - 1;
    int i = -1;
    while(++i < hl && values[i] < value);
    return i;
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
    mousefocus.assign();

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

    gui.gopts.set(GUIOptions.MAXTEXT, MAXTEXT[maxText.getValue()]);
    gui.gopts.set(GUIOptions.MAXRESULTS, MAXRESULTS[maxResults.getValue()]);

    creds.setText(TRANSLATION + COLS + creds(lang.getSelectedItem()));
    final int mr = MAXRESULTS[maxResults.getValue()];
    labelResults.setText(mr == Integer.MAX_VALUE ? ALL : new DecimalFormat("#,###,###").format(mr));
    final int mt = MAXTEXT[maxText.getValue()];
    labelText.setText(mt == Integer.MAX_VALUE ? ALL : Performance.format(mt));
    return true;
  }
}
