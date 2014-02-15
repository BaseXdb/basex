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
 * @author BaseX Team 2005-14, BSD License
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
  private final BaseXTextField path;
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
  private final BaseXButton button;

  /**
   * Default constructor.
   * @param d dialog reference
   */
  DialogGeneralPrefs(final BaseXDialog d) {
    border(8).setLayout(new TableLayout(8, 1));
    gui = d.gui;

    final GlobalOptions opts = gui.context.globalopts;
    final GUIOptions gopts = gui.gopts;
    path = new BaseXTextField(opts.dbpath().path(), d);

    button = new BaseXButton(BROWSE_D, d);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final IOFile dir = new BaseXFileChooser(CHOOSE_DIR, path.getText(), gui).select(Mode.DOPEN);
        if(dir != null) path.setText(dir.dirPath());
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
    lang.setSelectedItem(opts.get(GlobalOptions.LANG));
    creds = new BaseXLabel(" ");

    add(new BaseXLabel(DATABASE_PATH + COL, true, true));

    BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 0));
    p.add(path);
    p.add(button);
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
    int mh = gui.gopts.get(MainOptions.MAXHITS);
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
    for(int i = 0; i < LANGS[0].length; ++i) {
      if(LANGS[0][i].equals(lng)) return LANGS[1][i];
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
    final GlobalOptions opts = gui.context.globalopts;
    if(source == path || source == button) {
      final String dbpath = path.getText();
      if(!opts.get(GlobalOptions.DBPATH).equals(dbpath) && gui.context.data() != null) {
        new Close().run(gui.context);
        gui.notify.init();
      }
      opts.set(GlobalOptions.DBPATH, dbpath);
    }
    opts.set(GlobalOptions.LANG, lang.getSelectedItem());

    gui.gopts.set(GUIOptions.MAXHITS, HITS[limit.getValue()]);

    creds.setText(TRANSLATION + COLS + creds(lang.getSelectedItem()));
    final int mh = HITS[limit.getValue()];
    label.setText(mh == -1 ? ALL : Integer.toString(mh));
  }
}
