package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.basex.build.file.HTMLParser;
import org.basex.build.xml.CatalogResolverWrapper;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.cmd.List;
import org.basex.data.DataText;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;
import org.basex.util.StringList;
import org.basex.util.Util;

/**
 * Dialog window for specifying options for creating a new database.
 *
 * @author BaseX Team 2005-11, ISC License
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

  /** Parser. */
  private final BaseXCombo parser;
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
  /** Full-text index flag. */
  private final BaseXCheckBox ftxindex;
  /** Editable full-text options. */
  private final DialogFT ft;
  /** Buttons. */
  private final BaseXBack buttons;
  /** Available databases. */
  private final StringList db;
  /** Catalog file. */
  private final BaseXTextField cfile;
  /** Use XML Catalog. */
  private final BaseXCheckBox usecat;
  /** Browse Catalog file. */
  private final BaseXButton browsec;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogCreate(final GUI main) {
    super(main, CREATEADVTITLE);

    db = List.list(main.context);
    final Prop prop = gui.context.prop;
    final GUIProp gprop = gui.gprop;

    // create panels
    final BaseXBack p1 = new BaseXBack(new BorderLayout()).border(8);

    BaseXBack p = new BaseXBack(new TableLayout(6, 2, 6, 0));
    p.add(new BaseXLabel(CREATETITLE + COL, true, true).border(0, 0, 4, 0));
    p.add(new BaseXLabel());

    path = new BaseXTextField(gprop.get(GUIProp.CREATEPATH), this);
    path.addKeyListener(keys);
    p.add(path);

    final BaseXButton browse = new BaseXButton(BUTTONBROWSE, this);
    browse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { choose(); }
    });
    p.add(browse);

    p.add(new BaseXLabel(CREATEPATTERN + COL, true, true).border(8, 0, 4, 0));
    p.add(new BaseXLabel());

    filter = new BaseXTextField(prop.get(Prop.CREATEFILTER), this);
    p.add(filter);
    p.add(new BaseXLabel());
    p.add(new BaseXLabel(CREATENAME, false, true).border(8, 0, 4, 0));
    p.add(new BaseXLabel());

    dbname = new BaseXTextField(gprop.get(GUIProp.CREATENAME), this);
    dbname.addKeyListener(keys);
    p.add(dbname);
    p.add(new BaseXLabel());
    p1.add(p, BorderLayout.CENTER);

    info = new BaseXLabel(" ");
    p1.add(info, BorderLayout.SOUTH);

    final BaseXBack p2 = new BaseXBack(new TableLayout(11, 1)).border(8);

    // always use internal/external parser, chop whitespaces, ...?
    p = new BaseXBack(new TableLayout(1, 2, 6, 0));

    final BaseXLabel parse = new BaseXLabel(CREATEFORMAT, true, true);
    final StringList parsers = new StringList();
    parsers.add(DataText.M_XML);
    if(HTMLParser.available()) parsers.add(DataText.M_HTML);
    parsers.add(DataText.M_TEXT);
    parsers.add(DataText.M_CSV);

    parser = new BaseXCombo(this, parsers.toArray());
    parser.setSelectedItem(prop.get(Prop.PARSER));
    p.add(parse);
    p.add(parser);
    p2.add(p);
    p2.add(new BaseXLabel(FORMATINFO, true, false));

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
    p2.add(new BaseXLabel(CHOPPINGINFO, false, false).border(0, 0, 8, 0));
    p2.add(new BaseXLabel());

    // CatalogResolving
    final boolean rsen = CatalogResolverWrapper.available();
    final BaseXBack fl = new BaseXBack(new TableLayout(2, 2, 6, 0));
    usecat = new BaseXCheckBox(USECATFILE,
        !prop.get(Prop.CATFILE).isEmpty(), 0, this);
    usecat.setEnabled(rsen);
    fl.add(usecat);
    fl.add(new BaseXLabel());
    cfile = new BaseXTextField(prop.get(Prop.CATFILE), this);
    cfile.setEnabled(rsen);
    fl.add(cfile);

    browsec = new BaseXButton(BUTTONBROWSE, this);
    browsec.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { catchoose(); }
    });
    browsec.setEnabled(rsen);
    fl.add(browsec);
    p2.add(fl);
    if(!rsen) {
      final BaseXBack rs = new BaseXBack(new TableLayout(2, 1));
      rs.add(new BaseXLabel(USECATHLP).color(GUIConstants.COLORDARK));
      rs.add(new BaseXLabel(USECATHLP2).color(GUIConstants.COLORDARK));
      p2.add(rs);
    }

    final BaseXBack p3 = new BaseXBack(new TableLayout(6, 1, 0, 0)).border(8);
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

    final BaseXBack p4 = new BaseXBack(new TableLayout(2, 1, 0, 0)).border(8);
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
    final GUIProp gprop = gui.gprop;
    final BaseXFileChooser fc = new BaseXFileChooser(CREATETITLE,
        gprop.get(GUIProp.CREATEPATH), gui);
    fc.addFilter(CREATEGZDESC, IO.GZSUFFIX);
    fc.addFilter(CREATEZIPDESC, IO.ZIPSUFFIX);
    fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);

    final IO file = fc.select(BaseXFileChooser.Mode.FDOPEN);
    if(file != null) {
      path.setText(file.path());
      dbname.setText(file.dbname().replaceAll("[^\\w-]", ""));
      gprop.set(GUIProp.CREATEPATH, file.dir());
    }
  }

  /**
   * Opens a file dialog to choose an XML catalog or directory.
   */
  void catchoose() {
    final GUIProp gprop = gui.gprop;
    final BaseXFileChooser fc = new BaseXFileChooser(CREATETITLE,
        gprop.get(GUIProp.CREATEPATH), gui);
    fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);

    final IO file = fc.select(BaseXFileChooser.Mode.FDOPEN);
    if(file != null) cfile.setText(file.path());
  }

  @Override
  public void action(final Object cmp) {
    ft.action(ftxindex.isSelected());

    final boolean xml = parser.getSelectedItem().toString().equals(
        DataText.M_XML);
    intparse.setEnabled(!usecat.isSelected() && xml);
    entities.setEnabled(intparse.isSelected());
    dtd.setEnabled(intparse.isSelected());

    usecat.setEnabled(!intparse.isSelected() &&
        CatalogResolverWrapper.available() && xml);
    cfile.setEnabled(usecat.isSelected());
    browsec.setEnabled(cfile.isEnabled());
    chop.setEnabled(xml);

    boolean valid;
    final String pth = path.getText().trim();
    final IO in = IO.get(pth);
    valid = in.exists() || pth.isEmpty();
    gui.gprop.set(GUIProp.CREATEPATH, pth);

    final String nm = dbname.getText().trim();
    ok = valid && !nm.isEmpty();

    String inf = !valid ? PATHWHICH : !ok ? DBWHICH : null;
    Msg icon = Msg.ERROR;
    if(ok) {
      ok = Command.validName(nm);
      if(!ok) {
        inf = Util.info(INVALID, EDITNAME);
      } else if(pth.isEmpty()) {
        inf = EMPTYDATABASE;
        icon = Msg.WARN;
      } else if(db.contains(nm)) {
        inf = RENAMEOVER;
        icon = Msg.WARN;
      }
    }
    if(ok) gui.gprop.set(GUIProp.CREATENAME, nm);

    info.setText(inf, icon);
    filter.setEnabled(valid && in.isDir());
    enableOK(buttons, BUTTONOK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;
    super.close();

    gui.set(Prop.CHOP, chop.isSelected());
    gui.set(Prop.CREATEFILTER, filter.getText());
    gui.set(Prop.ENTITY, entities.isSelected());
    gui.set(Prop.DTD, dtd.isSelected());
    gui.set(Prop.PATHINDEX, pathindex.isSelected());
    gui.set(Prop.TEXTINDEX, txtindex.isSelected());
    gui.set(Prop.ATTRINDEX, atvindex.isSelected());
    gui.set(Prop.FTINDEX, ftxindex.isSelected());
    gui.set(Prop.INTPARSE, intparse.isSelected());
    gui.set(Prop.CATFILE, usecat.isSelected() ? cfile.getText().trim() : "");
    gui.set(Prop.PARSER, parser.getSelectedItem().toString());
    ft.close();
  }
}
