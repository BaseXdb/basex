package org.basex.io.parse.csv;

import org.basex.build.csv.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * This class converts CSV data to XML, using direct or attributes conversion.
 *
 * @author BaseX Team, BSD License
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
   * @param copts CSV options
   */
  CsvDirectConverter(final CsvParserOptions copts) {
    super(copts);
  }

  @Override
  protected void record() {
    finishRecord();
    record = FElem.build(Q_RECORD);
    col = -1;
  }

  @Override
  protected void header(final byte[] value) {
    headers.add(shared.token(attributes ? value : XMLToken.encode(value, lax)));
  }

  @Override
  protected void entry(final byte[] value) {
    ++col;
    if(skipEmpty && value.length == 0) return;

    final byte[] name = headers.get(col);
    final FBuilder elem;
    if(attributes) {
      elem = FElem.build(Q_ENTRY).add(Q_NAME, name);
    } else {
      elem = FElem.build(name != null ? shared.qName(name) : Q_ENTRY);
    }
    record.add(elem.add(shared.token(value)));
  }

  @Override
  protected void init(final String uri) {
    doc = FDoc.build(Token.token(uri));
    root = FElem.build(Q_CSV);
  }

  @Override
  protected FNode finish(final InputInfo ii, final QueryContext qc) {
    finishRecord();
    return doc.add(root).finish();
  }

  /**
   * Finishes a record.
   */
  private void finishRecord() {
    if(record != null) root.add(record);
  }
}
