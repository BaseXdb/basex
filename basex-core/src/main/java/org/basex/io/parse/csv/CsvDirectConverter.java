package org.basex.io.parse.csv;

import org.basex.build.csv.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * This class converts CSV data to XML, using direct or attributes conversion.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
final class CsvDirectConverter extends CsvConverter {
  /** Document node. */
  private FBuilder doc;
  /** Root node. */
  private FBuilder root;
  /** Record node. */
  private FBuilder record;

  /**
   * Constructor.
   * @param opts CSV options
   */
  CsvDirectConverter(final CsvParserOptions opts) {
    super(opts);
  }

  @Override
  protected void record() {
    if(record != null) root.add(record);
    record = FElem.build(Q_RECORD);
    col = 0;
  }

  @Override
  protected void header(final byte[] value) {
    headers.add(shared.token(ats ? value : XMLToken.encode(value, lax)));
  }

  @Override
  protected void entry(final byte[] value) {
    final byte[] name = headers.get(col++);
    final FBuilder elem;
    if(ats) {
      elem = FElem.build(Q_ENTRY).add(Q_NAME, name);
    } else {
      elem = FElem.build(name != null ? shared.qname(name) : Q_ENTRY);
    }
    record.add(elem.add(shared.token(value)));
  }

  @Override
  protected void init(final String uri) {
    doc = FDoc.build(Token.token(uri));
    root = FElem.build(Q_CSV);
  }

  @Override
  protected FNode finish() {
    if(record != null) root.add(record);
    return doc.add(root).finish();
  }
}
