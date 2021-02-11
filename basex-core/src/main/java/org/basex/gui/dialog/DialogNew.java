package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.GUIConstants.*;
import org.basex.gui.layout.*;
import org.basex.index.*;
import org.basex.util.*;

/**
 * Dialog window for specifying options for creating a new database.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class DialogNew extends BaseXDialog {
  /** Options dialog. */
  private final DialogOptions options;
  /** Database name. */
  private final BaseXTextField dbName;
  /** Buttons. */
  private final BaseXBack buttons;

  /** Text index flag. */
  private final BaseXCheckBox textindex;
  /** Attribute value index flag. */
  private final BaseXCheckBox attrindex;
  /** Full-text index flag. */
  private final BaseXCheckBox ftindex;
  /** Token index flag. */
  private final BaseXCheckBox tokenindex;

  /** Index creation options. */
  private DialogIndex[] indexes;
  /** Dialog for importing new resources. */
  private DialogImport general;

  /**
   * Default constructor.
   * @param gui reference to the main window
   */
  public DialogNew(final GUI gui) {
    super(gui, CREATE_DATABASE);

    // define buttons first to assign simplest mnemonics
    buttons = okCancel();

    final MainOptions opts = gui.context.options;
    final GUIOptions gopts = gui.gopts;

    dbName = new BaseXTextField(this, gopts.get(GUIOptions.DBNAME));

    final BaseXBack pnl = new BaseXBack(new RowLayout());
    pnl.add(new BaseXLabel(NAME_OF_DB + COLS, false, true).border(8, 0, 6, 0));
    pnl.add(dbName);

    // option panels
    final BaseXTabs tabs = new BaseXTabs(this);
    final DialogParsing parsePanel = new DialogParsing(this, tabs);
    general = new DialogImport(this, pnl, parsePanel);

    indexes = new DialogIndex[] {
      new DialogValues(this, IndexType.TEXT), new DialogValues(this, IndexType.ATTRIBUTE),
      new DialogValues(this, IndexType.TOKEN), new DialogFT(this, true)
    };
    textindex = new BaseXCheckBox(this, TEXT_INDEX, MainOptions.TEXTINDEX, opts).bold().large();
    attrindex = new BaseXCheckBox(this, ATTRIBUTE_INDEX, MainOptions.ATTRINDEX, opts).bold().
        large();
    tokenindex = new BaseXCheckBox(this, TOKEN_INDEX, MainOptions.TOKENINDEX, opts).bold().large();
    ftindex = new BaseXCheckBox(this, FULLTEXT_INDEX, MainOptions.FTINDEX, opts).bold().large();

    // index panel
    final BaseXBack indexPanel = new BaseXBack(new RowLayout()).border(8);
    indexPanel.add(textindex);
    indexPanel.add(indexes[0]);
    indexPanel.add(new BaseXBack());
    indexPanel.add(attrindex);
    indexPanel.add(indexes[1]);
    indexPanel.add(new BaseXBack());
    indexPanel.add(tokenindex);
    indexPanel.add(indexes[2]);

    // full-text index panel
    final BaseXBack ftPanel = new BaseXBack(new RowLayout()).border(8);
    ftPanel.add(ftindex);
    ftPanel.add(indexes[3]);

    // options panel
    options = new DialogOptions(this, null);

    tabs.addTab(GENERAL, general);
    tabs.addTab(PARSING, parsePanel);
    tabs.addTab(INDEXES, indexPanel);
    tabs.addTab(FULLTEXT, ftPanel);
    tabs.addTab(OPTIONS, options);
    set(tabs, BorderLayout.CENTER);

    set(buttons, BorderLayout.SOUTH);

    general.setType(general.input());
    action(general.parsers);

    setResizable(true);
    finish();
  }

  @Override
  public void action(final Object comp) {
    if(general == null) return;

    final boolean valid = general.action(comp, true) && options.action();
    indexes[0].action(textindex.isSelected());
    indexes[1].action(attrindex.isSelected());
    indexes[2].action(tokenindex.isSelected());
    indexes[3].action(ftindex.isSelected());

    // ...must be located before remaining checks
    if(comp == general.browse || comp == general.input) dbName.setText(general.dbName);

    final String nm = dbName.getText().trim();
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
      } else if(gui.context.listDBs().contains(nm)) {
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

    gui.set(MainOptions.TEXTINDEX, textindex.isSelected());
    gui.set(MainOptions.ATTRINDEX, attrindex.isSelected());
    gui.set(MainOptions.TOKENINDEX, tokenindex.isSelected());
    gui.set(MainOptions.FTINDEX, ftindex.isSelected());
    general.setOptions();
    options.setOptions(null);
    for(final DialogIndex index : indexes) index.setOptions();
    super.close();
  }

  @Override
  public void dispose() {
    general = null;
    indexes = null;
    super.dispose();
  }
}
