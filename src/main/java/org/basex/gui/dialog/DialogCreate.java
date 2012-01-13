package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.BorderLayout;

import org.basex.core.Prop;
import org.basex.core.cmd.List;
import org.basex.data.MetaData;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTabs;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IOFile;
import org.basex.util.Util;
import org.basex.util.list.StringList;

/**
 * Dialog window for specifying options for creating a new database.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class DialogCreate extends DialogImport {
  /** Database name. */
  private final BaseXTextField dbname;

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
    final GUIProp gprop = main.gprop;

    // create panels
    final BaseXBack p1 = new BaseXBack(new BorderLayout()).border(8);

    final BaseXBack p = new BaseXBack(new TableLayout(8, 2, 8, 0));
    init(p);

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

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(GENERALINFO, p1);
    tabs.addTab(PARSEINFO, parsing);
    tabs.addTab(INDEXINFO, p3);
    tabs.addTab(FTINFO, p4);
    set(tabs, BorderLayout.CENTER);

    action(null);
    finish(null);
  }

  @Override
  protected IOFile choose() {
    final IOFile input = super.choose();
    if(input != null) dbname.setText(input.dbname().replaceAll("[^\\w-]", ""));
    return input;
  }

  @Override
  public void action(final Object cmp) {
    final boolean valid = action(cmp, true);
    ft.action(ftxindex.isSelected());

    final String pth = path.getText().trim();
    final String nm = dbname.getText().trim();
    ok = valid && !nm.isEmpty();

    String inf = !valid ? PATHWHICH : !ok ? DBWHICH : null;
    Msg icon = Msg.ERROR;
    if(ok) {
      ok = MetaData.validName(nm, false);
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
    enableOK(buttons, BUTTONOK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;
    super.close();
    gui.set(Prop.PATHINDEX, pathindex.isSelected());
    gui.set(Prop.TEXTINDEX, txtindex.isSelected());
    gui.set(Prop.ATTRINDEX, atvindex.isSelected());
    gui.set(Prop.FTINDEX,   ftxindex.isSelected());
    ft.close();
  }
}
