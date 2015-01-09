package org.basex.io.parse.csv;

import org.basex.build.csv.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * This class converts CSV data to XML, using direct or attributes conversion.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
final class CsvDirectConverter extends CsvConverter {
  /** Root node. */
  private final FElem root = new FElem(CSV);
  /** Document node. */
  private final FDoc doc = new FDoc().add(root);
  /** Record. */
  private FElem record;

  /**
   * Constructor.
   * @param opts CSV options
   */
  CsvDirectConverter(final CsvParserOptions opts) {
    super(opts);
  }

  @Override
  protected void record() {
    record = new FElem(RECORD);
    root.add(record);
    col = 0;
  }

  @Override
  protected void header(final byte[] value) {
    headers.add(ats ? value : XMLToken.encode(value, lax));
  }

  @Override
  protected void entry(final byte[] entry) {
    final byte[] name = headers.get(col++);
    final FElem e;
    if(ats) {
      e = new FElem(ENTRY);
      if(name != null) e.add(NAME, name);
    } else {
      e = new FElem(name == null ? ENTRY : name);
    }
    record.add(e.add(entry));
  }

  @Override
  protected FDoc finish() {
    return doc;
  }
}
