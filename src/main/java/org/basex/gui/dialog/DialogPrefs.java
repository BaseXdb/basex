package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.awt.event.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.io.*;

/**
 * Dialog window for changing some project's preferences.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogPrefs extends Dialog {
  /** Information on available languages. */
  private static final String[][] LANGS = Lang.parse();

  /** Directory path. */
  final BaseXTextField path;
  /** Number of hits. */
  final BaseXSlider limit;
  /** Label for number of hits. */
  final BaseXLabel label;

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
  private final BaseXCheckBox javalook;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogPrefs(final GUI main) {
    super(main, PREFERENCES);

    // create checkboxes
    final BaseXBack pp = new BaseXBack(new TableLayout(12, 1));
    pp.add(new BaseXLabel(DATABASE_PATH, true, true));

    BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 0));

    final MainProp mprop = gui.context.mprop;
    final GUIProp gprop = gui.gprop;
    path = new BaseXTextField(mprop.dbpath().path(), this);

    final BaseXButton button = new BaseXButton(BROWSE_D, this);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final IOFile file = new BaseXFileChooser(CHOOSE_DIR, path.getText(),
            gui).select(Mode.DOPEN);
        if(file != null) path.setText(file.dir());
      }
    });

    p.add(path);
    p.add(button);
    pp.add(p);
    pp.add(new BaseXLabel(GUI_INTERACTIONS, true, true).border(12, 0, 6, 0));

    // checkbox for Java look and feel
    javalook = new BaseXCheckBox(JAVA_LF, gprop.is(GUIProp.JAVALOOK), this);
    pp.add(javalook);

    // checkbox for realtime mouse focus
    focus = new BaseXCheckBox(RT_FOCUS, gprop.is(GUIProp.MOUSEFOCUS), this);
    pp.add(focus);

    // checkbox for simple file dialog
    simpfd = new BaseXCheckBox(SIMPLE_FILE_CHOOSER, gprop.is(GUIProp.SIMPLEFD), this);
    pp.add(simpfd);

    // enable only if current document contains name attributes
    names = new BaseXCheckBox(SHOW_NAME_ATTS, gprop.is(GUIProp.SHOWNAME), 6, this);
    final Data data = gui.context.data();
    names.setEnabled(data != null && data.nameID != 0);
    pp.add(names);

    // maximum number of hits to be displayed
    int mh = gui.gprop.num(Prop.MAXHITS);
    mh = mh == -1 ? 6 : Math.min(6, (int) Math.log10(Math.max(10, mh)) - 1);
    limit = new BaseXSlider(0, 6, mh, this, new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { action(limit); }
    });
    label = new BaseXLabel(" ");
    p = new BaseXBack(new TableLayout(1, 3, 16, 0));
    p.add(new BaseXLabel(MAX_NO_OF_HITS + COL));
    p.add(limit);
    p.add(label);
    pp.add(p);

    // checkbox for simple file dialog
    pp.add(new BaseXLabel(LANGUAGE_RESTART, true, true).border(16, 0, 6, 0));
    lang = new BaseXCombo(this, LANGS[0]);
    lang.setSelectedItem(mprop.get(MainProp.LANG));
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
    creds.setText(TRANSLATION + COLS + creds(lang.getSelectedItem().toString()));
    final int mh = maxHits();
    label.setText(mh == -1 ? ALL : Integer.toString(mh));
    if(cmp == names) gui.notify.layout();
  }

  @Override
  public void close() {
    final MainProp mprop = gui.context.mprop;
    mprop.set(MainProp.LANG, lang.getSelectedItem().toString());
    // new database path: close existing database
    final String dbpath = path.getText();
    if(!mprop.get(MainProp.DBPATH).equals(dbpath)) gui.execute(new Close());
    mprop.set(MainProp.DBPATH, dbpath);
    mprop.write();
    final GUIProp gprop = gui.gprop;
    gprop.set(GUIProp.MOUSEFOCUS, focus.isSelected());
    gprop.set(GUIProp.SHOWNAME, names.isSelected());
    gprop.set(GUIProp.SIMPLEFD, simpfd.isSelected());
    gprop.set(GUIProp.JAVALOOK, javalook.isSelected());
    final int mh = maxHits();
    gprop.set(GUIProp.MAXHITS, mh);
    gprop.write();
    gui.context.prop.set(Prop.MAXHITS, mh);
    dispose();
  }

  /**
   * Returns the selected maximum number of hits.
   * @return maximum number of hits
   */
  private int maxHits() {
    final int mh = limit.value();
    return mh == 6 ? -1 : (int) Math.pow(10, mh + 1);
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
