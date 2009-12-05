package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogPrefs extends Dialog {
  /** Information on available languages. */
  private static final String[][] LANGS = Lang.parse();

  /** Language label. */
  BaseXLabel creds;
  /** Language Combo Box. */
  BaseXCombo lang;
  /** Directory path. */
  BaseXTextField path;

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

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogPrefs(final GUI main) {
    super(main, PREFSTITLE);

    // create checkboxes
    final BaseXBack pp = new BaseXBack();
    pp.setLayout(new TableLayout(11, 1, 0, 0));
    pp.add(new BaseXLabel(DATABASEPATH, true, true));

    BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(1, 2, 6, 0));

    final Prop prop = gui.context.prop;
    final GUIProp gprop = gui.prop;
    path = new BaseXTextField(prop.get(Prop.DBPATH), this);
    path.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        action(null);
      }
    });

    final BaseXButton button = new BaseXButton(BUTTONBROWSE, this);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final IO file = new BaseXFileChooser(DIALOGFC, path.getText(),
            gui).select(BaseXFileChooser.Mode.DOPEN);
        if(file != null) path.setText(file.getDir());
      }
    });

    p.add(path);
    p.add(button);
    pp.add(p);

    BaseXLabel label = new BaseXLabel(PREFINTER, true, true);
    label.setBorder(10, 0, 8, 0);
    pp.add(label);

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
    names.setEnabled(data != null && data.fs == null && data.nameID != 0);
    pp.add(names);

    // checkbox for simple file dialog
    label = new BaseXLabel(PREFLANG, true, true);
    pp.add(label);

    p = new BaseXBack();
    p.setLayout(new TableLayout(1, 2, 12, 0));

    lang = new BaseXCombo(LANGS[0], this);
    lang.setSelectedItem(prop.get(Prop.LANGUAGE));

    p.add(lang);
    creds = new BaseXLabel("");
    p.add(creds);

    pp.add(p);

    // create buttons
    buttons = okCancel(this);
    set(buttons, BorderLayout.SOUTH);

    set(pp, BorderLayout.CENTER);
    finish(null);
  }

  @Override
  public void action(final Object cmp) {
    creds.setText("Translated by " + creds(lang.getSelectedItem().toString()));
    gui.notify.layout();
  }

  @Override
  public void close() {
    final Prop prop = gui.context.prop;
    prop.set(Prop.DBPATH, path.getText());
    prop.set(Prop.LANGUAGE, lang.getSelectedItem().toString());
    final GUIProp gprop = gui.prop;
    gprop.set(GUIProp.MOUSEFOCUS, focus.isSelected());
    gprop.set(GUIProp.SHOWNAME, names.isSelected());
    gprop.set(GUIProp.SIMPLEFD, simpfd.isSelected());
    gprop.set(GUIProp.JAVALOOK, javalook.isSelected());
    prop.write();
    gprop.write();
    dispose();
  }

  /**
   * Returns the translation credits for the specified language.
   * @param lng language
   * @return credits
   */
  static String creds(final String lng) {
    for(int i = 0; i < LANGS[0].length; i++) {
      if(lng.equals(LANGS[0][i])) return LANGS[1][i];
    }
    return "";
  }
}
