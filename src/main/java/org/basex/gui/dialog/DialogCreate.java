package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.cmd.List;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.StringList;
import org.basex.util.Util;

/**
 * Dialog window for specifying options for creating a new database.
 *
 * @author BaseX Team 2005-11, BSD License
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
  /** Editable parsing options. */
  final DialogParsing parsing;
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
    final GUIProp gprop = gui.gprop;

    // create panels
    final BaseXBack p1 = new BaseXBack(new BorderLayout()).border(8);

    final BaseXBack p = new BaseXBack(new TableLayout(6, 2, 8, 0));
    p.add(new BaseXLabel(CREATETITLE + COL, true, true).border(0, 0, 4, 0));
    p.add(new BaseXLabel());

    path = new BaseXTextField(gprop.get(GUIProp.CREATEPATH), this);
    path.addKeyListener(keys);
    p.add(path);

    final BaseXButton browse = new BaseXButton(BUTTONBROWSE, this);
    browse.setMnemonic();
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

    final BaseXBack p3 = new BaseXBack(new TableLayout(6, 1, 0, 0)).border(8);
    pathindex = new BaseXCheckBox(INFOPATHINDEX,
        prop.is(Prop.PATHINDEX), 0, this);
    p3.add(pathindex);
    p3.add(new BaseXLabel(PATHINDEXINFO, true, false));

    txtindex = new BaseXCheckBox(INFOTEXTINDEX,
        prop.is(Prop.TEXTINDEX), 0, this);
    p3.add(txtindex);
    p3.add(new BaseXLabel(TXTINDEXINFO, true, false));

    atvindex = new BaseXCheckBox(INFOATTRINDEX,
        prop.is(Prop.ATTRINDEX), 0, this);
    p3.add(atvindex);
    p3.add(new BaseXLabel(ATTINDEXINFO, true, false));

    final BaseXBack p4 = new BaseXBack(new TableLayout(2, 1, 0, 0)).border(8);
    ftxindex = new BaseXCheckBox(INFOFTINDEX, prop.is(Prop.FTINDEX), 0, this);
    p4.add(ftxindex);

    ft = new DialogFT(this, true);
    p4.add(ft);

    parsing = new DialogParsing(this);

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(GENERALINFO, p1);
    tabs.addTab(PARSEINFO, parsing);
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
    fc.addFilter(CREATEXMLDESC, IO.XMLSUFFIX);
    fc.addFilter(CREATEHTMLDESC, IO.HTMLSUFFIXES);
    fc.addFilter(CREATECSVDESC, IO.CSVSUFFIX);
    fc.addFilter(CREATETXTDESC, IO.TXTSUFFIX);
    fc.addFilter(CREATEGZDESC, IO.GZSUFFIX);
    fc.addFilter(CREATEZIPDESC, IO.ZIPSUFFIXES);

    final IOFile file = fc.select(BaseXFileChooser.Mode.FDOPEN);
    if(file != null) {
      path.setText(file.path());
      dbname.setText(file.dbname().replaceAll("[^\\w-]", ""));
      gprop.set(GUIProp.CREATEPATH, file.dir());
    }
  }

  @Override
  public void action(final Object cmp) {
    ft.action(ftxindex.isSelected());
    parsing.action(cmp);

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
      ok = Command.validName(nm, false);
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
    gui.set(Prop.CREATEFILTER, filter.getText());
    gui.set(Prop.PATHINDEX, pathindex.isSelected());
    gui.set(Prop.TEXTINDEX, txtindex.isSelected());
    gui.set(Prop.ATTRINDEX, atvindex.isSelected());
    gui.set(Prop.FTINDEX, ftxindex.isSelected());
    ft.close();
    parsing.close();
  }
}
