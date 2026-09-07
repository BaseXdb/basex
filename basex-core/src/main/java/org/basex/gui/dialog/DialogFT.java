package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.*;
import org.basex.io.*;
import org.basex.util.ft.*;

/**
 * Full-text creation dialog.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DialogFT extends DialogIndex {
  /** Language flag. */
  private static final int F_LANG = 0;
  /** Stemming flag. */
  private static final int F_STEM = 1;
  /** Case flag. */
  private static final int F_CASE = 2;
  /** Diacritics flag. */
  private static final int F_DIA = 3;
  /** Stopwords flag. */
  private static final int F_STOP = 4;
  /** Mixed content flag. */
  private static final int F_MIXED = 5;
  /** Number of flags. */
  private static final int FLAGS = 6;

  /** Full-text indexing. */
  private final BaseXCheckBox[] check = new BaseXCheckBox[FLAGS];
    /** Full-text language. */
  private final BaseXCombo language;
  /** Path to Full-text stopword list. */
  private final BaseXTextField swpath;
  /** Element names to include. */
  private final BaseXTextField ftinc;
  /** Path button for full-text stopword list path. */
  private final BaseXButton swbrowse;

  /**
   * Constructor.
   * @param dialog dialog reference
   * @param create create dialog
   */
  DialogFT(final BaseXDialog dialog, final boolean create) {
    super(dialog);
    layout(new TableLayout(create ? 11 : 18, 1));

    final MainOptions opts = dialog.gui().context.options;
    add(new BaseXLabel(H_FULLTEXT_INDEX, true, false).border(0, 0, 6, 0));

    ftinc = new BaseXTextField(dialog, opts.get(MainOptions.FTINCLUDE)).hint(QNAME_INPUT);
    add(ftinc);

    final String sw = opts.get(MainOptions.STOPWORDS);
    final String[] cb = { LANGUAGE, STEMMING, CASE_SENSITIVE, DIACRITICS, STOPWORD_LIST,
      MIXED_CONTENT };
    final String[] desc = { H_LANGUAGE, H_STEMMING, H_CASE, H_DIACRITICS, H_STOPWORDS,
      H_MIXED_CONTENT };
    final boolean[] val = {
      !opts.get(MainOptions.LANGUAGE).isEmpty(), opts.get(MainOptions.STEMMING),
      opts.get(MainOptions.CASESENS), opts.get(MainOptions.DIACRITICS), !sw.isEmpty(),
      opts.get(MainOptions.FTMIXED) };

    final BaseXLabel[] labels = new BaseXLabel[FLAGS];
    final int cl = check.length;
    for(int c = 0; c < cl; ++c) {
      check[c] = new BaseXCheckBox(dialog, cb[c], val[c]);
      if(create) {
        check[c].setToolTipText(desc[c]);
      } else {
        check[c].bold();
        labels[c] = new BaseXLabel(desc[c], true, false);
      }
    }

    // mixed content: refers to the element names of the input field above
    add(check[F_MIXED]);
    if(!create) add(labels[F_MIXED]);

    final BaseXBack b1 = new BaseXBack(new ColumnLayout(8)).border(12, 0, 0, 0);
    b1.add(check[F_LANG]);
    final String[] langs = FTLexer.languages().finish();
    language = new BaseXCombo(dialog, langs);
    final String ln = Language.get(opts).toString();
    for(final String lang : langs) {
      if(lang.replaceFirst(" \\(.*", "").equals(ln)) language.setSelectedItem(lang);
    }

    b1.add(language);
    add(b1);
    if(!create) add(labels[F_LANG]);

    for(int f = 1; f < F_STOP; ++f) {
      add(check[f]);
      if(!create) add(labels[f]);
    }

    add(check[F_STOP]);
    add(Box.createVerticalStrut(4));
    final BaseXBack b3 = new BaseXBack(new ColumnLayout(8));
    swpath = new BaseXTextField(
        dialog, sw.isEmpty() ? dialog.gui().gopts.get(GUIOptions.DATAPATH) : sw);
    b3.add(swpath);

    swbrowse = new BaseXButton(dialog, BROWSE_D);
    swbrowse.addActionListener(e -> chooseStop());
    b3.add(swbrowse);
    add(b3);
    if(!create) add(labels[F_STOP]);
  }

  /**
   * Opens a file dialog to choose a stopword list.
   */
  private void chooseStop() {
    final GUIOptions gopts = dialog.gui().gopts;
    final BaseXFileChooser fc = new BaseXFileChooser(dialog,
        FILE_OR_DIR, gopts.get(GUIOptions.DATAPATH));
    final IOFile file = fc.select(Mode.FOPEN);
    if(file != null) {
      swpath.setText(file.path());
      gopts.setFile(GUIOptions.DATAPATH, file);
    }
  }

  @Override
  boolean action(final boolean enabled) {
    for(final BaseXCheckBox c : check) c.setEnabled(enabled);

    ftinc.setEnabled(enabled);
    language.setEnabled(enabled && check[F_LANG].isSelected());
    swbrowse.setEnabled(enabled && check[F_STOP].isSelected());
    swpath.setEnabled(enabled && check[F_STOP].isSelected());

    final String sw = swpath.getText().trim();
    final IO file = IO.get(sw);
    final boolean exists = !sw.isEmpty() && file.exists();
    if(exists) dialog.gui().gopts.set(GUIOptions.DATAPATH, sw);

    // mixed content can only be indexed if element names are specified
    final boolean valid = !enabled || !check[F_MIXED].isSelected() || mixed();
    ftinc.valid(valid);
    return valid;
  }

  /**
   * Indicates if mixed content can be indexed, i.e., if element names were specified.
   * @return result of check
   */
  private boolean mixed() {
    return check[F_MIXED].isSelected() && !ftinc.getText().trim().isEmpty();
  }

  @Override
  void setOptions() {
    final GUI gui = dialog.gui();
    gui.set(MainOptions.LANGUAGE, check[F_LANG].isSelected() ?
        Language.get(language.getSelectedItem().replaceFirst(" \\(.*", "")).code() : "");
    gui.set(MainOptions.STEMMING, check[F_STEM].isSelected());
    gui.set(MainOptions.CASESENS, check[F_CASE].isSelected());
    gui.set(MainOptions.DIACRITICS, check[F_DIA].isSelected());
    gui.set(MainOptions.STOPWORDS, check[F_STOP].isSelected() ? swpath.getText() : "");
    gui.set(MainOptions.FTINCLUDE, ftinc.getText());
    // the properties dialog ignores the result of the action method: check the input again
    gui.set(MainOptions.FTMIXED, mixed());
  }
}
