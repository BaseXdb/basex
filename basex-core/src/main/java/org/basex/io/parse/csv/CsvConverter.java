package org.basex.io.parse.csv;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.value.item.*;
import org.basex.util.list.*;

/**
 * <p>This class converts CSV input to XML.</p>
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class CsvConverter extends Proc {
  /** CSV token. */
  public static final byte[] CSV = token("csv");
  /** CSV token. */
  public static final byte[] RECORD = token("record");
  /** CSV token. */
  public static final byte[] ENTRY = token("entry");
  /** CSV token. */
  public static final byte[] NAME = token("name");

  /** Headers. */
  protected final TokenList headers = new TokenList(1);
  /** Attributes format. */
  protected final boolean ats;
  /** Lax QName conversion. */
  protected final boolean lax;
  /** Current column. */
  protected int col;
  /** CSV options. */
  private final CsvParserOptions copts;
  /** Current input. */
  protected NewlineInput nli;

  /**
   * Constructor.
   * @param copts json options
   */
  protected CsvConverter(final CsvParserOptions copts) {
    this.copts = copts;
    lax = copts.get(CsvOptions.LAX);
    ats = copts.get(CsvOptions.FORMAT) == CsvFormat.ATTRIBUTES;
  }

  /**
   * Converts the specified input to XML.
   * @param input input
   * @return result
   * @throws IOException I/O exception
   */
  public Item convert(final IO input) throws IOException {
    try(final NewlineInput in = new NewlineInput(input)) {
      nli = in;
      CsvParser.parse(in.encoding(copts.get(CsvParserOptions.ENCODING)), copts, this);
    }
    return finish();
  }

  /**
   * Returns a CSV converter for the given configuration.
   * @param copts options
   * @return CSV converter
   */
  public static CsvConverter get(final CsvParserOptions copts) {
    switch(copts.get(CsvOptions.FORMAT)) {
      case MAP: return new CsvMapConverter(copts);
      default:  return new CsvDirectConverter(copts);
    }
  }

  /**
   * Adds a new header.
   * @param string string
   */
  protected abstract void header(final byte[] string);

  /**
   * Adds a new record.
   * @throws IOException I/O exception
   */
  protected abstract void record() throws IOException;

  /**
   * Called when an entry is encountered.
   * @param value string
   * @throws IOException I/O exception
   */
  protected abstract void entry(final byte[] value) throws IOException;

  /**
   * Returns the resulting byte array.
   * @return result
   * @throws IOException I/O exception
   */
  protected abstract Item finish() throws IOException;
}
