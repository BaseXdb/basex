package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.io.*;

import org.basex.build.*;
import org.basex.build.CsvOptions.CsvFormat;
import org.basex.build.CsvOptions.CsvSep;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.gui.*;
import org.basex.gui.editor.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * CSV parser panel.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
final class DialogCsvParser extends DialogParser {
  /** CSV example string. */
  private static final String EXAMPLE = "Name,Born,X?_\nJohn Adam,1984,";

  /** Options. */
  private final CsvParserOptions copts;
  /** JSON example. */
  private final Editor example;
  /** CSV: encoding. */
  private final BaseXCombo encoding;
  /** CSV: Use header. */
  private final BaseXCheckBox header;
  /** CSV: format. */
  private final BaseXCombo format;
  /** CSV: Separator. */
  private final BaseXCombo seps;
  /** CSV: Separator (numeric). */
  private final BaseXTextField sepchar;
  /** CSV: Lax name conversion. */
  private final BaseXCheckBox lax;

  /**
   * Constructor.
   * @param d dialog reference
   * @param opts main options
   */
  DialogCsvParser(final BaseXDialog d, final MainOptions opts) {
    super(d);
    copts = opts.get(MainOptions.CSVPARSER);

    final BaseXBack pp  = new BaseXBack(new TableLayout(2, 1, 0, 8));
    BaseXBack p = new BaseXBack(new TableLayout(4, 2, 8, 4));

    p.add(new BaseXLabel(ENCODING + COL, true, true));
    encoding = DialogExport.encoding(d, copts.get(CsvParserOptions.ENCODING));
    p.add(encoding);

    final BaseXBack sep = new BaseXBack().layout(new TableLayout(1, 2, 6, 0));
    final StringList sl = new StringList();
    for(final CsvSep cs : CsvSep.values()) sl.add(cs.toString());
    final String[] sa = sl.toArray();
    seps = new BaseXCombo(d, sl.add("").toArray());
    sep.add(seps);

    final String s = copts.get(CsvOptions.SEPARATOR);
    if(Token.eq(s, sa)) {
      seps.setSelectedItem(s);
    } else {
      seps.setSelectedIndex(sa.length);
    }
    sepchar = new BaseXTextField(s, d);
    sep.add(sepchar);
    BaseXLayout.setWidth(sepchar, 35);

    p.add(new BaseXLabel(SEPARATOR, true, true));
    p.add(sep);

    p.add(new BaseXLabel(FORMAT + COL, true, true));
    sl.reset();
    final CsvFormat[] formats = CsvFormat.values();
    final int fl = formats.length - 1;
    for(int f = 0; f < fl; f++) sl.add(formats[f].toString());
    format = new BaseXCombo(d, sl.toArray());
    format.setSelectedItem(copts.get(CsvOptions.FORMAT));
    p.add(format);
    pp.add(p);

    p = new BaseXBack(new TableLayout(2, 1));
    header = new BaseXCheckBox(FIRST_LINE_HEADER, CsvOptions.HEADER, copts, d);
    p.add(header);

    lax = new BaseXCheckBox(LAX_NAME_CONVERSION, CsvOptions.LAX, copts, d);
    p.add(lax);
    pp.add(p);

    add(pp, BorderLayout.WEST);

    example = new Editor(false, d);

    add(example, BorderLayout.CENTER);
    action(true);
  }

  @Override
  boolean action(final boolean active) {
    try {
      final boolean head = header.isSelected();
      format.setEnabled(head);
      lax.setEnabled(head && copts.get(CsvOptions.FORMAT) == CsvFormat.DIRECT);

      final IO io = CsvParser.toXML(new IOContent(EXAMPLE), copts);
      example.setText(example(MainParser.CSV.name(), EXAMPLE, io.toString()));
    } catch(final IOException ex) {
      example.setText(error(ex));
    }

    final boolean fixedsep = seps.getSelectedIndex() < CsvSep.values().length;
    sepchar.setEnabled(!fixedsep);
    if(fixedsep) sepchar.setText(new TokenBuilder().add(copts.separator()).toString());
    return fixedsep || sepchar.getText().length() == 1;
  }

  @Override
  void update() {
    final String enc = encoding.getSelectedItem();
    copts.set(CsvParserOptions.ENCODING, enc.equals(Token.UTF8) ? null : enc);
    copts.set(CsvOptions.HEADER, header.isSelected());
    copts.set(CsvOptions.FORMAT, format.getSelectedItem());
    copts.set(CsvOptions.LAX, lax.isSelected());
    String sep;
    if(seps.getSelectedIndex() < CsvSep.values().length) {
      sep = seps.getSelectedItem();
    } else {
      sep = sepchar.getText();
      for(final CsvSep cs : CsvSep.values()) {
        if(String.valueOf(cs.sep).equals(sep)) sep = cs.toString();
      }
    }
    copts.set(CsvOptions.SEPARATOR, sep);
  }

  @Override
  void setOptions(final GUI gui) {
    gui.set(MainOptions.CSVPARSER, copts);
  }
}
