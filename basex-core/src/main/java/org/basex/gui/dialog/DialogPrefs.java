package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.UIManager.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.gui.view.*;
import org.basex.io.*;
import org.basex.util.list.*;

/**
 * Dialog window for changing some project's preferences.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class DialogPrefs extends BaseXDialog {
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
  private final BaseXCheckBox focus;
  /** Show names checkbox. */
  private final BaseXCheckBox names;
  /** Simple file dialog checkbox. */
  private final BaseXCheckBox simpfd;
  /** Simple file dialog checkbox. */
  private final BaseXCombo lookfeel;
  /** Old value for show names flag. */
  private final boolean oldShowNames;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogPrefs(final GUI main) {
    super(main, PREFERENCES);

    // create checkboxes
    final BaseXBack pp = new BaseXBack(new TableLayout(13, 1));
    pp.add(new BaseXLabel(DATABASE_PATH + COL, true, true));

    BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 0));

    final GlobalOptions opts = gui.context.globalopts;
    final GUIOptions gopts = gui.gopts;
    path = new BaseXTextField(opts.dbpath().path(), this);

    final BaseXButton button = new BaseXButton(BROWSE_D, this);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final IOFile file = new BaseXFileChooser(CHOOSE_DIR, path.getText(), gui).
            select(Mode.DOPEN);
        if(file != null) path.setText(file.dirPath());
      }
    });

    p.add(path);
    p.add(button);
    pp.add(p);
    pp.add(new BaseXLabel(GUI_INTERACTIONS + COL, true, true).border(12, 0, 6, 0));

    // checkbox for realtime mouse focus
    focus = new BaseXCheckBox(RT_FOCUS, GUIOptions.MOUSEFOCUS, gopts, this);
    pp.add(focus);

    // checkbox for simple file dialog
    simpfd = new BaseXCheckBox(SIMPLE_FILE_CHOOSER, GUIOptions.SIMPLEFD, gopts, this);
    pp.add(simpfd);

    // enable only if current document contains name attributes
    final Data data = gui.context.data();
    names = new BaseXCheckBox(SHOW_NAME_ATTS, GUIOptions.SHOWNAME, gopts, this);
    names.setEnabled(data != null && ViewData.nameID(data) != 0);
    oldShowNames = names.isSelected();
    pp.add(names);

    // maximum number of hits to be displayed
    final int mh = hitsForSlider();
    limit = new BaseXSlider(0, HITS.length - 1, mh, this, new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { action(limit); }
    });
    label = new BaseXLabel(" ");

    p = new BaseXBack(new TableLayout(1, 3, 12, 0)).border(8, 0, 0, 0);
    p.add(new BaseXLabel(MAX_NO_OF_HITS + COL));
    p.add(limit);
    p.add(label);
    pp.add(p);

    pp.add(new BaseXLabel(JAVA_LF + COL).border(12, 0, 6, 0));
    final StringList lafs = new StringList("(default)");
    for(final LookAndFeelInfo lafi : UIManager.getInstalledLookAndFeels()) lafs.add(lafi.getName());
    lookfeel = new BaseXCombo(this, lafs.toArray());
    final String laf = gopts.get(GUIOptions.LOOKANDFEEL);
    if(laf.isEmpty()) {
      lookfeel.setSelectedIndex(0);
    } else {
      lookfeel.setSelectedItem(laf);
    }
    pp.add(lookfeel);

    // checkbox for simple file dialog
    pp.add(new BaseXLabel(LANGUAGE_RESTART + COL, true, true).border(16, 0, 6, 0));
    lang = new BaseXCombo(this, LANGS[0]);
    lang.setSelectedItem(opts.get(GlobalOptions.LANG));
    creds = new BaseXLabel(" ");
    p = new BaseXBack(new TableLayout(1, 2, 12, 0));
    p.add(lang);
    p.add(creds);
    pp.add(p);

    set(pp, BorderLayout.CENTER);
    set(okCancel(), BorderLayout.SOUTH);
    action(null);
    finish(null);
  }

  @Override
  public void action(final Object cmp) {
    creds.setText(TRANSLATION + COLS + creds(lang.getSelectedItem()));
    if(cmp == names) {
      gui.gopts.set(GUIOptions.SHOWNAME, names.isSelected());
      gui.notify.layout();
    }
    final int mh = hitsAsProperty();
    label.setText(mh == -1 ? ALL : Integer.toString(mh));
  }

  @Override
  public void close() {
    final GlobalOptions opts = gui.context.globalopts;
    opts.set(GlobalOptions.LANG, lang.getSelectedItem());

    // new database path: close existing database
    final String dbpath = path.getText();
    if(!opts.get(GlobalOptions.DBPATH).equals(dbpath)) gui.execute(new Close());
    opts.set(GlobalOptions.DBPATH, dbpath);
    opts.write();

    final int mh = hitsAsProperty();
    gui.context.options.set(MainOptions.MAXHITS, mh);

    final GUIOptions gopts = gui.gopts;
    gopts.set(GUIOptions.MOUSEFOCUS, focus.isSelected());
    gopts.set(GUIOptions.SIMPLEFD, simpfd.isSelected());
    gopts.set(GUIOptions.MAXHITS, mh);
    gopts.set(GUIOptions.LOOKANDFEEL, lookfeel.getSelectedIndex() == 0 ? "" :
      lookfeel.getSelectedItem());
    gopts.write();
    dispose();
  }

  @Override
  public void cancel() {
    final boolean sn = gui.gopts.get(GUIOptions.SHOWNAME);
    gui.gopts.set(GUIOptions.SHOWNAME, oldShowNames);
    if(sn != oldShowNames) gui.notify.layout();
    super.cancel();
  }

  /**
   * Returns the selected maximum number of hits as property value.
   * @return maximum number of hits
   */
  private int hitsAsProperty() {
    return HITS[limit.value()];
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
}
