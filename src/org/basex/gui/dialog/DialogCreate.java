package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.proc.List;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.GUIConstants.Msg;
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

/**
 * Dialog window for specifying options for creating a new database.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class DialogCreate extends Dialog {
  /** File path. */
  private final BaseXTextField path;
  /** Document filter. */
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
  /** Editable full-text options. */
  private final DialogFT ft;
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
    p1.setLayout(new TableLayout(3, 1, 0, 4));
    p1.setBorder(8, 8, 8, 8);

    BaseXBack p = new BaseXBack();
    p.setLayout(new TableLayout(2, 2, 6, 0));
    p.add(new BaseXLabel(CREATETITLE + COL, false, true));
    p.add(new BaseXLabel(""));

    path = new BaseXTextField(gprop.get(GUIProp.OPENPATH), this);
    path.addKeyListener(keys);
    p.add(path);

    final BaseXButton browse = new BaseXButton(BUTTONBROWSE, this);
    browse.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) { choose(); }
    });
    p.add(browse);
    p1.add(p);

    p = new BaseXBack();
    p.setLayout(new TableLayout(2, 2, 6, 0));
    p.add(new BaseXLabel(CREATENAME, false, true));
    p.add(new BaseXLabel(CREATEFILT + COL, false, true));

    dbname = new BaseXTextField(this);
    final String dbn = IO.get(gprop.get(GUIProp.OPENPATH)).dbname();
    dbname.setText(dbn.replaceAll("[^\\w.-]", ""));
    dbname.addKeyListener(keys);
    p.add(dbname);

    filter = new BaseXTextField(prop.get(Prop.CREATEFILTER), this);
    BaseXLayout.setWidth(filter, 54);
    p.add(filter);
    p1.add(p);

    info = new BaseXLabel(" ");
    info.setBorder(82, 0, 0, 0);
    p1.add(info);

    // create checkboxes
    final BaseXBack p2 = new BaseXBack();
    p2.setLayout(new TableLayout(9, 1));
    p2.setBorder(8, 8, 8, 8);

    intparse = new BaseXCheckBox(CREATEINTPARSE,
        prop.is(Prop.INTPARSE), 0, this);
    p2.add(intparse);
    p2.add(new BaseXLabel(INTPARSEINFO, true, false));

    entities = new BaseXCheckBox(CREATEENTITIES, prop.is(Prop.ENTITY), this);
    p2.add(entities);

    dtd = new BaseXCheckBox(CREATEDTD, prop.is(Prop.DTD), 12, this);
    p2.add(dtd);

    chop = new BaseXCheckBox(CREATECHOP, prop.is(Prop.CHOP), 0, this);
    p2.add(chop);
    p2.add(new BaseXLabel(CHOPPINGINFO, true, false));

    // create checkboxes
    final BaseXBack p3 = new BaseXBack();
    p3.setLayout(new TableLayout(6, 1, 0, 0));
    p3.setBorder(8, 8, 8, 8);

    txtindex = new BaseXCheckBox(INFOTEXTINDEX,
        prop.is(Prop.TEXTINDEX), 0, this);
    p3.add(txtindex);
    p3.add(new BaseXLabel(TXTINDEXINFO, true, false));

    atvindex = new BaseXCheckBox(INFOATTRINDEX,
        prop.is(Prop.ATTRINDEX), 0, this);
    p3.add(atvindex);
    p3.add(new BaseXLabel(ATTINDEXINFO, true, false));

    pathindex = new BaseXCheckBox(INFOPATHINDEX,
        prop.is(Prop.PATHINDEX), 0, this);
    p3.add(pathindex);
    p3.add(new BaseXLabel(PATHINDEXINFO, true, false));

    // create checkboxes
    final BaseXBack p4 = new BaseXBack();

    p4.setLayout(new TableLayout(2, 1, 0, 0));
    p4.setBorder(8, 8, 8, 8);

    ftxindex = new BaseXCheckBox(INFOFTINDEX, prop.is(Prop.FTINDEX), 0, this);
    p4.add(ftxindex);

    ft = new DialogFT(this, true);
    p4.add(ft);

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
  void choose() {
    final GUIProp gprop = gui.prop;
    final BaseXFileChooser fc = new BaseXFileChooser(CREATETITLE,
        gprop.get(GUIProp.OPENPATH), gui);
    fc.addFilter(CREATEGZDESC, IO.GZSUFFIX);
    fc.addFilter(CREATEZIPDESC, IO.ZIPSUFFIX);
    fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);

    final IO file = fc.select(BaseXFileChooser.Mode.FDOPEN);
    if(file != null) {
      path.setText(file.path());
      dbname.setText(file.dbname().replaceAll("[^\\w.-]", ""));
      gprop.set(GUIProp.OPENPATH, file.getDir());
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
  public void action(final Object cmp) {
    ft.action(ftxindex.isSelected());

    entities.setEnabled(intparse.isSelected());
    dtd.setEnabled(intparse.isSelected());

    final IO file = IO.get(path());
    final boolean exists = !path().isEmpty() && file.exists();
    if(exists) gui.prop.set(GUIProp.OPENPATH, file.path());

    final String nm = dbname();
    ok = exists && !nm.isEmpty();

    String inf = !exists ? PATHWHICH : !ok ? DBWHICH : null;
    Msg icon = Msg.ERR;
    if(ok) {
      ok = dbValid(nm);
      if(!ok) {
        inf = Main.info(INVALID, EDITNAME);
      } else if(db.contains(nm)) {
        inf = RENAMEOVER;
        icon = Msg.WARN;
      }
    }
    info.setText(inf, icon);
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
    ft.close();
  }

  /**
   * Checks if the specified filename is valid; allows only letters,
   * digits and some special characters.
   * @param fn filename
   * @return result of check
   */
  public static boolean dbValid(final String fn) {
    return fn.matches("[\\w.-]+");
  }
}
