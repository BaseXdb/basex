package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.basex.core.Prop;
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
  private final BaseXCheckBox[] check = new BaseXCheckBox[6];
  /** Full-text labels. */
  private final BaseXLabel[] labels = new BaseXLabel[6];
  /** Full-text scoring type. */
  private final BaseXCombo scoring;
  /** Path for Full-text stopword list.*/
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
        CREATEWC, CREATESTEM, CREATECS, CREATEDC, CREATESCT, CREATESW };
    final String[] desc = {
        WCINDEXINFO, FTSTEMINFO, FTCSINFO, FTDCINFO, FTSCINFO, FTSWINFO };
    final boolean[] val = {
        prop.is(Prop.WILDCARDS), prop.is(Prop.STEMMING), prop.is(Prop.CASESENS),
        prop.is(Prop.DIACRITICS), prop.num(Prop.SCORING) > 0, !sw.isEmpty() };

    for(int f = 0; f < check.length; f++) {
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
    scoring = new BaseXCombo(new String[] { CREATESCT1, CREATESCT2 }, d);
    b1.add(scoring);
    add(b1);
    if(!create) add(labels[4]);

    add(check[5]);
    final BaseXBack b2 = new BaseXBack();
    b2.setLayout(new TableLayout(1, 2, 6, 0));
    swpath = new BaseXTextField(sw.isEmpty() ?
        d.gui.prop.get(GUIProp.STOPPATH) : sw, d);
    b2.add(swpath);

    swbrowse = new BaseXButton(BUTTONBROWSE, d);
    swbrowse.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) { chooseStop(); }
    });
    b2.add(swbrowse);
    add(b2);
    if(!create) add(labels[5]);
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
    for(int f = 0; f < check.length; f++) {
      check[f].setEnabled(ftx);
      if(labels[f] != null) labels[f].setEnabled(ftx);
    }
    scoring.setEnabled(ftx && check[4].isSelected());
    swbrowse.setEnabled(ftx && check[5].isSelected());
    swpath.setEnabled(ftx && check[5].isSelected());
    final String sw = swpath.getText().trim();
    final IO file = IO.get(sw);
    final boolean exists = !sw.isEmpty() && file.exists();
    if(exists) dialog.gui.prop.set(GUIProp.STOPPATH, sw);
  }

  /**
   * Closes the dialog.
   */
  void close() {
    final Prop prop = dialog.gui.context.prop;
    prop.set(Prop.WILDCARDS, check[0].isSelected());
    prop.set(Prop.STEMMING, check[1].isSelected());
    prop.set(Prop.CASESENS, check[2].isSelected());
    prop.set(Prop.DIACRITICS, check[3].isSelected());
    prop.set(Prop.SCORING,
        check[4].isSelected() ? scoring.getSelectedIndex() + 1 : 0);
    prop.set(Prop.STOPWORDS, check[5].isSelected() ? swpath.getText() : "");
  }
}
