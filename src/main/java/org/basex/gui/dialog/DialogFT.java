package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumSet;
import org.basex.core.Prop;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IO;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.Language;

/**
 * Full-text creation dialog.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class DialogFT extends BaseXBack {
  /** Dialog reference. */
  private final Dialog dialog;
  /** Full-text indexing. */
  private final BaseXCheckBox[] check = new BaseXCheckBox[7];
  /** Full-text labels. */
  private final BaseXLabel[] labels = new BaseXLabel[7];
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
   * @param create flag
   */
  DialogFT(final Dialog d, final boolean create) {
    dialog = d;
    setLayout(new TableLayout(14, 1));

    final Prop prop = d.gui.context.prop;
    add(new BaseXLabel(FTINDEXINFO, true, false));

    final String sw = prop.get(Prop.STOPWORDS);
    final String[] cb = {
        CREATEWC, CREATESTEM, CREATECS, CREATEDC, CREATELANG,
        CREATESCT, CREATESW };
    final String[] desc = {
        WCINDEXINFO, FTSTEMINFO, FTCSINFO, FTDCINFO, FTLANGINFO,
        FTSCINFO, FTSWINFO };
    final boolean[] val = {
        prop.is(Prop.WILDCARDS), prop.is(Prop.STEMMING), prop.is(Prop.CASESENS),
        prop.is(Prop.DIACRITICS), prop.get(Prop.LANGUAGE).length() != 0,
        prop.num(Prop.SCORING) > 0, !sw.isEmpty() };

    for(int f = 0; f < check.length; ++f) {
      check[f] = new BaseXCheckBox(cb[f], val[f], create ? 1 : 0, d);
      if(!create) {
        labels[f] = new BaseXLabel(desc[f], true, false);
      } else {
        check[f].setToolTipText(desc[f]);
      }
      if(f < check.length - 2) {
        add(check[f]);
        if(!create) add(labels[f]);
      }
    }

    final BaseXBack b1 = new BaseXBack();
    b1.setLayout(new TableLayout(1, 2, 6, 0));
    b1.add(check[4]);
    final EnumSet<Language> langset = FTLexer.languages();
    final String[] langs = new String[langset.size()];
    int i = 0;
    for(final Language l : langset) langs[i++] = l.toString();
    language = new BaseXCombo(langs, d);
    b1.add(language);
    add(b1);
    if(!create) add(labels[4]);

    final BaseXBack b2 = new BaseXBack();
    b2.setLayout(new TableLayout(1, 2, 6, 0));
    b2.add(check[5]);
    scoring = new BaseXCombo(new String[] { CREATESCT1, CREATESCT2}, d);
    b2.add(scoring);
    add(b2);
    if(!create) add(labels[5]);

    add(check[6]);
    final BaseXBack b3 = new BaseXBack();
    b3.setLayout(new TableLayout(1, 2, 6, 0));
    swpath = new BaseXTextField(sw.isEmpty() ? d.gui.prop.get(GUIProp.STOPPATH)
        : sw, d);
    b3.add(swpath);

    swbrowse = new BaseXButton(BUTTONBROWSE, d);
    swbrowse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        chooseStop();
      }
    });
    b3.add(swbrowse);
    add(b3);
    if(!create) add(labels[6]);
  }

  /**
   * Opens a file dialog to choose a stopword list.
   */
  void chooseStop() {
    final GUIProp gprop = dialog.gui.prop;
    final BaseXFileChooser fc = new BaseXFileChooser(CREATETITLE,
        gprop.get(GUIProp.STOPPATH), dialog.gui);
    final IO file = fc.select(BaseXFileChooser.Mode.FOPEN);
    if(file != null) {
      swpath.setText(file.path());
      gprop.set(GUIProp.STOPPATH, file.path());
    }
  }

  /**
   * Reacts on user input.
   * @param ftx full-text flag
   */
  void action(final boolean ftx) {
    for(int f = 0; f < check.length; ++f) {
      check[f].setEnabled(ftx);
      if(labels[f] != null) labels[f].setEnabled(ftx);
    }
    language.setEnabled(ftx && check[4].isSelected());
    scoring.setEnabled(ftx && check[5].isSelected());
    swbrowse.setEnabled(ftx && check[6].isSelected());
    swpath.setEnabled(ftx && check[6].isSelected());
    final String sw = swpath.getText().trim();
    final IO file = IO.get(sw);
    final boolean exists = !sw.isEmpty() && file.exists();
    if(exists) dialog.gui.prop.set(GUIProp.STOPPATH, sw);
  }

  /**
   * Closes the dialog.
   */
  void close() {
    final GUI gui = dialog.gui;
    gui.set(Prop.WILDCARDS, check[0].isSelected());
    gui.set(Prop.STEMMING, check[1].isSelected());
    gui.set(Prop.CASESENS, check[2].isSelected());
    gui.set(Prop.DIACRITICS, check[3].isSelected());
    final String lang = check[4].isSelected() ?
        Language.get(language.getSelectedItem().toString()).name() : "";
    gui.set(Prop.LANGUAGE, lang);
    gui.set(Prop.SCORING, check[5].isSelected() ?
        scoring.getSelectedIndex() + 1 : 0);
    gui.set(Prop.STOPWORDS, check[6].isSelected() ? swpath.getText() : "");
  }
}
