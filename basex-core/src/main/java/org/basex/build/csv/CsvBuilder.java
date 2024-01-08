package org.basex.build.csv;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.build.*;
import org.basex.io.parse.csv.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class converts CSV data to XML, using direct or attributes conversion.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
final class CsvBuilder extends CsvConverter {
  /** Attributes. */
  private final Atts atts = new Atts();
  /** Namespaces. */
  private final Atts nsp = new Atts();
  /** Builder. */
  private final Builder builder;
  /** Current line. */
  private int line;

  /**
   * Constructor.
   * @param copts CSV options
   * @param builder builder
   * @throws IOException I/O exception
   */
  CsvBuilder(final CsvParserOptions copts, final Builder builder) throws IOException {
    super(copts);
    this.builder = builder;
    builder.openElem(Q_CSV.string(), atts, nsp);
  }

  @Override
  public void record() throws IOException {
    finishRecord();
    builder.openElem(Q_RECORD.string(), atts, nsp);
    column = -1;
    line++;
  }

  @Override
  public void header(final byte[] value) {
    headers.add(attributes ? value : XMLToken.encode(value, lax));
  }

  @Override
  public void entry(final byte[] value) throws IOException {
    ++column;
    if(skipEmpty && value.length == 0) return;

    final byte[] elem = Q_ENTRY.string(), name = headers.get(column);
    if(attributes) {
      if(name == null) {
        builder.openElem(elem, atts, nsp);
      } else {
        atts.add(Q_NAME.string(), name);
        builder.openElem(elem, atts, nsp);
        atts.reset();
      }
    } else {
      builder.openElem(name != null ? name : elem, atts, nsp);
    }
    builder.text(value);
    builder.closeElem();
  }

  @Override
  protected void init(final String uri) {
  }

  @Override
  protected Str finish() throws IOException {
    finishRecord();
    builder.closeElem();
    return null;
  }

  @Override
  public String detailedInfo() {
    return Util.info(LINE_X, line);
  }

  @Override
  public double progressInfo() {
    return (double) nli.size() / nli.length();
  }

  /**
   * Finishes a record.
   * @throws IOException I/O exception
   */
  private void finishRecord() throws IOException {
    if(column >= 0) builder.closeElem();
  }
}
