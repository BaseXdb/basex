package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXLayout.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.build.CsvOptions.CsvFormat;
import org.basex.build.CsvOptions.CsvSep;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.gui.*;
import org.basex.gui.editor.*;
import org.basex.gui.layout.*;
import org.basex.io.*;
import org.basex.query.*;
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
  private CsvParserOptions copts;
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
    try {
      copts = new CsvParserOptions(opts.get(MainOptions.CSVPARSER));
    } catch(final BaseXException ex) { copts = new CsvParserOptions(); }

    BaseXBack pp  = new BaseXBack(new TableLayout(2, 1, 0, 8));
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

    String f = "";
    final String s = copts.get(CsvOptions.SEPARATOR);
    if(Token.eq(s, sa)) {
      seps.setSelectedItem(s);
    } else {
      seps.setSelectedIndex(sa.length);
      final int ch = Token.toInt(s);
      f = ch > 0 ? String.valueOf((char) ch) : "";
    }
    sepchar = new BaseXTextField(f, d);
    sep.add(sepchar);
    BaseXLayout.setWidth(sepchar, 35);

    p.add(new BaseXLabel(SEPARATOR, true, true));
    p.add(sep);

    p.add(new BaseXLabel(FORMAT + COL, true, true));
    sl.reset();
    for(final CsvFormat cf : CsvFormat.values()) sl.add(cf.toString());
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
      lax.setEnabled(head && copts.format() == CsvFormat.DIRECT);

      final IO io = CsvParser.toXML(new IOContent(EXAMPLE), copts.toString());
      example.setText(example(DataText.M_CSV.toUpperCase(Locale.ENGLISH), EXAMPLE, io.toString()));
    } catch(final IOException ex) {
      example.setText(error(ex));
    }

    final boolean fixedsep = seps.getSelectedIndex() < CsvSep.values().length;
    sepchar.setEnabled(!fixedsep);
    if(fixedsep) {
      copts.set(CsvOptions.SEPARATOR, seps.getSelectedItem());
      try {
        sepchar.setText(new TokenBuilder().add(copts.separator()).toString());
      } catch(final QueryIOException ex) {
        Util.notexpected("Assigned string should equal pre-defined separators.");
      }
    }
    return fixedsep || sepchar.getText().length() == 1;
  }

  @Override
  void update() {
    final String enc = encoding.getSelectedItem();
    copts.set(CsvParserOptions.ENCODING, enc.equals(Token.UTF8) ? null : enc);
    tooltip(copts, CsvParserOptions.ENCODING, encoding);
    copts.set(CsvOptions.HEADER, header.isSelected());
    copts.set(CsvOptions.SEPARATOR, seps.getSelectedIndex() <
      CsvSep.values().length ? seps.getSelectedItem() : sepchar.getText());
    tooltip(copts, CsvOptions.SEPARATOR, seps);
    copts.set(CsvOptions.FORMAT, format.getSelectedItem());
    tooltip(copts, CsvOptions.FORMAT, format);
    copts.set(CsvOptions.LAX, lax.isSelected());
  }

  @Override
  void setOptions(final GUI gui) {
    gui.set(MainOptions.CSVPARSER, copts.toString());
  }
}
