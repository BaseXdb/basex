package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.core.*;
import org.basex.core.cmd.List;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Dialog window for specifying options for creating a new database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogNew extends BaseXDialog {
  /** General dialog. */
  private final DialogImport general;
  /** Database name. */
  private final BaseXTextField dbname;
  /** Buttons. */
  private final BaseXBack buttons;

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
    super(main, CREATE_DATABASE);

    // define buttons first to assign simplest mnemonics
    buttons = okCancel();

    db = List.list(main.context);
    final Prop prop = gui.context.prop;
    final GUIProp gprop = main.gprop;

    dbname = new BaseXTextField(gprop.get(GUIProp.DBNAME), this);

    final BaseXBack pnl = new BaseXBack(new TableLayout(2, 1));
    pnl.add(new BaseXLabel(NAME_OF_DB + COLS, false, true).border(8, 0, 6, 0));
    pnl.add(dbname);

    // option panels
    final BaseXTabs tabs = new BaseXTabs(this);
    final DialogParsing parsing = new DialogParsing(this, tabs);
    general = new DialogImport(this, pnl, parsing);

    // index panel
    final BaseXBack indexes = new BaseXBack(new TableLayout(6, 1, 0, 0)).border(8);

    txtindex = new BaseXCheckBox(TEXT_INDEX, prop.is(Prop.TEXTINDEX), 0, this).large();
    indexes.add(txtindex);
    indexes.add(new BaseXLabel(H_TEXT_INDEX, true, false));

    atvindex = new BaseXCheckBox(ATTRIBUTE_INDEX,
        prop.is(Prop.ATTRINDEX), 0, this).large();
    indexes.add(atvindex);
    indexes.add(new BaseXLabel(H_ATTR_INDEX, true, false));

    // full-text panel
    //final BaseXBack fulltext = new BaseXBack(new TableLayout(2, 1, 0, 0)).border(8);
    ftxindex = new BaseXCheckBox(FULLTEXT_INDEX, prop.is(Prop.FTINDEX), 0, this).large();
    indexes.add(ftxindex);

    ft = new DialogFT(this, true);
    indexes.add(ft);

    tabs.addTab(GENERAL, general);
    tabs.addTab(PARSING, parsing);
    tabs.addTab(INDEXES, indexes);
    set(tabs, BorderLayout.CENTER);

    set(buttons, BorderLayout.SOUTH);

    action(general.parser);
    finish(null);
  }

  @Override
  public void action(final Object comp) {
    final boolean valid = general.action(comp, true);
    ft.action(ftxindex.isSelected());

    // ...must be located before remaining checks
    if(comp == general.browse || comp == general.input) dbname.setText(general.dbname);

    final String nm = dbname.getText().trim();
    ok = valid && !nm.isEmpty();

    String inf = !valid ? RES_NOT_FOUND : !ok ? ENTER_DB_NAME : null;
    Msg icon = Msg.ERROR;
    if(ok) {
      ok = MetaData.validName(nm, false);
      if(ok) gui.gprop.set(GUIProp.DBNAME, nm);

      if(!ok) {
        // name of database is invalid
        inf = Util.info(INVALID_X, NAME);
      } else if(general.input.getText().trim().isEmpty()) {
        // database will be empty
        inf = EMPTY_DB;
        icon = Msg.WARN;
      } else if(db.contains(nm)) {
        // old database will be overwritten
        inf = OVERWRITE_DB;
        icon = Msg.WARN;
      }
    }

    general.info.setText(inf, icon);
    enableOK(buttons, B_OK, ok);
  }

  @Override
  public void close() {
    if(!ok) return;

    super.close();
    gui.set(Prop.TEXTINDEX, txtindex.isSelected());
    gui.set(Prop.ATTRINDEX, atvindex.isSelected());
    gui.set(Prop.FTINDEX,   ftxindex.isSelected());
    general.setOptions();
    ft.setOptions();
  }
}
