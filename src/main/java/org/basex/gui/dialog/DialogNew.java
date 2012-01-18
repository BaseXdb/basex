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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogNew extends Dialog {
  /** Buttons. */
  protected final DialogImport options;
  /** Database name. */
  private final BaseXTextField target;
  /** Parsing options. */
  private final DialogParsing parsing;
  /** Buttons. */
  protected final BaseXBack buttons;

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
  public DialogNew(final GUI main) {
    super(main, CREATEADVTITLE);

    db = List.list(main.context);
    final Prop prop = gui.context.prop;
    final GUIProp gprop = main.gprop;

    target = new BaseXTextField(gprop.get(GUIProp.CREATENAME), this);
    target.addKeyListener(keys);

    final BaseXBack pnl = new BaseXBack(new TableLayout(2, 1));
    pnl.add(new BaseXLabel(CREATENAME + COLS, false, true).border(8, 0, 4, 0));
    pnl.add(target);

    // option panels
    options = new DialogImport(this, pnl);
    parsing = new DialogParsing(this);

    // index panel
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

    // full-text panel
    final BaseXBack p4 = new BaseXBack(new TableLayout(2, 1, 0, 0)).border(8);
    ftxindex = new BaseXCheckBox(INFOFTINDEX, prop.is(Prop.FTINDEX), 0, this);
    p4.add(ftxindex);

    ft = new DialogFT(this, true);
    p4.add(ft);

    final BaseXTabs tabs = new BaseXTabs(this);
    tabs.addTab(GENERALINFO, options);
    tabs.addTab(PARSEINFO, parsing);
    tabs.addTab(INDEXINFO, p3);
    tabs.addTab(FTINFO, p4);
    set(tabs, BorderLayout.CENTER);

    buttons = okCancel();
    set(buttons, BorderLayout.SOUTH);

    action(null);
    finish(null);
  }

  /**
   * Chooses an input file.
   * @return resulting file
   */
  protected IOFile choose() {
    final IOFile input = options.choose();
    if(input != null) target.setText(input.dbname().replaceAll("[^\\w-]", ""));
    return input;
  }

  @Override
  public void action(final Object cmp) {
    final boolean valid = options.action(true);
    parsing.action(cmp);
    ft.action(ftxindex.isSelected());

    final String nm = target.getText().trim();
    ok = valid && !nm.isEmpty();

    String inf = !valid ? PATHWHICH : !ok ? DBWHICH : null;
    Msg icon = Msg.ERROR;
    if(ok) {
      ok = MetaData.validName(nm, false);
      if(ok) gui.gprop.set(GUIProp.CREATENAME, nm);

      if(!ok) {
        // name of database is invalid
        inf = Util.info(INVALID, EDITNAME);
      } else if(options.input.getText().trim().isEmpty()) {
        // database will be empty
        inf = EMPTYDATABASE;
        icon = Msg.WARN;
      } else if(db.contains(nm)) {
        // old database will be overwritten
        inf = RENAMEOVER;
        icon = Msg.WARN;
      }
    }

    options.info.setText(inf, icon);
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
    options.setOptions();
    parsing.setOptions();
    ft.setOptions();
  }
}
