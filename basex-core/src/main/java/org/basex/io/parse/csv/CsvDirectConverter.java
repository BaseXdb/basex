package org.basex.io.parse.csv;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.io.parse.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * This class converts CSV data to XML, using the direct or attributes format.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class CsvDirectConverter extends CsvXmlConverter {
  /** Element name. */
  private static final byte[] E_CSV = token("csv");
  /** Element name. */
  private static final byte[] E_RECORD = token("record");
  /** Element name. */
  private static final byte[] E_ENTRY = token("entry");
  /** Attribute name. */
  private static final byte[] NAME = token("name");

  /** Record element is open. */
  private boolean open;

  /**
   * Constructor.
   * @param copts CSV options
   * @param handler target of XML events (can be {@code null}: nodes will be built)
   */
  CsvDirectConverter(final CsvParserOptions copts, final XmlHandler handler) {
    super(copts, handler);
  }

  @Override
  protected void init(final String uri) throws IOException {
    super.init(uri);
    handler.openElem(E_CSV, atts, NO_NSP);
    open = false;
  }

  @Override
  protected void header(final byte[] value) {
    headers.add(attributes ? value : XMLToken.encode(value, lax));
  }

  @Override
  protected void record() throws IOException {
    line++;
    closeRecord();
    handler.openElem(E_RECORD, atts, NO_NSP);
    open = true;
    col = -1;
  }

  @Override
  protected void entry(final byte[] value) throws IOException {
    ++col;
    final byte[] name = headers.get(col);
    if(attributes) {
      if(name != null) atts.add(NAME, name);
      handler.openElem(E_ENTRY, atts, NO_NSP);
      atts.reset();
    } else {
      handler.openElem(name != null ? name : E_ENTRY, atts, NO_NSP);
    }
    handler.text(value);
    handler.closeElem();
  }

  @Override
  protected Value finish(final InputInfo ii, final QueryContext qc) throws IOException {
    closeRecord();
    handler.closeElem();
    return super.finish(ii, qc);
  }

  /**
   * Closes an open record element.
   * @throws IOException I/O exception
   */
  private void closeRecord() throws IOException {
    if(open) {
      handler.closeElem();
      open = false;
    }
  }
}
