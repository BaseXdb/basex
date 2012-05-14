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
import org.basex.util.list.*;

/**
 * Full-text creation dialog.
 *
 * @author BaseX Team 2005-12, BSD License
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
  /** Full-text labels. */
  private final BaseXLabel[] labels = new BaseXLabel[FLAGS];
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

    final Prop prop = d.gui.context.prop;
    add(new BaseXLabel(H_FULLTEXT_INDEX, true, false).border(0, 0, 6, 0));

    final String sw = prop.get(Prop.STOPWORDS);
    final String[] cb = { LANGUAGE, STEMMING, CASE_SENSITIVITY, DIACRITICS,
        STOPWORD_LIST };
    final String[] desc = { H_LANGUAGE, H_STEMMING, H_CASE, H_DIACRITICS, H_STOPWORDS };
    final boolean[] val = { !prop.get(Prop.LANGUAGE).isEmpty(), prop.is(Prop.STEMMING),
        prop.is(Prop.CASESENS), prop.is(Prop.DIACRITICS), !sw.isEmpty() };

    for(int f = 0; f < check.length; ++f) {
      check[f] = new BaseXCheckBox(cb[f], val[f], create ? 1 : 0, d);
      if(!create) {
        labels[f] = new BaseXLabel(desc[f], true, false);
      } else {
        check[f].setToolTipText(desc[f]);
      }
    }

    final BaseXBack b1 = new BaseXBack(new TableLayout(1, 2, 8, 0));
    b1.add(check[F_LANG]);
    final StringList langs = FTLexer.languages();
    language = new BaseXCombo(d, langs.toArray());
    final Language ln = Language.get(prop);
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
    swpath = new BaseXTextField(sw.isEmpty() ? d.gui.gprop.get(GUIProp.STOPPATH) : sw, d);
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
  void chooseStop() {
    final GUIProp gprop = dialog.gui.gprop;
    final BaseXFileChooser fc = new BaseXFileChooser(FILE_OR_DIR,
        gprop.get(GUIProp.STOPPATH), dialog.gui);
    final IO file = fc.select(Mode.FOPEN);
    if(file != null) {
      swpath.setText(file.path());
      gprop.set(GUIProp.STOPPATH, file.path());
    }
  }

  /**
   * Reacts on user input.
   * @param enabled enabled flag
   */
  void action(final boolean enabled) {
    for(BaseXCheckBox c : check) c.setEnabled(enabled);

    language.setEnabled(enabled && check[F_LANG].isSelected());
    swbrowse.setEnabled(enabled && check[F_STOP].isSelected());
    swpath.setEnabled(enabled && check[F_STOP].isSelected());

    final String sw = swpath.getText().trim();
    final IO file = IO.get(sw);
    final boolean exists = !sw.isEmpty() && file.exists();
    if(exists) dialog.gui.gprop.set(GUIProp.STOPPATH, sw);
  }

  /**
   * Sets the chosen options.
   */
  void setOptions() {
    final GUI gui = dialog.gui;
    final String lang = language.getSelectedItem().toString();
    gui.set(Prop.LANGUAGE, check[F_LANG].isSelected() ?
        Language.get(lang.replaceFirst(" \\(.*", "")).toString() : "");
    gui.set(Prop.STEMMING, check[F_STEM].isSelected());
    gui.set(Prop.CASESENS, check[F_CASE].isSelected());
    gui.set(Prop.DIACRITICS, check[F_DIA].isSelected());
    gui.set(Prop.STOPWORDS, check[F_STOP].isSelected() ? swpath.getText() : "");
  }
}
