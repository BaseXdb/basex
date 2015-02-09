package org.basex.io.parse.csv;

import org.basex.build.csv.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class converts CSV data to XML, using direct or attributes conversion.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class CsvStringConverter extends CsvConverter {
  /** XML string. */
  private final XMLBuilder xml = new XMLBuilder();
  /** Record. */
  private boolean record;

  /**
   * Constructor.
   * @param opts CSV options
   */
  public CsvStringConverter(final CsvParserOptions opts) {
    super(opts);
    xml.open(CSV);
  }

  @Override
  protected void record() {
    if(record) xml.close();
    xml.open(RECORD);
    record = true;
    col = 0;
  }

  @Override
  protected void header(final byte[] value) {
    headers.add(ats ? value : XMLToken.encode(value, lax));
  }

  @Override
  protected void entry(final byte[] entry) {
    final byte[] elem = ENTRY, name = headers.get(col++);
    if(ats) {
      if(name == null) xml.open(elem);
      else xml.open(elem, NAME, name);
    } else {
      xml.open(name != null ? name : elem);
    }
    xml.text(entry);
    xml.close();
  }

  @Override
  protected Str finish() {
    return Str.get(xml.finish());
  }
}
