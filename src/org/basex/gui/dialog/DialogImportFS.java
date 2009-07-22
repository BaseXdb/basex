package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.ImageIcon;
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
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;
import org.basex.util.StringList;

/**
 * Dialog window for specifying the options for importing a file system.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogImportFS extends Dialog {
  /** Available databases. */
  private final StringList db = List.list();
  /** Directory path. */
  BaseXTextField path;
  /** Database name. */
  BaseXTextField dbname;
  /** Backing path. */
  BaseXTextField backing;
  /** Mountpoint path. */
  BaseXTextField mountpoint;

  /** Database info. */
  private final BaseXLabel info;
  /** Parsing complete filesystem. */
  private final BaseXCheckBox all;
  /** Browse button. */
  private final BaseXButton button;
  /** ID3 parsing. */
  private final BaseXCheckBox meta;
  /** Context inclusion. */
  private final BaseXCheckBox cont;
  /** Button panel. */
  private final BaseXBack buttons;
  /** ComboBox. */
  private final BaseXCombo maxsize;

  /**
   * Default Constructor.
   * @param main reference to the main window
   */
  public DialogImportFS(final GUI main) {
    super(main, IMPORTFSTITLE);

    // create panels
    final BaseXBack p1 = new BaseXBack();
    p1.setLayout(new TableLayout(3, 1));
    p1.setBorder(8, 8, 8, 8);

    p1.add(new BaseXLabel(IMPORTFSTEXT, false, true));

    BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(7, 2, 6, 0));

    path = new BaseXTextField(GUIProp.guifsimportpath, HELPFSPATH, this);
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
        final IO file = new BaseXFileChooser(DIALOGFC, path.getText(),
            main).select(BaseXFileChooser.Mode.DOPEN);
        if(file != null) {
          path.setText(file.path());
          dbname.setText(file.dbname());
        }
      }
    });
    p.add(button);

    p.add(new BaseXLabel(CREATENAME, false, true));
    p.add(new BaseXLabel(""));

    dbname = new BaseXTextField(GUIProp.guifsdbname, HELPFSNAME, this);
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

    backing = new BaseXTextField(GUIProp.guibackingroot, HELPFSBACKING, this);
    backing.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        action(null);
      }
    });
    BaseXLayout.setWidth(backing, 240);

    mountpoint = new BaseXTextField(GUIProp.guimountpoint, HELPFSMOUNT, this);
    mountpoint.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        action(null);
      }
    });
    BaseXLayout.setWidth(mountpoint, 240);

    if(Prop.fuse) {
      p.add(new BaseXLabel("Backing store:", false, true));
      p.add(new BaseXLabel(""));
      p.add(backing);
      p.add(new BaseXLabel(""));
      p.add(new BaseXLabel("DeepFS mount point:", false, true));
      p.add(new BaseXLabel(""));
      p.add(mountpoint);
      p.add(new BaseXLabel(""));
    }

    p1.add(p);

    info = new BaseXLabel(" ");
    info.setBorder(20, 0, 10, 0);
    p1.add(info);

    // create checkboxes
    final BaseXBack p2 = new BaseXBack();
    p2.setLayout(new TableLayout(4, 1));
    p2.setBorder(8, 8, 8, 8);

    BaseXLabel label = new BaseXLabel(IMPORTFSTEXT1, false, true);
    p2.add(label);
    meta = new BaseXCheckBox(IMPORTMETA, HELPMETA, Prop.fsmeta, 12, this);
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

    maxsize = new BaseXCombo(IMPORTFSMAX, HELPFSMAX, this);
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

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(GENERALINFO, p1);
    tabs.addTab(METAINFO, p2);
    set(tabs, BorderLayout.CENTER);

    // create buttons
    buttons = BaseXLayout.okCancel(this);

    set(buttons, BorderLayout.SOUTH);

    action(null);
    finish(null);
  }

  @Override
  public void action(final String cmd) {
    final boolean sel = !all.isSelected();
    BaseXLayout.enable(path, sel);
    BaseXLayout.enable(button, sel);
    BaseXLayout.enable(maxsize, cont.isSelected());

    boolean cAll; // import all is choosen?
    boolean cNam; // dbname given?
    boolean cBac = true; // backing store is existent directory?
    boolean cMou = true; // mount point is existent directory?

    final String nm = dbname.getText().trim();
    cNam = nm.length() != 0;
    if(cNam) GUIProp.guifsdbname = nm;
    ok = cNam;

    cAll = all.isSelected();
    if(cAll) {
      GUIProp.guifsimportpath = path.getText();
    }
    if(!cAll && cNam) {
      final String p = path.getText().trim();
      final IO file = IO.get(p);
      cAll = p.length() != 0 && file.exists();
    }
    ok &= cAll;

    if(Prop.fuse) {
      cBac = new File(backing.getText().trim()).isDirectory();
      ok &= cBac;
      cMou = new File(mountpoint.getText().trim()).isDirectory();
      ok &= cMou;
    }

    String inf = " ";

    if(!ok) {
      if(!cMou) inf = MOUNTWHICH;
      if(!cBac) inf = BACKINGWHICH;
      if(!cAll) inf = PATHWHICH;
      if(!cNam) inf = DBWHICH;
    }


    ImageIcon img = null;
    if(ok) {
      ok = IO.valid(nm);
      if(!ok) {
        inf = RENAMEINVALID;
      } else if(db.contains(nm)) {
        inf = Prop.fuse ? RENAMEOVERBACKING : RENAMEOVER;
        img = GUI.icon("warn");
      }
    }

    final boolean err = inf.trim().length() != 0;
    info.setText(inf);
    info.setIcon(err ? img != null ? img : GUI.icon("error") : null);
    BaseXLayout.enableOK(buttons, BUTTONOK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;

    Prop.fscont = cont.isSelected();
    Prop.fsmeta = meta.isSelected();
    Prop.fstextmax = IMPORTFSMAXSIZE[maxsize.getSelectedIndex()];
    GUIProp.fsall = all.isSelected();
    GUIProp.guifsimportpath = path.getText();
    GUIProp.guifsdbname = dbname.getText();
    if(Prop.fuse) {
      GUIProp.guimountpoint = mountpoint.getText().trim();
      GUIProp.guibackingroot = backing.getText().trim();
    }

    super.close();
  }
}
