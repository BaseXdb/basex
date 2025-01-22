package org.basex.io.parse.csv;

import org.basex.build.csv.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * This class converts CSV data to XML according to the rules of fn:csv-to-xml.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class CsvXmlConverter extends CsvConverter {
  /** QName. */
  public static final QNm Q_FN_CSV = new QNm("csv", QueryText.FN_URI);
  /** QName. */
  protected static final QNm Q_FN_ROWS = new QNm("rows", QueryText.FN_URI);
  /** QName. */
  public static final QNm Q_FN_ROW = new QNm("row", QueryText.FN_URI);
  /** QName. */
  protected static final QNm Q_FN_FIELD = new QNm("field", QueryText.FN_URI);
  /** QName. */
  protected static final QNm Q_FN_COLUMNS = new QNm("columns", QueryText.FN_URI);
  /** QName. */
  public static final QNm Q_FN_COLUMN = new QNm("column", QueryText.FN_URI);
  /** QName. */
  public static final QNm Q_COLUMN = new QNm("column");

  /** Document node. */
  private FBuilder doc;
  /** Root node. */
  private FBuilder rows;
  /** Record node. */
  private FBuilder record;

  /**
   * Constructor.
   * @param copts CSV options
   * @throws QueryException query exception
   */
  public CsvXmlConverter(final CsvParserOptions copts) throws QueryException {
    super(copts);
    final TokenSet names = new TokenSet();
    final Value columns = copts.get(CsvOptions.HEADER);
    if(!SeqType.BOOLEAN_O.instance(columns)) {
      for(final Item columnName : columns) {
        final byte[] token = columnName.string(null);
        header(names.add(token) ? token : Token.EMPTY, false);
      }
    }
  }

  @Override
  protected final void record() {
    finishRecord();
    record = FElem.build(Q_FN_ROW);
    column = -1;
  }

  @Override
  public final void header(final byte[] value) {
    header(value, true);
  }

  /**
   * Adds a new header.
   * @param value header value
   * @param trim whether to trim the header value
   */
  public final void header(final byte[] value, final boolean trim) {
    headers.add(shared.token(trim ? Token.trim(value) : value));
  }

  @Override
  protected final void entry(final byte[] value) {
    ++column;
    if(skipEmpty && value.length == 0) return;

    final FBuilder elem = FElem.build(Q_FN_FIELD);
    final byte[] name = headers.get(column);
    if(name != null && name.length > 0) elem.add(Q_COLUMN, name);
    record.add(elem.add(shared.token(value)));
  }

  @Override
  protected final void init(final String uri) {
    doc = FDoc.build(Token.token(uri));
    rows = FElem.build(Q_FN_ROWS);
  }

  @Override
  protected final FNode finish() {
    finishRecord();
    final FBuilder root = FElem.build(Q_FN_CSV);
    if(headers.size() > 0) {
      final FBuilder columns = FElem.build(Q_FN_COLUMNS);
      for(final byte[] h : headers) columns.add(FElem.build(Q_FN_COLUMN).add(h));
      root.add(columns);
    }
    return doc.add(root.add(rows)).finish();
  }

  /**
   * Finishes a record.
   */
  private void finishRecord() {
    if(record != null) rows.add(record);
  }
}
