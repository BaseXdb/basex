package org.basex.build.csv;

import java.io.*;

import org.basex.build.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * This class parses files in the CSV format and converts them to XML.
 *
 * <p>The parser provides some options, which can be specified via the
 * {@link MainOptions#CSVPARSER} option.</p>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CsvParser extends SingleParser {
  /** CSV Parser options. */
  private final CsvParserOptions copts;
  /** CSV converter (can be {@code null}). */
  private CsvConverter csv;

  /**
   * Constructor.
   * @param source document source
   * @param options database options
   * @param copts parser options
   */
  public CsvParser(final IO source, final MainOptions options, final CsvParserOptions copts) {
    super(source, options);
    this.copts = copts;
  }

  @Override
  protected void parse() throws IOException {
    final CsvFormat format = copts.get(CsvOptions.FORMAT);
    if(format == CsvFormat.W3 || format == CsvFormat.W3_ARRAYS) {
      throw new IOException(Util.info("Format '%' cannot be converted to XML.", format));
    }
    csv = pushJob(CsvConverter.get(copts, builder));
    try {
      csv.convert(source);
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    } finally {
      popJob();
    }
  }

  @Override
  public String detailedInfo() {
    return csv != null ? csv.detailedInfo() : super.detailedInfo();
  }

  @Override
  public double progressInfo() {
    return csv != null ? csv.progressInfo() : super.progressInfo();
  }
}
