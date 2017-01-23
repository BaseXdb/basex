package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;

/**
 * Database options dialog.
 *
 * @author BaseX Team 2005-17, BSD License
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
   * @param d dialog reference
   * @param data data reference (can be {@code null})
   */
  DialogOptions(final BaseXDialog d, final Data data) {
    gui = d.gui;
    if(data != null) {
      final MetaData meta = data.meta;
      maxlen = new BaseXTextField(Integer.toString(meta.maxlen), d);
      maxcats = new BaseXTextField(Integer.toString(meta.maxcats), d);
      splitsize = new BaseXTextField(Integer.toString(meta.splitsize), d);
      updindex = new BaseXCheckBox(UPD_INDEX, meta.updindex, d);
      autooptimize = new BaseXCheckBox(AUTOOPTIMIZE, meta.autooptimize, d);
    } else {
      final MainOptions opts = d.gui.context.options;
      maxlen = new BaseXTextField(MainOptions.MAXLEN, opts, d);
      maxcats = new BaseXTextField(MainOptions.MAXCATS, opts, d);
      splitsize = new BaseXTextField(MainOptions.SPLITSIZE, opts, d);
      updindex = new BaseXCheckBox(UPD_INDEX, MainOptions.UPDINDEX, opts, d);
      autooptimize = new BaseXCheckBox(AUTOOPTIMIZE, MainOptions.AUTOOPTIMIZE, opts, d);
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
    layout(new TableLayout(5, 1));
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
