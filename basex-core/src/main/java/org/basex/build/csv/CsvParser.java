package org.basex.build.csv;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;

/**
 * This class parses files in the CSV format and converts them to XML.
 *
 * <p>The parser provides some options, which can be specified via the
 * {@link MainOptions#CSVPARSER} option.</p>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CsvParser extends SingleParser {
  /** CSV Parser options. */
  private final CsvParserOptions copts;
  /** CSV Builder. */
  private CsvBuilder csv;

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
    csv = pushJob(new CsvBuilder(copts, builder));
    try {
      csv.convert(source);
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
