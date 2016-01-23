package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Database options dialog.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public class DialogOptions extends BaseXBack {
  /** Dialog reference. */
  private final GUI gui;

  /** Autooptimize. */
  private final BaseXCheckBox autooptimize;
  /** Updindex. */
  private final BaseXCheckBox updindex;
  /** Maximum length. */
  private final BaseXTextField maxlen;
  /** Maximum categories. */
  private final BaseXTextField maxcats;
  /** Index split size. */
  private final BaseXTextField indexsplitsize;
  /** Full-text index split size. */
  private final BaseXTextField ftindexsplitsize;

  /**
   * Default constructor.
   * @param d dialog reference
   * @param data data reference (can be {@code null})
   */
  DialogOptions(final BaseXDialog d, final Data data) {
    gui = d.gui;
    if(data != null) {
      final MetaData meta = data.meta;
      maxlen = new BaseXTextField(Integer.toString(meta.maxlen), d);
      maxcats = new BaseXTextField(Integer.toString(meta.maxcats), d);
      indexsplitsize = new BaseXTextField(Integer.toString(meta.splitsize), d);
      ftindexsplitsize = new BaseXTextField(Integer.toString(meta.ftsplitsize), d);
      updindex = new BaseXCheckBox(UPD_INDEX, meta.updindex, d);
      autooptimize = new BaseXCheckBox(AUTOOPTIMIZE, meta.autooptimize, d);
    } else {
      final MainOptions opts = d.gui.context.options;
      maxlen = new BaseXTextField(MainOptions.MAXLEN, opts, d);
      maxcats = new BaseXTextField(MainOptions.MAXCATS, opts, d);
      indexsplitsize = new BaseXTextField(MainOptions.INDEXSPLITSIZE, opts, d);
      ftindexsplitsize = new BaseXTextField(MainOptions.FTINDEXSPLITSIZE, opts, d);
      updindex = new BaseXCheckBox(UPD_INDEX, MainOptions.UPDINDEX, opts, d);
      autooptimize = new BaseXCheckBox(AUTOOPTIMIZE, MainOptions.AUTOOPTIMIZE, opts, d);
    }
    maxlen.setColumns(10);
    maxcats.setColumns(10);
    indexsplitsize.setColumns(10);
    ftindexsplitsize.setColumns(10);

    final BaseXBack p = new BaseXBack(new TableLayout(4, 2, 8, 4));
    p.border(12, 0, 0, 0);
    p.add(new BaseXLabel(MainOptions.MAXLEN.name() + COL, true, true));
    p.add(maxlen);
    p.add(new BaseXLabel(MainOptions.MAXCATS.name() + COL, true, true));
    p.add(maxcats);
    p.add(new BaseXLabel(MainOptions.INDEXSPLITSIZE.name() + COL, true, true));
    p.add(indexsplitsize);
    p.add(new BaseXLabel(MainOptions.FTINDEXSPLITSIZE.name() + COL, true, true));
    p.add(ftindexsplitsize);

    border(8);
    layout(new TableLayout(5, 1));
    add(new BaseXLabel(INDEX_CREATION).border(0, 0, 8, 0).large());
    add(updindex);
    add(autooptimize);
    add(p);
    add(new BaseXLabel(H_DB_OPTIONS_X).border(12, 0, 0, 0));
  }

  /**
   * Reacts on user input.
   * @return success flag
   */
  boolean action() {
    // no short-circuiting, do all checks...
    return maxlen.check() & maxcats.check() & indexsplitsize.check() & ftindexsplitsize.check();
  }

  /**
   * Assigns options.
   * @param data data reference
   */
  void setOptions(final Data data) {
    if(data != null) {
      final MetaData meta = data.meta;
      meta.maxlen = Integer.parseInt(maxlen.getText());
      meta.maxcats = Integer.parseInt(maxcats.getText());
    } else {
      gui.set(MainOptions.MAXLEN, Integer.parseInt(maxlen.getText()));
      gui.set(MainOptions.MAXCATS, Integer.parseInt(maxcats.getText()));
    }
    gui.set(MainOptions.UPDINDEX, updindex.isSelected());
    gui.set(MainOptions.AUTOOPTIMIZE, autooptimize.isSelected());
    gui.set(MainOptions.INDEXSPLITSIZE, Integer.parseInt(indexsplitsize.getText()));
    gui.set(MainOptions.FTINDEXSPLITSIZE, Integer.parseInt(ftindexsplitsize.getText()));
  }
}
