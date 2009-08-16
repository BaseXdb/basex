package org.basex.gui.dialog;

import static org.basex.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
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
import org.basex.gui.layout.BaseXTabs;
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
  /** Buttons. */
  private final BaseXBack buttons;
  /** Available databases. */
  private final StringList db;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogCreate(final GUI main) {
    super(main, CREATEADVTITLE);

    db = List.list(main.context);
    final Prop prop = gui.context.prop;
    final GUIProp gprop = gui.prop;

    // create panels
    final BaseXBack p1 = new BaseXBack();
    p1.setLayout(new TableLayout(7, 1));
    p1.setBorder(8, 8, 8, 8);

    final BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(2, 3, 6, 0));
    p.add(new BaseXLabel(CREATETITLE + ":", false, true));
    p.add(new BaseXLabel(CREATEFILT + ":", false, true));
    p.add(new BaseXLabel(""));

    path = new BaseXTextField(gprop.get(GUIProp.CREATEPATH), null, this);
    path.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) { action(null); }
    });
    BaseXLayout.setWidth(path, 240);
    p.add(path);

    filter = new BaseXTextField(prop.get(Prop.CREATEFILTER), null, this);
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
    dbname.setText(IO.get(gprop.get(GUIProp.CREATEPATH)).dbname());
    dbname.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(final KeyEvent e) { action(null); }
    });
    BaseXLayout.setWidth(dbname, 240);
    p1.add(dbname);

    info = new BaseXLabel(" ");
    info.setBorder(40, 0, 0, 0);
    p1.add(info);

    // create checkboxes
    final BaseXBack p2 = new BaseXBack();
    p2.setLayout(new TableLayout(9, 1));
    p2.setBorder(8, 8, 8, 8);

    intparse = new BaseXCheckBox(CREATEINTPARSE, Token.token(INTPARSEINFO),
        prop.is(Prop.INTPARSE), 0, this);
    p2.add(intparse);
    p2.add(new BaseXLabel(INTPARSEINFO, true, false));

    entities = new BaseXCheckBox(CREATEENTITIES, Token.token(ENTITIESINFO),
        prop.is(Prop.ENTITY), this);
    p2.add(entities);

    dtd = new BaseXCheckBox(CREATEDTD, Token.token(DTDINFO),
        prop.is(Prop.DTD), 12, this);
    p2.add(dtd);

    chop = new BaseXCheckBox(CREATECHOP, Token.token(CHOPPINGINFO),
        prop.is(Prop.CHOP), 0, this);
    p2.add(chop);
    p2.add(new BaseXLabel(CHOPPINGINFO, true, false));

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
    tabs.addTab(PARSEINFO, p2);
    tabs.addTab(INDEXINFO, p3);
    tabs.addTab(FTINFO, p4);
    set(tabs, BorderLayout.CENTER);

    // create buttons
    buttons = okCancel(this);
    set(buttons, BorderLayout.SOUTH);

    action(null);
    finish(null);
  }

  /**
   * Opens a file dialog to choose an XML document or directory.
   */
  public void choose() {
    final GUIProp gprop = gui.prop;
    final BaseXFileChooser fc = new BaseXFileChooser(CREATETITLE,
        gprop.get(GUIProp.CREATEPATH), gui);
    fc.addFilter(CREATEGZDESC, IO.GZSUFFIX);
    fc.addFilter(CREATEZIPDESC, IO.ZIPSUFFIX);
    fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);

    final IO file = fc.select(BaseXFileChooser.Mode.FDOPEN);
    if(file != null) {
      path.setText(file.path());
      dbname.setText(file.dbname());
      gprop.set(GUIProp.CREATEPATH, file.getDir());
    }
  }

  /**
   * Returns the chosen XML file or directory path.
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
    for(final BaseXCheckBox f : ft) f.setEnabled(ftx);

    entities.setEnabled(intparse.isSelected());
    dtd.setEnabled(intparse.isSelected());

    final String pth = path();
    final IO file = IO.get(pth);
    final boolean exists = pth.length() != 0 && file.exists();
    if(exists) gui.prop.set(GUIProp.CREATEPATH, file.path());

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
        img = BaseXLayout.icon("warn");
      }
    }

    final boolean err = inf.trim().length() != 0;
    info.setText(inf);
    info.setIcon(err ? img != null ? img : BaseXLayout.icon("error") : null);
    filter.setEnabled(exists && file.isDir());

    enableOK(buttons, BUTTONOK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;
    super.close();
    final Prop prop = gui.context.prop;
    prop.set(Prop.CHOP, chop.isSelected());
    prop.set(Prop.CREATEFILTER, filter.getText());
    prop.set(Prop.ENTITY, entities.isSelected());
    prop.set(Prop.DTD, dtd.isSelected());
    prop.set(Prop.PATHINDEX, pathindex.isSelected());
    prop.set(Prop.TEXTINDEX, txtindex.isSelected());
    prop.set(Prop.ATTRINDEX, atvindex.isSelected());
    prop.set(Prop.FTINDEX, ftxindex.isSelected());
    prop.set(Prop.INTPARSE, intparse.isSelected());
    prop.set(Prop.FTFUZZY, ft[0].isSelected());
    prop.set(Prop.FTST, ft[1].isSelected());
    prop.set(Prop.FTCS, ft[2].isSelected());
    prop.set(Prop.FTDC, ft[3].isSelected());
  }
}
