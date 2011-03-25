package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.basex.core.Lang;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;

/**
 * Dialog window for changing some project's preferences.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DialogPrefs extends Dialog {
  /** Information on available languages. */
  private static final String[][] LANGS = Lang.parse();

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
  /** Buttons. */
  private final BaseXBack buttons;

  /** Directory path. */
  final BaseXTextField path;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogPrefs(final GUI main) {
    super(main, PREFSTITLE);

    // create checkboxes
    final BaseXBack pp = new BaseXBack(new TableLayout(11, 1));
    pp.add(new BaseXLabel(DATABASEPATH, true, true));

    BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 0));

    final Prop prop = gui.context.prop;
    final GUIProp gprop = gui.gprop;
    path = new BaseXTextField(prop.get(Prop.DBPATH), this);
    path.addKeyListener(keys);

    final BaseXButton button = new BaseXButton(BUTTONBROWSE, this);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final IO file = new BaseXFileChooser(DIALOGFC, path.getText(),
            gui).select(BaseXFileChooser.Mode.DOPEN);
        if(file != null) path.setText(file.dir());
      }
    });

    p.add(path);
    p.add(button);
    pp.add(p);
    pp.add(new BaseXLabel(PREFINTER, true, true).border(10, 0, 8, 0));

    // checkbox for realtime mouse focus
    javalook = new BaseXCheckBox(PREFLF, gprop.is(GUIProp.JAVALOOK), this);
    pp.add(javalook);

    // checkbox for realtime mouse focus
    focus = new BaseXCheckBox(PREFFOCUS, gprop.is(GUIProp.MOUSEFOCUS), this);
    pp.add(focus);

    // checkbox for simple file dialog
    simpfd = new BaseXCheckBox(SIMPLEFILE, gprop.is(GUIProp.SIMPLEFD), this);
    pp.add(simpfd);

    // enable only if current document contains name attributes
    names = new BaseXCheckBox(PREFNAME, gprop.is(GUIProp.SHOWNAME), 12, this);
    final Data data = gui.context.data;
    names.setEnabled(data != null && data.nameID != 0);
    pp.add(names);

    // checkbox for simple file dialog
    pp.add(new BaseXLabel(PREFLANG, true, true));

    p = new BaseXBack(new TableLayout(1, 2, 12, 0));

    lang = new BaseXCombo(this, LANGS[0]);
    lang.setSelectedItem(prop.get(Prop.LANG));

    p.add(lang);
    creds = new BaseXLabel(" ");
    p.add(creds);
    creds.setText(TRANSLATION + creds(lang.getSelectedItem().toString()));

    pp.add(p);

    // create buttons
    buttons = okCancel(this);
    set(buttons, BorderLayout.SOUTH);

    set(pp, BorderLayout.CENTER);
    finish(null);
  }

  @Override
  public void action(final Object cmp) {
    creds.setText(TRANSLATION + creds(lang.getSelectedItem().toString()));
    gui.notify.layout();
  }

  @Override
  public void close() {
    final Prop prop = gui.context.prop;
    prop.set(Prop.DBPATH, path.getText());
    prop.set(Prop.LANG, lang.getSelectedItem().toString());
    prop.write();
    final GUIProp gprop = gui.gprop;
    gprop.set(GUIProp.MOUSEFOCUS, focus.isSelected());
    gprop.set(GUIProp.SHOWNAME, names.isSelected());
    gprop.set(GUIProp.SIMPLEFD, simpfd.isSelected());
    gprop.set(GUIProp.JAVALOOK, javalook.isSelected());
    gprop.write();
    dispose();
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
