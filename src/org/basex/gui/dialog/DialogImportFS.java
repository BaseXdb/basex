package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.border.EmptyBorder;
import org.basex.core.Prop;
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

/**
 * Dialog window for specifying the options for importing a file system.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogImportFS  extends Dialog {
  /** Directory path. */
  protected BaseXTextField path;
  /** Parsing complete filesystem. */
  private BaseXCheckBox all;
  /** Browse button. */
  private BaseXButton button;
  /** Database name. */
  private BaseXTextField database;
  /** ID3 parsing. */
  private BaseXCheckBox meta;
  /** Context inclusion. */
  private BaseXCheckBox cont;
  /** Button panel. */
  private BaseXBack buttons;
  /** ComboBox. */
  private BaseXCombo maxsize;

  /**
   * Default Constructor.
   * @param parent parent frame
   */
  public DialogImportFS(final JFrame parent) {
    super(parent, IMPORTFSTITLE);

    // create checkboxes
    final BaseXBack pp = new BaseXBack();
    pp.setLayout(new TableLayout(10, 1, 0, 0));
    pp.add(new BaseXLabel(IMPORTFSTEXT, true));

    BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(2, 3, 6, 0));
    
    p.add(new BaseXLabel(IMPORTDIR + "   "));
    path = new BaseXTextField(GUIProp.fspath, HELPFSPATH, this);
    path.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        check();
      }
    });
    p.add(path);
    
    button = new BaseXButton(BUTTONBROWSE, HELPBROWSE, this);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final BaseXFileChooser fc = new BaseXFileChooser(
            DIALOGFC, path.getText(), parent);
        if(fc.select(BaseXFileChooser.MODE.DIR)) path.setText(fc.getDir());
      }
    });
    p.add(button);
    
    p.add(new BaseXLabel(IMPORTSAVE + "   "));
    database = new BaseXTextField(GUIProp.importfsname, HELPFSNAME, this);
    database.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        check();
      }
    });
    p.add(database);

    all = new BaseXCheckBox(IMPORTALL, HELPFSALL, GUIProp.fsall, this);
    all.setToolTipText(IMPORTALLINFO);
    all.setBorder(new EmptyBorder(4, 4, 0, 0));
    all.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        check();
      }
    });
    p.add(all);
    pp.add(p);

    BaseXLabel label = new BaseXLabel(IMPORTFSTEXT1, true);
    label.setBorder(15, 0, 8, 0);
    pp.add(label);
    meta = new BaseXCheckBox(IMPORTMETA, HELPMETA, Prop.fsmeta, this);
    pp.add(meta);

    label = new BaseXLabel(IMPORTFSTEXT2, true);
    label.setBorder(10, 0, 8, 0);
    pp.add(label);

    p = new BaseXBack();
    p.setLayout(new BorderLayout());

    cont = new BaseXCheckBox(IMPORTCONT, HELPCONT, Prop.fscont, this);
    cont.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        check();
      }
    });
    p.add(cont, BorderLayout.WEST);

    maxsize = new BaseXCombo(IMPORTFSMAX, HELPFSMAX, false, this);
    maxsize.setToolTipText(IMPORTFSMAXINFO);
    final int m = Prop.fstextmax;
    int i = -1;
    while(++i < IMPORTFSMAXSIZE.length - 1) {
      if(IMPORTFSMAXSIZE[i] == m) break;
    }
    maxsize.setSelectedIndex(i);

    p.add(maxsize, BorderLayout.EAST);
    BaseXLayout.setWidth(p, pp.getPreferredSize().width);
    pp.add(p);

    set(pp, BorderLayout.CENTER);

    // create buttons
    buttons = BaseXLayout.okCancel(this);
    set(buttons, BorderLayout.SOUTH);

    check();
    finish(parent);
  }

  /**
   * Checks data, disables/enables the OK button and returns validity.
   * @return true if data is valid
   */
  boolean check() {
    final boolean sel = !all.isSelected();
    BaseXLayout.enable(path, sel);
    BaseXLayout.enable(button, sel);
    BaseXLayout.enable(maxsize, cont.isSelected());

    final boolean valid = database.getText().length() != 0 &&
      (all.isSelected() || path.getText().length() > 0);
    BaseXLayout.enableOK(buttons, valid);
    return valid;
  }

  @Override
  public void cancel() {
    super.cancel();
  }

  @Override
  public void close() {
    if(!check()) return;

    super.close();
    Prop.fscont = cont.isSelected();
    Prop.fsmeta = meta.isSelected();
    Prop.fstextmax = IMPORTFSMAXSIZE[maxsize.getSelectedIndex()];
    GUIProp.fsall = all.isSelected();
    GUIProp.importfsname = database.getText();
    GUIProp.fspath = path.getText();
  }
}
