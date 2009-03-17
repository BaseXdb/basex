package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JTabbedPane;
import org.basex.core.Prop;
import org.basex.core.proc.List;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXLayout;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * Dialog window for specifying options for creating a new database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogCreate extends Dialog {
  /** Database Input. */
  private final BaseXTextField path;
  /** Database Input. */
  private final BaseXTextField filter;
  /** Database name. */
  private final BaseXTextField dbname;
  /** Database info. */
  private final BaseXLabel info;
  
  /** Internal XML parsing. */
  private final BaseXCheckBox intparse;
  /** Whitespace chopping. */
  private final BaseXCheckBox chop;
  /** Entities mode. */
  private final BaseXCheckBox entities;
  /** DTD mode. */
  private final BaseXCheckBox dtd;
  /** Indexing mode. */
  private final BaseXCheckBox txtindex;
  /** Indexing mode. */
  private final BaseXCheckBox atvindex;
  /** Word Indexing mode. */
  private final BaseXCheckBox ftxindex;
  /** Fulltext indexing. */
  private final BaseXCheckBox[] ft = new BaseXCheckBox[4];
  /** Buttons. */
  private final BaseXBack buttons;
  /** Available databases. */
  private final StringList db = List.list();

  /**
   * Default Constructor.
   * @param main reference to the main window
   */
  public DialogCreate(final GUI main) {
    super(main, CREATEADVTITLE);

    // create panels
    final BaseXBack p1 = new BaseXBack();
    p1.setLayout(new TableLayout(7, 1));
    p1.setBorder(8, 8, 8, 8);
 
    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(2, 3, 6, 0));
    p.add(new BaseXLabel(CREATETITLE + ":", false, true));
    p.add(new BaseXLabel(CREATEFILTER + ":", false, true));
    p.add(new BaseXLabel(""));

    path = new BaseXTextField(GUIProp.createpath, null, this);
    path.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) { action(null); }
    });
    BaseXLayout.setWidth(path, 240);
    p.add(path);

    filter = new BaseXTextField(Prop.createfilter, null, this);
    path.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) { action(null); }
    });
    BaseXLayout.setWidth(filter, 54);
    p.add(filter);

    final BaseXButton button = new BaseXButton(BUTTONBROWSE,
        HELPBROWSE, this);
    button.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) { choose(); }
    });
    p.add(button);
    p1.add(p);
    
    final BaseXLabel l = new BaseXLabel(CREATENAME, false, true);
    l.setBorder(0, 0, 0, 0);
    p1.add(l);
    dbname = new BaseXTextField(null, this);
    dbname.setText(IO.get(GUIProp.createpath).dbname());
    dbname.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) { action(null); }
    });
    BaseXLayout.setWidth(dbname, 300);
    p1.add(dbname);

    info = new BaseXLabel(" ");
    info.setBorder(40, 0, 0, 0);
    p1.add(info);

    // create checkboxes
    final BaseXBack p2 = new BaseXBack();
    p2.setLayout(new TableLayout(9, 1));
    p2.setBorder(8, 8, 8, 8);

    intparse = new BaseXCheckBox(CREATEINTPARSE, Token.token(INTPARSEINFO),
        Prop.intparse, 0, this);
    p2.add(intparse);
    p2.add(new BaseXLabel(INTPARSEINFO, true));

    entities = new BaseXCheckBox(CREATEENTITIES, Token.token(ENTITIESINFO),
        Prop.entity, 0, this);
    p2.add(entities);

    dtd = new BaseXCheckBox(CREATEDTD, Token.token(DTDINFO),
        Prop.entity, 12, this);
    p2.add(dtd);

    chop = new BaseXCheckBox(CREATECHOP, Token.token(CHOPPINGINFO),
        Prop.chop, 0, this);
    p2.add(chop);
    p2.add(new BaseXLabel(CHOPPINGINFO, true));

    // create checkboxes
    final BaseXBack p3 = new BaseXBack();
    p3.setLayout(new TableLayout(10, 1, 0, 0));
    p3.setBorder(8, 8, 8, 8);

    txtindex = new BaseXCheckBox(INFOTEXTINDEX, Token.token(TXTINDEXINFO),
        Prop.textindex, 0, this);
    p3.add(txtindex);
    p3.add(new BaseXLabel(TXTINDEXINFO, true));

    atvindex = new BaseXCheckBox(INFOATTRINDEX, Token.token(ATTINDEXINFO),
        Prop.attrindex, 0, this);
    p3.add(atvindex);
    p3.add(new BaseXLabel(ATTINDEXINFO, true));

    // create checkboxes
    final BaseXBack p4 = new BaseXBack();
    p4.setLayout(new TableLayout(10, 1, 0, 0));
    p4.setBorder(8, 8, 8, 8);

    ftxindex = new BaseXCheckBox(INFOFTINDEX, Token.token(FTINDEXINFO),
        Prop.ftindex, 0, this);
    p4.add(ftxindex);
    p4.add(new BaseXLabel(FTINDEXINFO, true));

    final String[] cb = { CREATEFZ, CREATESTEM, CREATECS, CREATEDC };
    final String[] desc = { FZINDEXINFO, FTSTEMINFO, FTCSINFO, FTDCINFO };
    final boolean[] val = { Prop.ftfuzzy, Prop.ftst, Prop.ftcs, Prop.ftdc };
    for(int f = 0; f < ft.length; f++) {
      ft[f] = new BaseXCheckBox(cb[f], Token.token(desc[f]), val[f], 0, this);
      p4.add(ft[f]);
    }

    final JTabbedPane tabs = new JTabbedPane();
    BaseXLayout.addDefaultKeys(tabs, this);
    tabs.addTab(GENERALINFO, p1);
    tabs.addTab(PARSEINFO, p2);
    tabs.addTab(INDEXINFO, p3);
    tabs.addTab(FTINFO, p4);
    set(tabs, BorderLayout.CENTER);

    // create buttons
    buttons = BaseXLayout.okCancel(this);
    set(buttons, BorderLayout.SOUTH);
    action(null);
    finish(null);
  }

  /**
   * Choose an XML document or directory.
   */
  public void choose() {
    final BaseXFileChooser fc = new BaseXFileChooser(CREATETITLE,
        GUIProp.createpath, gui);
    fc.addFilter(CREATEGZDESC, IO.GZSUFFIX);
    fc.addFilter(CREATEZIPDESC, IO.ZIPSUFFIX);
    fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);

    final IO file = fc.select(BaseXFileChooser.Mode.FDOPEN);
    if(file != null) {
      path.setText(file.path());
      dbname.setText(file.dbname());
      GUIProp.createpath = file.getDir();
    }
  }

  /**
   * Returns the chosen XML file or directory.
   * @return file or directory
   */
  public String path() {
    return path.getText().trim();
  }

  /**
   * Returns the database name.
   * @return file or directory
   */
  public String dbname() {
    return dbname.getText().trim();
  }

  @Override
  public void action(final String cmd) {
    final boolean ftx = ftxindex.isSelected();
    for(int f = 0; f < ft.length; f++) ft[f].setEnabled(ftx);

    entities.setEnabled(intparse.isSelected());
    dtd.setEnabled(intparse.isSelected());
    
    final String pth = path();
    final IO file = IO.get(pth);
    final boolean exists = pth.length() != 0 && file.exists();
    if(exists) {
      GUIProp.createpath = file.path();
    }

    final String nm = dbname();
    ok = exists && nm.length() != 0;
    
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
    filter.setEnabled(exists && file.isDir());

    BaseXLayout.enableOK(buttons, BUTTONOK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;
    super.close();
    Prop.chop  = chop.isSelected();
    Prop.createfilter = filter.getText();
    Prop.entity   = entities.isSelected();
    Prop.dtd = dtd.isSelected();
    Prop.textindex = txtindex.isSelected();
    Prop.attrindex = atvindex.isSelected();
    Prop.ftindex = ftxindex.isSelected();
    Prop.intparse = intparse.isSelected();
    Prop.ftfuzzy = ft[0].isSelected();
    Prop.ftst = ft[1].isSelected();
    Prop.ftdc = ft[3].isSelected();
    Prop.ftcs = ft[2].isSelected();
  }
}
