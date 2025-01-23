package org.basex.io.parse.csv;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * <p>This class converts CSV input to XML.</p>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class CsvConverter extends Job {
  /** Columns. */
  public static final Str COLUMNS = Str.get("columns");
  /** Column-index. */
  public static final Str COLUMN_INDEX = Str.get("column-index");
  /** Rows. */
  public static final Str ROWS = Str.get("rows");
  /** Get. */
  public static final Str GET = Str.get("get");

  /** QName. */
  protected static final QNm Q_CSV = new QNm("csv");
  /** QName. */
  protected static final QNm Q_RECORD = new QNm("record");
  /** QName. */
  protected static final QNm Q_ENTRY = new QNm("entry");
  /** QName. */
  protected static final QNm Q_NAME = new QNm("name");

  /** CSV options. */
  protected final CsvParserOptions copts;
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

  /**
   * Returns a CSV converter for the given configuration.
   * @param copts options
   * @return CSV converter
   * @throws QueryException query exception
   */
  public static CsvConverter get(final CsvParserOptions copts) throws QueryException {
    switch(copts.get(CsvOptions.FORMAT)) {
      case XQUERY:    return new CsvXQueryConverter(copts);
      case W3_MAP:    return new CsvW3MapConverter(copts);
      case W3_ARRAYS: return new CsvW3ArraysConverter(copts);
      case W3_XML:    return new CsvW3XmlConverter(copts);
      default:        return new CsvDirectConverter(copts);
    }
  }

  /**
   * Constructor.
   * @param copts CSV options
   */
  protected CsvConverter(final CsvParserOptions copts) {
    this.copts = copts;
    lax = copts.get(CsvOptions.LAX);
    attributes = copts.get(CsvOptions.FORMAT) == CsvFormat.ATTRIBUTES;
    skipEmpty = copts.get(CsvParserOptions.SKIP_EMPTY) && copts.get(CsvOptions.HEADER) != Bln.FALSE;
  }

  /**
   * Converts the specified input to an XQuery value.
   * @param input input
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public final Value convert(final IO input) throws QueryException, IOException {
    return convert(input, null, null);
  }

  /**
   * Converts the specified input to an XQuery value.
   * @param input input
   * @param ii input info (can be {@code null})
   * @param qc query context (if {@code null}, result may lack XQuery-specific contents)
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public final Value convert(final IO input, final InputInfo ii, final QueryContext qc)
      throws QueryException, IOException {
    init(input.url());
    try(NewlineInput in = new NewlineInput(input)) {
      nli = in.encoding(copts.get(CsvParserOptions.ENCODING));
      new CsvParser(in, copts, this).parse(ii);
    }
    return finish(ii, qc);
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
   * @param ii input info (can be {@code null})
   * @param qc query context (if {@code null}, result may lack XQuery-specific contents)
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract Value finish(InputInfo ii, QueryContext qc)
      throws QueryException, IOException;
}
