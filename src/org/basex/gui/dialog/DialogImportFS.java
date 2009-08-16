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
import org.basex.util.Token;

/**
 * Dialog window for specifying the options for importing a file system.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogImportFS extends Dialog {
  /** Available databases. */
  private final StringList db;
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
  /** Backing path. */
  private final BaseXTextField backing;
  /** Mountpoint path. */
  private final BaseXTextField mountpoint;

  /** Path summary flag. */
  private final BaseXCheckBox pathindex;
  /** Text index flag. */
  private final BaseXCheckBox txtindex;
  /** Attribute value index flag. */
  private final BaseXCheckBox atvindex;
  /** Fulltext index flag. */
  private final BaseXCheckBox ftxindex;
  /** Full-text indexing. */
  private final BaseXCheckBox[] ft = new BaseXCheckBox[4];

  /** Directory path. */
  final BaseXTextField path;
  /** Database name. */
  final BaseXTextField dbname;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogImportFS(final GUI main) {
    super(main, IMPORTFSTITLE);
    db = List.list(main.context);

    // create panels
    final BaseXBack p1 = new BaseXBack();
    p1.setLayout(new TableLayout(3, 1));
    p1.setBorder(8, 8, 8, 8);

    p1.add(new BaseXLabel(IMPORTFSTEXT, false, true));

    BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(7, 2, 6, 0));

    final Prop prop = gui.context.prop;
    final GUIProp gprop = gui.prop;
    path = new BaseXTextField(gprop.get(GUIProp.FSIMPORTPATH),
        HELPFSPATH, this);
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

    dbname = new BaseXTextField(gprop.get(GUIProp.FSDBNAME), HELPFSNAME, this);
    dbname.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        action(null);
      }
    });
    BaseXLayout.setWidth(dbname, 240);
    p.add(dbname);

    all = new BaseXCheckBox(IMPORTALL, HELPFSALL,
        gprop.is(GUIProp.FSALL), this);
    all.setToolTipText(IMPORTALLINFO);
    all.setBorder(new EmptyBorder(4, 4, 0, 0));
    all.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        action(null);
      }
    });
    p.add(all);

    backing = new BaseXTextField(gprop.get(GUIProp.FSBACKING),
        HELPFSBACKING, this);
    backing.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        action(null);
      }
    });
    BaseXLayout.setWidth(backing, 240);

    mountpoint = new BaseXTextField(gprop.get(GUIProp.FSMOUNT),
        HELPFSMOUNT, this);
    mountpoint.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) {
        action(null);
      }
    });
    BaseXLayout.setWidth(mountpoint, 240);

    if(prop.is(Prop.FUSE)) {
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
    meta = new BaseXCheckBox(IMPORTMETA, HELPMETA, prop.is(Prop.FSMETA),
        12, this);
    p2.add(meta);

    label = new BaseXLabel(IMPORTFSTEXT2, false, true);
    p2.add(label);

    p = new BaseXBack();
    p.setLayout(new BorderLayout());

    cont = new BaseXCheckBox(IMPORTCONT, HELPCONT, prop.is(Prop.FSCONT), this);
    cont.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        action(null);
      }
    });
    p.add(cont, BorderLayout.WEST);

    maxsize = new BaseXCombo(IMPORTFSMAX, HELPFSMAX, this);
    maxsize.setToolTipText(IMPORTFSMAXINFO);

    final int m = prop.num(Prop.FSTEXTMAX);
    int i = -1;
    while(++i < IMPORTFSMAXSIZE.length - 1) {
      if(IMPORTFSMAXSIZE[i] == m) break;
    }
    maxsize.setSelectedIndex(i);

    p.add(maxsize, BorderLayout.EAST);
    BaseXLayout.setWidth(p, p2.getPreferredSize().width);
    p2.add(p);

 // create checkboxes
    final BaseXBack p3 = new BaseXBack();
    p3.setLayout(new TableLayout(6, 1, 0, 0));
    p3.setBorder(8, 8, 8, 8);

    pathindex = new BaseXCheckBox(INFOPATHINDEX, Token.token(PATHINDEXINFO),
        prop.is(Prop.PATHINDEX), 0, this);
    p3.add(pathindex);
    p3.add(new BaseXLabel(PATHINDEXINFO, true, false));

    txtindex = new BaseXCheckBox(INFOTEXTINDEX, Token.token(TXTINDEXINFO),
        prop.is(Prop.TEXTINDEX), 0, this);
    p3.add(txtindex);
    p3.add(new BaseXLabel(TXTINDEXINFO, true, false));

    atvindex = new BaseXCheckBox(INFOATTRINDEX, Token.token(ATTINDEXINFO),
        prop.is(Prop.ATTRINDEX), 0, this);
    p3.add(atvindex);
    p3.add(new BaseXLabel(ATTINDEXINFO, true, false));

    // create checkboxes
    final BaseXBack p4 = new BaseXBack();
    p4.setLayout(new TableLayout(10, 1, 0, 0));
    p4.setBorder(8, 8, 8, 8);

    ftxindex = new BaseXCheckBox(INFOFTINDEX, Token.token(FTINDEXINFO),
        prop.is(Prop.FTINDEX), 0, this);
    p4.add(ftxindex);
    p4.add(new BaseXLabel(FTINDEXINFO, true, false));

    final String[] cb = { CREATEFZ, CREATESTEM, CREATECS, CREATEDC };
    final String[] desc = { FZINDEXINFO, FTSTEMINFO, FTCSINFO, FTDCINFO };
    final boolean[] val = { prop.is(Prop.FTFUZZY), prop.is(Prop.FTST),
        prop.is(Prop.FTCS), prop.is(Prop.FTDC)
    };
    for(int f = 0; f < ft.length; f++) {
      ft[f] = new BaseXCheckBox(cb[f], Token.token(desc[f]), val[f], this);
      p4.add(ft[f]);
    }

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(GENERALINFO, p1);
    tabs.addTab(METAINFO, p2);
    tabs.addTab(INDEXINFO, p3);
    tabs.addTab(FTINFO, p4);
    set(tabs, BorderLayout.CENTER);

    // create buttons
    buttons = okCancel(this);

    set(buttons, BorderLayout.SOUTH);

    action(null);
    finish(null);
  }

  @Override
  public void action(final String cmd) {
    final boolean ftx = ftxindex.isSelected();
    for(final BaseXCheckBox f : ft) f.setEnabled(ftx);

    final boolean sel = !all.isSelected();
    BaseXLayout.enable(path, sel);
    BaseXLayout.enable(button, sel);
    BaseXLayout.enable(maxsize, cont.isSelected());

    boolean cAll; // import all is chosen?
    boolean cNam; // dbname given?
    boolean cBac = true; // backing store is existent directory?
    boolean cMou = true; // mount point is existent directory?

    final Prop prop = gui.context.prop;
    final GUIProp gprop = gui.prop;
    final String nm = dbname.getText().trim();
    cNam = nm.length() != 0;
    if(cNam) gprop.set(GUIProp.FSDBNAME, nm);
    ok = cNam;

    cAll = all.isSelected();
    if(cAll) gprop.set(GUIProp.FSIMPORTPATH, path.getText());

    if(!cAll && cNam) {
      final String p = path.getText().trim();
      final IO file = IO.get(p);
      cAll = p.length() != 0 && file.exists();
    }
    ok &= cAll;

    if(prop.is(Prop.FUSE)) {
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
        inf = prop.is(Prop.FUSE) ? RENAMEOVERBACKING : RENAMEOVER;
        img = BaseXLayout.icon("warn");
      }
    }

    final boolean err = inf.trim().length() != 0;
    info.setText(inf);
    info.setIcon(err ? img != null ? img : BaseXLayout.icon("error") : null);
    enableOK(buttons, BUTTONOK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;

    final Prop prop = gui.context.prop;
    prop.set(Prop.FSCONT, cont.isSelected());
    prop.set(Prop.FSMETA, meta.isSelected());
    prop.set(Prop.FSTEXTMAX, IMPORTFSMAXSIZE[maxsize.getSelectedIndex()]);
    prop.set(Prop.PATHINDEX, pathindex.isSelected());
    prop.set(Prop.TEXTINDEX, txtindex.isSelected());
    prop.set(Prop.ATTRINDEX, atvindex.isSelected());
    prop.set(Prop.FTINDEX, ftxindex.isSelected());
    prop.set(Prop.FTFUZZY, ft[0].isSelected());
    prop.set(Prop.FTST, ft[1].isSelected());
    prop.set(Prop.FTCS, ft[2].isSelected());
    prop.set(Prop.FTDC, ft[3].isSelected());

    final GUIProp gprop = gui.prop;
    gprop.set(GUIProp.FSALL, all.isSelected());
    gprop.set(GUIProp.FSIMPORTPATH, path.getText());
    gprop.set(GUIProp.FSDBNAME, dbname.getText());
    if(prop.is(Prop.FUSE)) {
      gprop.set(GUIProp.FSMOUNT, mountpoint.getText().trim());
      gprop.set(GUIProp.FSBACKING, backing.getText().trim());
    }
    gprop.set(GUIProp.CREATEPATH, path.getText());

    super.close();
  }
}
