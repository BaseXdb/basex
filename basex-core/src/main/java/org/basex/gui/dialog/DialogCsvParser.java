package org.basex.gui.dialog;

import static org.basex.core.Text.*;

import java.awt.*;
import java.io.*;
import java.util.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.gui.*;
import org.basex.gui.layout.*;
import org.basex.gui.text.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * CSV parser panel.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class DialogCsvParser extends DialogParser {
  /** CSV example string. */
  private static final String EXAMPLE = "Name,Born?,Comment\n\"John, Adam\\\",1984,";

  /** Options. */
  private final CsvParserOptions copts;
  /** Example. */
  private final TextPanel example;
  /** Encoding. */
  private final BaseXCombo encoding;
  /** Use header. */
  private final BaseXCheckBox header;
  /** Format. */
  private final BaseXCombo format;
  /** Separator. */
  private final BaseXCombo separator;
  /** Lax name conversion. */
  private final BaseXCheckBox lax;
  /** Skip empty fields. */
  private final BaseXCheckBox skipEmpty;
  /** Parse quotes. */
  private final BaseXCheckBox quotes;
  /** Backslashes. */
  private final BaseXCheckBox backslashes;

  /**
   * Constructor.
   * @param dialog dialog reference
   * @param opts main options
   */
  DialogCsvParser(final BaseXDialog dialog, final MainOptions opts) {
    copts = new CsvParserOptions(opts.get(MainOptions.CSVPARSER));

    final BaseXBack pp = new BaseXBack(new RowLayout(8));
    BaseXBack p = new BaseXBack(new TableLayout(4, 2, 8, 4));

    p.add(new BaseXLabel(ENCODING + COL, true, true));
    encoding = encoding(dialog, copts.get(CsvParserOptions.ENCODING));
    p.add(encoding);

    final StringList csv = new StringList();
    for(final CsvSep cs : CsvSep.values()) csv.add(cs.toString());
    separator = new BaseXCombo(dialog, csv.finish());
    final String sep = copts.get(CsvOptions.SEPARATOR);
    for(final CsvSep cs : CsvSep.values()) {
      if(String.valueOf(cs.sep).equals(sep)) separator.setSelectedItem(cs.toString());
    }
    p.add(new BaseXLabel(SEPARATOR, true, true));
    p.add(separator);
    pp.add(p);

    p.add(new BaseXLabel(FORMAT + COL, true, true));
    final String[] formats = Arrays.stream(new CsvFormat[] {
      CsvFormat.DIRECT, CsvFormat.ATTRIBUTES, CsvFormat.W3_XML
    }).map(CsvFormat::toString).toArray(String[]::new);
    format = new BaseXCombo(dialog, formats);
    format.setSelectedItem(copts.get(CsvOptions.FORMAT));
    p.add(format);

    p = new BaseXBack(new RowLayout());
    header = new BaseXCheckBox(dialog, FIRST_LINE_HEADER, copts.get(CsvOptions.HEADER) == Bln.TRUE);
    p.add(header);
    quotes = new BaseXCheckBox(dialog, PARSE_QUOTES, CsvOptions.QUOTES, copts);
    p.add(quotes);
    backslashes = new BaseXCheckBox(dialog, BACKSLASHES, CsvOptions.BACKSLASHES, copts);
    p.add(backslashes);
    lax = new BaseXCheckBox(dialog, LAX_NAME_CONVERSION, CsvOptions.LAX, copts);
    p.add(lax);
    skipEmpty = new BaseXCheckBox(dialog, SKIP_EMPTY, CsvParserOptions.SKIP_EMPTY, copts);
    p.add(skipEmpty);
    pp.add(p);

    add(pp, BorderLayout.WEST);

    example = new TextPanel(dialog, false);

    add(example, BorderLayout.CENTER);
    action(true);
  }

  @Override
  boolean action(final boolean active) {
    try {
      final boolean head = header.isSelected();
      format.setEnabled(head);
      lax.setEnabled(head && copts.get(CsvOptions.FORMAT) == CsvFormat.DIRECT);
      skipEmpty.setEnabled(head);

      final Value value = CsvConverter.get(copts).convert(new IOContent(EXAMPLE));
      example.setText(example(MainParser.CSV.name(), EXAMPLE, value));
    } catch(final QueryException | IOException ex) {
      example.setText(error(ex));
    }
    return true;
  }

  @Override
  void update() {
    final String enc = encoding.getSelectedItem();
    copts.set(CsvParserOptions.ENCODING, enc.equals(Strings.UTF8) ? null : enc);
    copts.set(CsvOptions.HEADER, Bln.get(header.isSelected()));
    copts.set(CsvOptions.FORMAT, format.getSelectedItem());
    copts.set(CsvOptions.LAX, lax.isSelected());
    copts.set(CsvOptions.QUOTES, quotes.isSelected());
    copts.set(CsvOptions.BACKSLASHES, backslashes.isSelected());
    copts.set(CsvParserOptions.SKIP_EMPTY, skipEmpty.isSelected());

    final String sep = separator.getText();
    for(final CsvSep cs : CsvSep.values()) {
      if(cs.toString().equals(sep)) copts.set(CsvOptions.SEPARATOR, String.valueOf(cs.sep));
    }
  }

  @Override
  void setOptions(final GUI gui) {
    gui.set(MainOptions.CSVPARSER, copts);
  }
}
