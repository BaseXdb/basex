package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
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
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.gui.view.View;

/**
 * Dialog window for changing some project's preferences.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class DialogPrefs extends Dialog {
  /** Browse button. */
  private BaseXButton button;
  /** Combo box. */
  private BaseXCombo lang;
  /** Directory path. */
  private BaseXTextField path;
  /** Button panel. */
  private BaseXBack buttons;
  /** Focus checkbox. */
  private BaseXCheckBox focus;
  /** Show names checkbox. */
  private BaseXCheckBox names;
  /** Simple file dialog checkbox. */
  private BaseXCheckBox simpfd;
  /** Simple file dialog checkbox. */
  private BaseXCheckBox javalook;
  /** Focus flag. */
  private boolean lf;
  /** Focus flag. */
  private boolean foc;
  /** Show names flag. */
  private boolean nam;
  /** File dialog flag. */
  private boolean fd;

  /**
   * Default Constructor.
   * @param gui reference to main frame
   */
  public DialogPrefs(final GUI gui) {
    super(gui, PREFSTITLE);
    foc = GUIProp.mousefocus;
    nam = GUIProp.shownames;
    fd = GUIProp.simplefd;
    lf = GUIProp.javalook;

    // create checkboxes
    final BaseXBack pp = new BaseXBack();
    pp.setLayout(new TableLayout(10, 1, 0, 0));
    pp.add(new BaseXLabel(DATABASEPATH, true));

    BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(1, 2, 6, 0));
    
    path = new BaseXTextField(Prop.dbpath, HELPDBPATH, this);
    BaseXLayout.setWidth(path, 250);
    p.add(path);
    
    button = new BaseXButton(BUTTONBROWSE, HELPBROWSE, this);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        chooseDir(gui);
      }
    });
    p.add(button);
    pp.add(p);

    BaseXLabel label = new BaseXLabel(PREFINTER, true);
    label.setBorder(10, 0, 8, 0);
    pp.add(label);

    // checkbox for realtime mouse focus
    javalook = new BaseXCheckBox(PREFLF, HELPLF, lf, this);
    pp.add(javalook);

    // checkbox for realtime mouse focus
    focus = new BaseXCheckBox(PREFFOCUS, HELPFOCUS, foc, this);
    pp.add(focus);

    // checkbox for simple file dialog
    simpfd = new BaseXCheckBox(SIMPLEFD, HELPSIMPLEFD, fd, this);
    pp.add(simpfd);

    // enable only if current document contains name attributes
    names = new BaseXCheckBox(PREFNAMES, HELPNAMES, nam, this);
    final Data data = GUI.context.data();
    names.setEnabled(data != null && !data.deepfs && data.nameID != 0);
    pp.add(names);

    // checkbox for simple file dialog
    pp.add(new BaseXLabel(PREFLANG, true));

    final Box box = new Box(BoxLayout.X_AXIS);
    lang = new BaseXCombo(Prop.LANGUAGES, HELPLANG, false, this);
    lang.setSelectedItem(Prop.language);
    box.add(lang);
    box.add(new BaseXLabel("   " + PREFLANGNEW));
    pp.add(box);

    set(pp, BorderLayout.CENTER);

    // create buttons
    buttons = BaseXLayout.okCancel(this);
    set(buttons, BorderLayout.SOUTH);
    
    finish(gui);
  }
  
  /**
   * Opens the directory chooser and sets the new path.
   * @param parent parent reference
   */
  final void chooseDir(final JFrame parent) {
    final BaseXFileChooser fc = new BaseXFileChooser(DIALOGFC, path.getText(),
        parent);
    if(fc.select(BaseXFileChooser.DIR)) path.setText(fc.getDir());
  }

  @Override
  public final void action(final String cmd) {
    GUIProp.mousefocus = focus.isSelected();
    GUIProp.shownames = names.isSelected();
    GUIProp.simplefd = simpfd.isSelected();
    GUIProp.javalook = javalook.isSelected();
    if(GUI.context.db()) View.notifyUpdate();
  }

  @Override
  public final void cancel() {
    super.cancel();
    GUIProp.mousefocus = foc;
    GUIProp.shownames = nam;
    GUIProp.simplefd = fd;
    GUIProp.javalook = lf;
    if(GUI.context.db()) View.notifyUpdate();
  }

  @Override
  public final void close() {
    super.close();
    Prop.dbpath = path.getText();
    Prop.language = lang.getSelectedItem().toString();
  }
}
