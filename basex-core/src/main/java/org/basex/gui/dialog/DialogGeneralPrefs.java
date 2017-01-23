package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.event.*;
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
 * @author BaseX Team 2005-17, BSD License
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
  /** Simple file dialog checkbox. */
  private final BaseXCheckBox simplefd;
  /** Browse button. */
  private final BaseXButton dbButton;
  /** Browse button. */
  private final BaseXButton repoButton;

  /**
   * Default constructor.
   * @param d dialog reference
   */
  DialogGeneralPrefs(final BaseXDialog d) {
    border(8).setLayout(new TableLayout(10, 1));
    gui = d.gui;

    final StaticOptions opts = gui.context.soptions;
    final GUIOptions gopts = gui.gopts;
    dbPath = new BaseXTextField(opts.get(StaticOptions.DBPATH), d);
    repoPath = new BaseXTextField(opts.get(StaticOptions.REPOPATH), d);

    dbButton = new BaseXButton(BROWSE_D, d);
    dbButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final String path = dbPath.getText();
        final IOFile dir = new BaseXFileChooser(CHOOSE_DIR, path, gui).select(Mode.DOPEN);
        if(dir != null) dbPath.setText(dir.path());
      }
    });

    repoButton = new BaseXButton(BROWSE_D, d);
    repoButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final String path = repoPath.getText();
        final IOFile dir = new BaseXFileChooser(CHOOSE_DIR, path, gui).select(Mode.DOPEN);
        if(dir != null) repoPath.setText(dir.path());
      }
    });

    mousefocus = new BaseXCheckBox(RT_FOCUS, GUIOptions.MOUSEFOCUS, gopts, d);
    simplefd = new BaseXCheckBox(SIMPLE_FILE_CHOOSER, GUIOptions.SIMPLEFD, gopts, d);

    int val = sliderIndex(gui.gopts.get(GUIOptions.MAXRESULTS), MAXRESULTS);
    maxResults = new BaseXSlider(0, MAXRESULTS.length - 1, val, d);
    maxResults.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { action(maxResults); }
    });
    labelResults = new BaseXLabel(" ");

    val = sliderIndex(gui.gopts.get(GUIOptions.MAXTEXT), MAXTEXT);
    maxText = new BaseXSlider(0, MAXTEXT.length - 1, val, d);
    maxText.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { action(maxText); }
    });
    labelText = new BaseXLabel(" ");

    lang = new BaseXCombo(d, LANGS[0]);
    lang.setSelectedItem(opts.get(StaticOptions.LANG));
    creds = new BaseXLabel(" ");

    add(new BaseXLabel(DATABASE_PATH + COL, true, true));
    BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 0)), pp, ppp;
    p.add(dbPath);
    p.add(dbButton);
    add(p);

    add(new BaseXLabel(REPOSITORY_PATH + COL, true, true));
    p = new BaseXBack(new TableLayout(1, 2, 8, 0));
    p.add(repoPath);
    p.add(repoButton);
    add(p);

    p = new BaseXBack(new TableLayout(2, 2, 24, 0)).border(0);
    p.add(new BaseXLabel(GUI_INTERACTIONS + COL, true, true).border(8, 0, 8, 0));
    p.add(new BaseXLabel(LIMITS + COL, true, true).border(8, 0, 8, 0));

    pp = new BaseXBack(new TableLayout(2, 1, 0, 0)).border(0);
    pp.add(mousefocus);
    pp.add(simplefd);
    p.add(pp);
    pp = new BaseXBack(new TableLayout(4, 1, 0, 0)).border(0);
    ppp = new BaseXBack(new TableLayout(1, 2, 12, 0)).border(0);
    ppp.add(maxResults);
    ppp.add(labelResults);
    pp.add(new BaseXLabel(MAX_NO_OF_HITS + COL));
    pp.add(ppp);
    ppp = new BaseXBack(new TableLayout(1, 2, 12, 0)).border(0);
    ppp.add(maxText);
    ppp.add(labelText);
    pp.add(new BaseXLabel(SIZE_TEXT_RESULTS + COL));
    pp.add(ppp);
    p.add(pp);

    add(p);

    // checkbox for simple file dialog
    add(new BaseXLabel(LANGUAGE_RESTART + COL, true, true).border(8, 0, 8, 0));
    p = new BaseXBack(new TableLayout(1, 2, 12, 0));
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
  private int sliderIndex(final int value, final int[] values) {
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
    simplefd.assign();

    // new database path: close opened database
    final StaticOptions opts = gui.context.soptions;
    if(source == dbPath || source == dbButton) {
      final String dbpath = dbPath.getText();
      if(!opts.get(StaticOptions.DBPATH).equals(dbpath) && gui.context.data() != null) {
        new Close().run(gui.context);
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
    labelText.setText(mt == Integer.MAX_VALUE ? ALL : Performance.format(mt, true));
    return true;
  }
}
