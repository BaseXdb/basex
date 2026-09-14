package org.basex.io.parse.csv;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.io.parse.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class converts CSV data to XML, using the format defined by fn:csv-to-xml.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CsvW3XmlConverter extends CsvXmlConverter {
  /** QName. */
  public static final QNm Q_FN_ROW = new QNm("row", QueryText.FN_URI);
  /** QName. */
  public static final QNm Q_FN_COLUMN = new QNm("column", QueryText.FN_URI);
  /** QName. */
  public static final QNm Q_COLUMN = new QNm("column");

  /** Namespace declaration of the root element. */
  private static final Atts FN_NSP = new Atts().add(EMPTY, QueryText.FN_URI);
  /** Element name. */
  private static final byte[] E_CSV = token("csv");
  /** Element name. */
  private static final byte[] E_COLUMNS = token("columns");
  /** Element name. */
  private static final byte[] E_COLUMN = token("column");
  /** Element name. */
  private static final byte[] E_ROWS = token("rows");
  /** Element name. */
  private static final byte[] E_ROW = token("row");
  /** Element name. */
  private static final byte[] E_FIELD = token("field");
  /** Attribute name. */
  private static final byte[] COLUMN = token("column");

  /** Rows element is open. */
  private boolean rows;
  /** Row element is open. */
  private boolean open;

  /**
   * Constructor.
   * @param copts CSV options
   * @param handler target of XML events (can be {@code null}: nodes will be built)
   */
  CsvW3XmlConverter(final CsvParserOptions copts, final XmlHandler handler) {
    super(copts, handler);
  }

  @Override
  protected void init(final String uri) throws IOException {
    super.init(uri);
    handler.openElem(E_CSV, atts, FN_NSP);
    rows = false;
    open = false;
  }

  @Override
  protected void header(final byte[] value) {
    headers.add(value);
  }

  @Override
  protected void record() throws IOException {
    line++;
    if(!rows) openRows();
    closeRow();
    handler.openElem(E_ROW, atts, NO_NSP);
    open = true;
    col = -1;
  }

  @Override
  protected void entry(final byte[] value) throws IOException {
    ++col;
    final byte[] name = headers.get(col);
    if(name != null && name.length > 0) atts.add(COLUMN, name);
    handler.openElem(E_FIELD, atts, NO_NSP);
    atts.reset();
    handler.text(value);
    handler.closeElem();
  }

  @Override
  protected Value finish(final InputInfo ii, final QueryContext qc) throws IOException {
    if(!rows) openRows();
    closeRow();
    handler.closeElem();
    handler.closeElem();
    return super.finish(ii, qc);
  }

  /**
   * Adds the column names and opens the rows element.
   * @throws IOException I/O exception
   */
  private void openRows() throws IOException {
    // skip trailing empty column names; omit element if no non-empty names exist
    int size = headers.size();
    while(size > 0 && headers.get(size - 1).length == 0) --size;
    if(size > 0) {
      handler.openElem(E_COLUMNS, atts, NO_NSP);
      for(int h = 0; h < size; h++) {
        handler.openElem(E_COLUMN, atts, NO_NSP);
        handler.text(headers.get(h));
        handler.closeElem();
      }
      handler.closeElem();
    }
    handler.openElem(E_ROWS, atts, NO_NSP);
    rows = true;
  }

  /**
   * Closes an open row element.
   * @throws IOException I/O exception
   */
  private void closeRow() throws IOException {
    if(open) {
      handler.closeElem();
      open = false;
    }
  }
}
