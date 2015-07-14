package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.core.*;
import org.basex.core.cmd.List;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.Msg;
import org.basex.gui.layout.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Dialog window for specifying options for creating a new database.
 *
 * @author BaseX Team 2005-15, BSD License
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
  /** Index creation options. */
  private final DialogIndex[] index;
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

    db = List.list(main.context.soptions);
    final MainOptions opts = gui.context.options;
    final GUIOptions gopts = main.gopts;

    dbname = new BaseXTextField(gopts.get(GUIOptions.DBNAME), this);

    final BaseXBack pnl = new BaseXBack(new TableLayout(2, 1));
    pnl.add(new BaseXLabel(NAME_OF_DB + COLS, false, true).border(8, 0, 6, 0));
    pnl.add(dbname);

    // option panels
    final BaseXTabs tabs = new BaseXTabs(this);
    final DialogParsing parsing = new DialogParsing(this, tabs);
    general = new DialogImport(this, pnl, parsing);

    index = new DialogIndex[] {
      new DialogValues(this, true), new DialogValues(this, false), new DialogFT(this, true)
    };
    txtindex = new BaseXCheckBox(TEXT_INDEX, MainOptions.TEXTINDEX, opts, this).bold().large();
    atvindex = new BaseXCheckBox(ATTRIBUTE_INDEX, MainOptions.ATTRINDEX, opts, this).bold().large();
    ftxindex = new BaseXCheckBox(FULLTEXT_INDEX, MainOptions.FTINDEX, opts, this).bold().large();

    // index panel
    final BaseXBack indexes = new BaseXBack(new TableLayout(5, 1)).border(8);
    indexes.add(txtindex);
    indexes.add(index[0]);
    indexes.add(new BaseXBack());
    indexes.add(atvindex);
    indexes.add(index[1]);

    // full-text panel
    final BaseXBack ftindex = new BaseXBack(new TableLayout(2, 1)).border(8);
    ftindex.add(ftxindex);
    ftindex.add(index[2]);

    tabs.addTab(GENERAL, general);
    tabs.addTab(PARSING, parsing);
    tabs.addTab(INDEXES, indexes);
    tabs.addTab(FULLTEXT_INDEX, ftindex);
    set(tabs, BorderLayout.CENTER);

    set(buttons, BorderLayout.SOUTH);

    general.setType(general.input());
    action(general.parsers);

    setResizable(true);
    finish(null);
  }

  @Override
  public void action(final Object comp) {
    final boolean valid = general.action(comp, true);
    index[0].action(txtindex.isSelected());
    index[1].action(atvindex.isSelected());
    index[2].action(ftxindex.isSelected());

    // ...must be located before remaining checks
    if(comp == general.browse || comp == general.input) dbname.setText(general.dbname);

    final String nm = dbname.getText().trim();
    ok = valid && !nm.isEmpty();

    String inf = valid ? ok ? null : ENTER_DB_NAME : RES_NOT_FOUND;
    Msg icon = Msg.ERROR;
    if(ok) {
      ok = Databases.validName(nm);
      if(ok) gui.gopts.set(GUIOptions.DBNAME, nm);

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
    gui.set(MainOptions.TEXTINDEX, txtindex.isSelected());
    gui.set(MainOptions.ATTRINDEX, atvindex.isSelected());
    gui.set(MainOptions.FTINDEX,   ftxindex.isSelected());
    gui.set(MainOptions.FTINDEX,   ftxindex.isSelected());
    general.setOptions();
    for(final DialogIndex di : index) di.setOptions();
  }
}
