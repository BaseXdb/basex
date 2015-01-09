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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class CsvBuilder extends CsvConverter {
  /** Attributes. */
  private final Atts atts = new Atts();
  /** Namespaces. */
  private final Atts nsp = new Atts();
  /** Record. */
  private boolean record;
  /** Builder. */
  private Builder builder;
  /** Current line. */
  private int line;

  /**
   * Constructor.
   * @param opts CSV options
   * @param builder builder
   * @throws IOException I/O exception
   */
  CsvBuilder(final CsvParserOptions opts, final Builder builder) throws IOException {
    super(opts);
    this.builder = builder;
    builder.openElem(CsvConverter.CSV, atts, nsp);
  }

  @Override
  public void record() throws IOException {
    if(record) builder.closeElem();
    builder.openElem(RECORD, atts, nsp);
    record = true;
    col = 0;
    line++;
  }

  @Override
  public void header(final byte[] value) {
    headers.add(ats ? value : XMLToken.encode(value, lax));
  }

  @Override
  public void entry(final byte[] entry) throws IOException {
    final byte[] elem = ENTRY, name = headers.get(col++);
    if(ats) {
      if(name == null) {
        builder.openElem(elem, atts, nsp);
      } else {
        atts.add(NAME, name);
        builder.openElem(elem, atts, nsp);
        atts.reset();
      }
    } else {
      builder.openElem(name != null ? name : elem, atts, nsp);
    }
    builder.text(entry);
    builder.closeElem();
  }

  @Override
  public Str finish() throws IOException {
    if(record) builder.closeElem();
    builder.closeElem();
    return null;
  }

  @Override
  protected String det() {
    return Util.info(LINE_X, line);
  }

  @Override
  public double prog() {
    return (double) nli.size() / nli.length();
  }
}
