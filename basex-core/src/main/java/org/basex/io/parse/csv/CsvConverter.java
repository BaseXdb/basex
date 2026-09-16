package org.basex.io.parse.csv;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.core.jobs.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.parse.*;
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

  /** Input stream. */
  protected TextInput ti;
  /** Current column. */
  protected int col = -1;

  /**
   * Returns a CSV converter for the given configuration.
   * @param copts options
   * @return CSV converter
   */
  public static CsvConverter get(final CsvParserOptions copts) {
    return get(copts, null);
  }

  /**
   * Returns a CSV converter for the given configuration.
   * @param copts options
   * @param handler target of XML events (can be {@code null}: nodes will be built)
   * @return CSV converter
   */
  public static CsvConverter get(final CsvParserOptions copts, final XmlHandler handler) {
    return switch(copts.get(CsvOptions.FORMAT)) {
      case W3        -> new CsvW3Converter(copts);
      case W3_ARRAYS -> new CsvW3ArraysConverter(copts);
      case W3_XML    -> new CsvW3XmlConverter(copts, handler);
      default        -> new CsvDirectConverter(copts, handler);
    };
  }

  /**
   * Constructor.
   * @param copts CSV options
   */
  protected CsvConverter(final CsvParserOptions copts) {
    this.copts = copts;
    lax = copts.get(CsvOptions.LAX);
    attributes = copts.get(CsvOptions.FORMAT) == CsvFormat.ATTRIBUTES;
    if(copts.header() == null) {
      try {
        for(final Item name : copts.get(CsvOptions.HEADER)) header(name.string(null));
      } catch(final QueryException ex) {
        throw Util.notExpected(ex);
      }
    }
  }

  /**
   * Converts the specified input to an XQuery value.
   * @param input input
   * @throws QueryException query exception
   * @throws IOException I/O exception
   * @return result
   */
  public final Value convert(final IO input) throws QueryException, IOException {
    try(NewlineInput in = new NewlineInput(input, copts.get(CsvParserOptions.ENCODING))) {
      return convert(in, input.url(), null, null);
    }
  }

  /**
   * Converts the specified input to an XQuery value.
   * @param input input
   * @param uri uri (can be empty)
   * @param ii input info (can be {@code null})
   * @param qc query context (if {@code null}, result may lack XQuery-specific contents)
   * @return result
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public final Value convert(final TextInput input, final String uri, final InputInfo ii,
      final QueryContext qc) throws QueryException, IOException {
    ti = input;
    init(uri);
    new CsvParser(input, copts, this).parse(ii);
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
   * @throws IOException I/O exception
   */
  protected abstract void init(String uri) throws IOException;

  /**
   * Returns the resulting XQuery value.
   * @param ii input info (can be {@code null})
   * @param qc query context (if {@code null}, result may lack XQuery-specific contents)
   * @return result, or {@code null}
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  protected abstract Value finish(InputInfo ii, QueryContext qc)
      throws QueryException, IOException;
}
