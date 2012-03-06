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
  private static final int[] HITS = {
    10, 25, 100, 250, 1000, 2500, 10000, 25000, 100000, 250000, 1000000, -1
  };

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
  /** Old value for show names flag. */
  private final boolean oldShowNames;

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
    final boolean sn = gprop.is(GUIProp.SHOWNAME);
    names = new BaseXCheckBox(SHOW_NAME_ATTS, sn, 6, this);
    final Data data = gui.context.data();
    names.setEnabled(data != null && data.nameID != 0);
    oldShowNames = sn;
    pp.add(names);

    // maximum number of hits to be displayed
    final int mh = hitsForSlider();
    limit = new BaseXSlider(0, HITS.length - 1, mh, this, new ActionListener() {
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
    if(cmp == names) {
      gui.gprop.set(GUIProp.SHOWNAME, names.isSelected());
      gui.notify.layout();
    } else if(cmp == label) {
      final int mh = hitsAsProperty();
      label.setText(mh == -1 ? ALL : Integer.toString(mh));
    }
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

    final int mh = hitsAsProperty();
    gui.context.prop.set(Prop.MAXHITS, mh);

    final GUIProp gprop = gui.gprop;
    gprop.set(GUIProp.MOUSEFOCUS, focus.isSelected());
    gprop.set(GUIProp.SIMPLEFD, simpfd.isSelected());
    gprop.set(GUIProp.JAVALOOK, javalook.isSelected());
    gprop.set(GUIProp.MAXHITS, mh);
    gprop.write();
    dispose();
  }

  @Override
  public void cancel() {
    final boolean sn = gui.gprop.is(GUIProp.SHOWNAME);
    gui.gprop.set(GUIProp.SHOWNAME, oldShowNames);
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
    int mh = gui.gprop.num(Prop.MAXHITS);
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
