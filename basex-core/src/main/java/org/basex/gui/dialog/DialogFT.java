package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.event.*;

import javax.swing.*;

import org.basex.core.*;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.io.*;
import org.basex.util.ft.*;

/**
 * Full-text creation dialog.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class DialogFT extends BaseXBack {
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
  /** Number of flags. */
  private static final int FLAGS = 5;

  /** Dialog reference. */
  private final BaseXDialog dialog;
  /** Full-text indexing. */
  private final BaseXCheckBox[] check = new BaseXCheckBox[FLAGS];
    /** Full-text language. */
  private final BaseXCombo language;
  /** Path for Full-text stopword list. */
  private final BaseXTextField swpath;
  /** Path button for full-text stopword list path. */
  private final BaseXButton swbrowse;

  /**
   * Constructor.
   * @param d dialog reference
   * @param create create dialog
   */
  DialogFT(final BaseXDialog d, final boolean create) {
    dialog = d;
    layout(new TableLayout(create ? 9 : 15, 1));

    final MainOptions opts = d.gui.context.options;
    add(new BaseXLabel(H_FULLTEXT_INDEX, true, false).border(0, 0, 6, 0));

    final String sw = opts.get(MainOptions.STOPWORDS);
    final String[] cb = { LANGUAGE, STEMMING, CASE_SENSITIVE, DIACRITICS, STOPWORD_LIST };
    final String[] desc = { H_LANGUAGE, H_STEMMING, H_CASE, H_DIACRITICS, H_STOPWORDS };
    final boolean[] val = {
      !opts.get(MainOptions.LANGUAGE).isEmpty(), opts.get(MainOptions.STEMMING),
      opts.get(MainOptions.CASESENS), opts.get(MainOptions.DIACRITICS), !sw.isEmpty() };

    final BaseXLabel[] labels = new BaseXLabel[FLAGS];
    for(int f = 0; f < check.length; ++f) {
      check[f] = new BaseXCheckBox(cb[f], val[f], d);
      if(create) {
        check[f].setToolTipText(desc[f]);
      } else {
        check[f].bold();
        labels[f] = new BaseXLabel(desc[f], true, false);
      }
    }

    final BaseXBack b1 = new BaseXBack(new TableLayout(1, 2, 8, 0));
    b1.add(check[F_LANG]);
    final String[] langs = FTLexer.languages().finish();
    language = new BaseXCombo(d, langs);
    final Language ln = Language.get(opts);
    for(final String l : langs) {
      final String s = l.replaceFirst(" \\(.*", "");
      if(s.equals(ln.toString())) language.setSelectedItem(l);
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
    final BaseXBack b3 = new BaseXBack(new TableLayout(1, 2, 8, 0));
    swpath = new BaseXTextField(sw.isEmpty() ? d.gui.gopts.get(GUIOptions.DATAPATH) : sw, d);
    b3.add(swpath);

    swbrowse = new BaseXButton(BROWSE_D, d);
    swbrowse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        chooseStop();
      }
    });
    b3.add(swbrowse);
    add(b3);
    if(!create) add(labels[F_STOP]);
  }

  /**
   * Opens a file dialog to choose a stopword list.
   */
  private void chooseStop() {
    final GUIOptions gopts = dialog.gui.gopts;
    final BaseXFileChooser fc = new BaseXFileChooser(FILE_OR_DIR, gopts.get(GUIOptions.DATAPATH),
        dialog.gui);
    final IO file = fc.select(Mode.FOPEN);
    if(file != null) {
      swpath.setText(file.path());
      gopts.set(GUIOptions.DATAPATH, file.path());
    }
  }

  /**
   * Reacts on user input.
   * @param enabled enabled flag
   */
  void action(final boolean enabled) {
    for(final BaseXCheckBox c : check) c.setEnabled(enabled);

    language.setEnabled(enabled && check[F_LANG].isSelected());
    swbrowse.setEnabled(enabled && check[F_STOP].isSelected());
    swpath.setEnabled(enabled && check[F_STOP].isSelected());

    final String sw = swpath.getText().trim();
    final IO file = IO.get(sw);
    final boolean exists = !sw.isEmpty() && file.exists();
    if(exists) dialog.gui.gopts.set(GUIOptions.DATAPATH, sw);
  }

  /**
   * Sets the chosen options.
   */
  void setOptions() {
    final GUI gui = dialog.gui;
    final String lang = language.getSelectedItem();
    gui.set(MainOptions.LANGUAGE, check[F_LANG].isSelected() ?
        Language.get(lang.replaceFirst(" \\(.*", "")).code() : "");
    gui.set(MainOptions.STEMMING, check[F_STEM].isSelected());
    gui.set(MainOptions.CASESENS, check[F_CASE].isSelected());
    gui.set(MainOptions.DIACRITICS, check[F_DIA].isSelected());
    gui.set(MainOptions.STOPWORDS, check[F_STOP].isSelected() ? swpath.getText() : "");
  }
}
