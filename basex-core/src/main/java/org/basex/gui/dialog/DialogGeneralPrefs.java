package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.event.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.io.*;

/**
 * General preferences.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class DialogGeneralPrefs extends BaseXBack {
  /** Main window reference. */
  private final GUI gui;

  /** Information on available languages. */
  private static final int[] HITS = {
    10, 25, 100, 250, 1000, 2500, 10000, 25000, 100000, 250000, 1000000, -1
  };

  /** Information on available languages. */
  private static final String[][] LANGS = Lang.parse();

  /** Directory path. */
  private final BaseXTextField dbPath;
  /** Repository path. */
  private final BaseXTextField repoPath;
  /** Number of hits. */
  private final BaseXSlider limit;
  /** Label for number of hits. */
  private final BaseXLabel label;

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
        if(dir != null) dbPath.setText(dir.dir());
      }
    });

    repoButton = new BaseXButton(BROWSE_D, d);
    repoButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final String path = repoPath.getText();
        final IOFile dir = new BaseXFileChooser(CHOOSE_DIR, path, gui).select(Mode.DOPEN);
        if(dir != null) repoPath.setText(dir.dir());
      }
    });

    mousefocus = new BaseXCheckBox(RT_FOCUS, GUIOptions.MOUSEFOCUS, gopts, d);
    simplefd = new BaseXCheckBox(SIMPLE_FILE_CHOOSER, GUIOptions.SIMPLEFD, gopts, d);
    final int val = hitsForSlider();
    limit = new BaseXSlider(0, HITS.length - 1, val, d);
    limit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { action(limit); }
    });
    label = new BaseXLabel(" ");
    lang = new BaseXCombo(d, LANGS[0]);
    lang.setSelectedItem(opts.get(StaticOptions.LANG));
    creds = new BaseXLabel(" ");

    add(new BaseXLabel(DATABASE_PATH + COL, true, true));
    BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 0));
    p.add(dbPath);
    p.add(dbButton);
    add(p);

    add(new BaseXLabel(REPOSITORY_PATH + COL, true, true));
    p = new BaseXBack(new TableLayout(1, 2, 8, 0));
    p.add(repoPath);
    p.add(repoButton);
    add(p);

    add(new BaseXLabel(GUI_INTERACTIONS + COL, true, true).border(8, 0, 8, 0));

    p = new BaseXBack(new TableLayout(2, 2, 40, 0));
    p.add(mousefocus);
    p.add(new BaseXLabel(MAX_NO_OF_HITS + COL));
    p.add(simplefd);
    final BaseXBack pp = new BaseXBack(new TableLayout(1, 2, 12, 0)).border(0);
    pp.add(limit);
    pp.add(label);
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
   * @return maximum number of hits
   */
  private int hitsForSlider() {
    int mh = gui.gopts.get(GUIOptions.MAXRESULTS);
    if(mh == -1) mh = Integer.MAX_VALUE;
    final int hl = HITS.length - 1;
    int h = -1;
    while(++h < hl && HITS[h] < mh);
    return h;
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
   */
  void action(final Object source) {
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

    gui.gopts.set(GUIOptions.MAXRESULTS, HITS[limit.getValue()]);

    creds.setText(TRANSLATION + COLS + creds(lang.getSelectedItem()));
    final int mh = HITS[limit.getValue()];
    label.setText(mh == -1 ? ALL : Integer.toString(mh));
  }
}
