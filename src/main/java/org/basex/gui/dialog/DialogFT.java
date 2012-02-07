package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.border.*;

import org.basex.core.Prop;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.Language;
import org.basex.util.list.StringList;

/**
 * Full-text creation dialog.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class DialogFT extends BaseXBack {
  /** Language flag. */
  private static final int F_LANG = 0;
  /** Wildcards flag. */
  private static final int F_WILD = 1;
  /** Stemming flag. */
  private static final int F_STEM = 2;
  /** Case flag. */
  private static final int F_CASE = 3;
  /** Diacritics flag. */
  private static final int F_DIA = 4;
  /** Scoring flag. */
  private static final int F_SCORE = 5;
  /** Stopwords flag. */
  private static final int F_STOP = 6;
  /** Number of flags. */
  private static final int FLAGS = 7;

  /** Dialog reference. */
  private final Dialog dialog;
  /** Full-text indexing. */
  private final BaseXCheckBox[] check = new BaseXCheckBox[FLAGS];
  /** Full-text labels. */
  private final BaseXLabel[] labels = new BaseXLabel[FLAGS];
  /** Full-text language. */
  private final BaseXCombo language;
  /** Full-text scoring type. */
  private final BaseXCombo scoring;
  /** Path for Full-text stopword list. */
  private final BaseXTextField swpath;
  /** Path button for full-text stopword list path. */
  private final BaseXButton swbrowse;

  /**
   * Constructor.
   * @param d dialog reference
   * @param create create dialog
   */
  DialogFT(final Dialog d, final boolean create) {
    dialog = d;
    layout(new TableLayout(create ? 9 : 16, 1));

    final Prop prop = d.gui.context.prop;
    add(new BaseXLabel(H_FULLTEXT_INDEX, true, false).border(0, 0, 12, 0));

    final String sw = prop.get(Prop.STOPWORDS);
    final String[] cb = { LANGUAGE, SUPPORT_WILDCARDS, STEMMING,
        CASE_SENSITIVITY, DIACRITICS, TFIDF_SCORING, STOPWORD_LIST };
    final String[] desc = { H_LANGUAGE, H_WILDCARD, H_STEMMING, H_CASE,
        H_DIACRITICS, H_SCORING, H_STOPWORDS };
    final boolean[] val = {
        !prop.get(Prop.LANGUAGE).isEmpty(), prop.is(Prop.WILDCARDS),
        prop.is(Prop.STEMMING), prop.is(Prop.CASESENS),
        prop.is(Prop.DIACRITICS), prop.num(Prop.SCORING) > 0, !sw.isEmpty() };

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

    for(int f = 1; f < F_SCORE; ++f) {
      add(check[f]);
      if(!create) add(labels[f]);
    }

    final BaseXBack b2 = new BaseXBack(new TableLayout(1, 2, 8, 0));
    b2.add(check[F_SCORE]);
    scoring = new BaseXCombo(d, DOCUMENTS, TEXT_NODES);
    b2.add(scoring);
    add(b2);
    if(!create) add(labels[F_SCORE]);

    add(check[F_STOP]);
    check[F_STOP].setBorder(new EmptyBorder(0, 0, 4, 0));
    final BaseXBack b3 = new BaseXBack(new TableLayout(1, 2, 8, 0));
    swpath = new BaseXTextField(sw.isEmpty() ?
        d.gui.gprop.get(GUIProp.STOPPATH) : sw, d);
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
   */
  void action() {
    /*
    for(int f = 0; f < check.length; ++f) {
      check[f].setEnabled(ftx);
      if(labels[f] != null) labels[f].setEnabled(ftx);
    }
    */
    language.setEnabled(check[F_LANG].isSelected());
    scoring.setEnabled(check[F_SCORE].isSelected());
    swbrowse.setEnabled(check[F_STOP].isSelected());
    swpath.setEnabled(check[F_STOP].isSelected());

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
    gui.set(Prop.WILDCARDS, check[F_WILD].isSelected());
    gui.set(Prop.SCORING, check[F_SCORE].isSelected() ?
        scoring.getSelectedIndex() + 1 : 0);
    gui.set(Prop.STOPWORDS, check[F_STOP].isSelected() ? swpath.getText() : "");
  }
}
