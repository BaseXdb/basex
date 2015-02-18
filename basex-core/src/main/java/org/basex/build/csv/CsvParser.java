package org.basex.build.csv;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.parse.csv.*;
import org.basex.query.value.item.*;

/**
 * This class parses files in the CSV format and converts them to XML.
 *
 * <p>The parser provides some options, which can be specified via the
 * {@link MainOptions#CSVPARSER} option.</p>
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class CsvParser extends SingleParser {
  /** CSV Parser options. */
  private final CsvParserOptions copts;

  /**
   * Constructor.
   * @param source document source
   * @param opts database options
   */
  public CsvParser(final IO source, final MainOptions opts) {
    this(source, opts, opts.get(MainOptions.CSVPARSER));
  }

  /**
   * Constructor.
   * @param source document source
   * @param opts database options
   * @param copts parser options
   */
  public CsvParser(final IO source, final MainOptions opts, final CsvParserOptions copts) {
    super(source, opts);
    this.copts = copts;
  }

  @Override
  protected void parse() throws IOException {
    proc(new CsvBuilder(copts, builder)).convert(source);
  }

  /**
   * Converts a JSON document to XML.
   * @param io input
   * @param options parser options
   * @return parser
   * @throws IOException I/O exception
   */
  public static IOContent toXML(final IO io, final CsvParserOptions options) throws IOException {
    return new IOContent(((Str) new CsvStringConverter(options).convert(io)).string());
  }
}
