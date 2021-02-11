package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Database options dialog.
 *
 * @author BaseX Team 2005-21, BSD License
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
  /** Index splits. */
  private final BaseXTextField splitsize;

  /**
   * Default constructor.
   * @param dialog dialog reference
   * @param data data reference (can be {@code null})
   */
  DialogOptions(final BaseXDialog dialog, final Data data) {
    gui = dialog.gui;
    if(data != null) {
      final MetaData meta = data.meta;
      maxlen = new BaseXTextField(dialog, Integer.toString(meta.maxlen));
      maxcats = new BaseXTextField(dialog, Integer.toString(meta.maxcats));
      splitsize = new BaseXTextField(dialog, Integer.toString(meta.splitsize));
      updindex = new BaseXCheckBox(dialog, UPD_INDEX, meta.updindex);
      autooptimize = new BaseXCheckBox(dialog, AUTOOPTIMIZE, meta.autooptimize);
    } else {
      final MainOptions opts = dialog.gui.context.options;
      maxlen = new BaseXTextField(dialog, MainOptions.MAXLEN, opts);
      maxcats = new BaseXTextField(dialog, MainOptions.MAXCATS, opts);
      splitsize = new BaseXTextField(dialog, MainOptions.SPLITSIZE, opts);
      updindex = new BaseXCheckBox(dialog, UPD_INDEX, MainOptions.UPDINDEX, opts);
      autooptimize = new BaseXCheckBox(dialog, AUTOOPTIMIZE, MainOptions.AUTOOPTIMIZE, opts);
    }
    maxlen.setColumns(8);
    maxcats.setColumns(8);
    splitsize.setColumns(8);

    final BaseXBack p = new BaseXBack(new TableLayout(4, 2, 6, 4));
    p.border(12, 0, 0, 0);
    p.add(new BaseXLabel(MainOptions.MAXLEN.name() + COL, true, true));
    p.add(maxlen);
    p.add(new BaseXLabel(MainOptions.MAXCATS.name() + COL, true, true));
    p.add(maxcats);
    p.add(new BaseXLabel(MainOptions.SPLITSIZE.name() + COL, true, true));
    p.add(splitsize);

    border(8);
    layout(new RowLayout());
    add(new BaseXLabel(INDEX_CREATION).border(0, 0, 8, 0).large());
    add(updindex);
    add(autooptimize);
    add(p);
    add(new BaseXLabel(data != null ? H_DB_OPTIONS_X : "").border(12, 0, 0, 0));
  }

  /**
   * Reacts on user input.
   * @return success flag
   */
  boolean action() {
    // no short-circuiting, do all checks...
    return maxlen.check() & maxcats.check() & splitsize.check();
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
    gui.set(MainOptions.SPLITSIZE, Integer.parseInt(splitsize.getText()));
  }
}
