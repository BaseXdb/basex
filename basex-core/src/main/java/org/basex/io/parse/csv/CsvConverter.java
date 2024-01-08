package org.basex.io.parse.csv;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.util.list.*;

/**
 * <p>This class converts CSV input to XML.</p>
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class CsvConverter extends Job {
  /** QName. */
  protected static final QNm Q_CSV = new QNm("csv");
  /** QName. */
  protected static final QNm Q_RECORD = new QNm("record");
  /** QName. */
  protected static final QNm Q_ENTRY = new QNm("entry");
  /** QName. */
  protected static final QNm Q_NAME = new QNm("name");

  /** Shared data references. */
  protected final SharedData shared = new SharedData();
  /** Headers. */
  protected final TokenList headers = new TokenList(1);
  /** Attributes format. */
  protected final boolean attributes;
  /** Lax QName conversion. */
  protected final boolean lax;
  /** Skip empty fields. */
  protected final boolean skipEmpty;

  /** Current input. */
  protected NewlineInput nli;
  /** Current column. */
  protected int column = -1;

  /** CSV options. */
  private final CsvParserOptions copts;

  /**
   * Constructor.
   * @param copts CSV options
   */
  protected CsvConverter(final CsvParserOptions copts) {
    this.copts = copts;
    lax = copts.get(CsvOptions.LAX);
    attributes = copts.get(CsvOptions.FORMAT) == CsvFormat.ATTRIBUTES;
    skipEmpty = copts.get(CsvParserOptions.SKIP_EMPTY) && copts.get(CsvOptions.HEADER);
  }

  /**
   * Converts the specified input to an XQuery value.
   * @param input input
   * @return result
   * @throws IOException I/O exception
   */
  public final Item convert(final IO input) throws IOException {
    init(input.url());
    try(NewlineInput in = new NewlineInput(input)) {
      nli = in.encoding(copts.get(CsvParserOptions.ENCODING));
      new CsvParser(in, copts, this).parse();
    }
    return finish();
  }

  /**
   * Returns a CSV converter for the given configuration.
   * @param copts options
   * @return CSV converter
   */
  public static CsvConverter get(final CsvParserOptions copts) {
    return copts.get(CsvOptions.FORMAT) == CsvFormat.XQUERY ?
      new CsvXQueryConverter(copts) : new CsvDirectConverter(copts);
  }

  /**
   * Adds a new header.
   * @param string string
   */
  protected abstract void header(byte[] string);

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
  protected abstract void entry(byte[] value) throws IOException;

  /**
   * Initializes the conversion.
   * @param uri base URI
   */
  protected abstract void init(String uri);

  /**
   * Returns the resulting XQuery value.
   * @return result
   * @throws IOException I/O exception
   */
  protected abstract Item finish() throws IOException;
}
