package org.basex.io.parse.csv;

import static org.basex.util.Token.*;

import org.basex.build.*;
import org.basex.build.CsvOptions.CsvFormat;
import org.basex.io.parse.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class converts CSV data to XML, using direct or attributes conversion.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class CsvStringConverter extends CsvConverter {
  /** CSV token. */
  private static final byte[] CSV = token("csv");
  /** CSV token. */
  private static final byte[] RECORD = token("record");
  /** CSV token. */
  private static final byte[] ENTRY = token("entry");
  /** CSV token. */
  private static final byte[] NAME = token("name");

  /** Headers. */
  private final TokenList headers = new TokenList();
  /** Attributes format. */
  private final boolean atts;
  /** Lax QName conversion. */
  private final boolean lax;

  /** XML string. */
  private final XmlTokenBuilder xml = new XmlTokenBuilder();
  /** Record. */
  private boolean record;
  /** Current column. */
  private int col;

  /**
   * Constructor.
   * @param opts CSV options
   */
  public CsvStringConverter(final CsvParserOptions opts) {
    super(opts);
    lax = opts.get(CsvOptions.LAX);
    atts = opts.get(CsvOptions.FORMAT) == CsvFormat.ATTRIBUTES;
    xml.openElement(CSV);
  }

  @Override
  public void record() {
    if(record) xml.closeElement(RECORD);
    xml.openElement(RECORD);
    record = true;
    col = 0;
  }

  @Override
  public void header(final byte[] value) {
    headers.add(atts ? value : XMLToken.encode(value, lax));
  }

  @Override
  public void entry(final byte[] entry) {
    final byte[] name = headers.get(col++);
    byte[] elem = ENTRY, attr = null;
    if(atts) {
      attr = name;
    } else if(name != null) {
      elem = name;
    }
    xml.openElement(elem, NAME, attr);
    xml.addText(entry);
    xml.closeElement(elem);
  }

  @Override
  public Str finish() {
    if(record) xml.closeElement(RECORD);
    xml.closeElement(CSV);
    return Str.get(xml.finish());
  }
}
