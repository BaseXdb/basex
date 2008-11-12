package org.basex.gui.dialog;

import static org.basex.Text.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import org.basex.core.Prop;
import org.basex.core.proc.List;
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
import org.basex.io.IO;
import org.basex.util.StringList;

/**
 * Dialog window for specifying the options for importing a file system.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class DialogImportFS  extends Dialog {
  /** Available databases. */
  private final StringList db = List.list();
  /** Directory path. */
  BaseXTextField path;
  /** Database name. */
  BaseXTextField dbname;
  /** Database info. */
  private final BaseXLabel info;
  /** Parsing complete filesystem. */
  private BaseXCheckBox all;
  /** Import hidden Files. */
  private BaseXCheckBox hidden;
  /** Browse button. */
  private BaseXButton button;
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

    // create panels
    final BaseXBack p1 = new BaseXBack();
    p1.setLayout(new TableLayout(7, 1));
    p1.setBorder(8, 8, 8, 8);

    p1.add(new BaseXLabel(IMPORTFSTEXT, false, true));

    BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(3, 2, 6, 0));
    
    path = new BaseXTextField(GUIProp.fspath, HELPFSPATH, this);
    path.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        action(null);
      }
    });
    BaseXLayout.setWidth(path, 240);
    p.add(path);

    button = new BaseXButton(BUTTONBROWSE, HELPBROWSE, this);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final BaseXFileChooser fc = new BaseXFileChooser(
            DIALOGFC, path.getText(), parent);
        if(fc.select(BaseXFileChooser.MODE.DIR)) {
          final IO file = fc.getFile();
          path.setText(file.path());
          dbname.setText(file.dbname());
        }
      }
    });
    p.add(button);
    
    p.add(new BaseXLabel(CREATENAME, false, true));
    p.add(new BaseXLabel(""));

    dbname = new BaseXTextField(GUIProp.importfsname, HELPFSNAME, this);
    dbname.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        action(null);
      }
    });
    BaseXLayout.setWidth(dbname, 240);
    p.add(dbname);

    all = new BaseXCheckBox(IMPORTALL, HELPFSALL, GUIProp.fsall, this);
    all.setToolTipText(IMPORTALLINFO);
    all.setBorder(new EmptyBorder(4, 4, 0, 0));
    all.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        action(null);
      }
    });
    
    p.add(all);
    p1.add(p);

    info = new BaseXLabel(" ");
    info.setBorder(20, 0, 10, 0);
    p1.add(info);

    // create checkboxes
    final BaseXBack p2 = new BaseXBack();
    p2.setLayout(new TableLayout(9, 1));
    p2.setBorder(8, 8, 8, 8);
    
    BaseXLabel label = new BaseXLabel(IMPORTFSTEXT1, false, true);
    p2.add(label);
    meta = new BaseXCheckBox(IMPORTMETA, HELPMETA, Prop.fsmeta, this);
    p2.add(meta);

    label = new BaseXLabel(IMPORTFSTEXT2, false, true);
    p2.add(label);

    p = new BaseXBack();
    p.setLayout(new BorderLayout());

    cont = new BaseXCheckBox(IMPORTCONT, HELPCONT, Prop.fscont, this);
    cont.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        action(null);
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
    BaseXLayout.setWidth(p, p2.getPreferredSize().width);
    p2.add(p);

    final JTabbedPane tabs = new JTabbedPane();
    BaseXLayout.addDefaultKeys(tabs, this);
    tabs.addTab(GENERALINFO, p1);
    tabs.addTab(METAINFO, p2);
    set(tabs, BorderLayout.CENTER);
    
    // create buttons
    buttons = BaseXLayout.okCancel(this);
    set(buttons, BorderLayout.SOUTH);
    action(null);
    finish(parent);
  }

  @Override
  public void action(final String cmd) {
    final boolean sel = !all.isSelected();
    BaseXLayout.enable(path, sel);
    BaseXLayout.enable(button, sel);
    BaseXLayout.enable(maxsize, cont.isSelected());

    final String nm = dbname.getText().trim();
    ok = nm.length() != 0;
    if(ok) GUIProp.importfsname = nm;
    
    boolean exists = all.isSelected();
    if(!exists) {
      final String p = path.getText().trim();;
      final IO file = IO.get(p);
      exists = p.length() != 0 && file.exists();
      if(exists) GUIProp.fspath = path.getText();
    }
    ok &= exists;
    
    String inf = !exists ? PATHWHICH : !ok ? DBWHICH : " ";
    ImageIcon img = null;
    if(ok) {
      ok = IO.valid(nm);
      if(!ok) {
        inf = RENAMEINVALID;
      } else if(db.contains(nm)) {
        inf = RENAMEOVER;
        img = GUI.icon("warn");
      }
    }
    
    final boolean err = inf.trim().length() != 0;
    info.setText(inf);
    info.setIcon(err ? img != null ? img : GUI.icon("error") : null);
    BaseXLayout.enableOK(buttons, ok);
  }
  
  @Override
  public void close() {
    if(!ok) return;

    super.close();
    Prop.fscont = cont.isSelected();
    Prop.fsmeta = meta.isSelected();
    Prop.fstextmax = IMPORTFSMAXSIZE[maxsize.getSelectedIndex()];
    GUIProp.fsall = all.isSelected();
    GUIProp.importfsname = dbname.getText();
    GUIProp.fspath = path.getText();
  }
}
