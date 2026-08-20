package org.basex.io.serial.csv;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.io.parse.csv.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class serializes items as CSV.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class CsvSerializer extends StandardSerializer {
  /** CSV options. */
  final CsvOptions copts;
  /** Separator. */
  final int separator;
  /** Generate quotes. */
  final boolean quotes;
  /** Generate backslashes. */
  final boolean backslashes;
  /** Quote character (see {@link CsvOptions#QUOTE_CHARACTER}). */
  private final int quoteCharacter;
  /** Select columns (see {@link CsvOptions#SELECT_COLUMNS}). */
  private final int[] selectColumns;
  /** Maximum select columns value. */
  private int maxCol;
  /** Header flag. */
  boolean header;

  /**
   * Returns a CSV serializer for the given serialization options.
   * @param os output stream reference
   * @param so serialization options
   * @return serializer
   * @throws IOException I/O exception
   */
  public static Serializer get(final OutputStream os, final SerializerOptions so)
      throws IOException {
    return switch(so.get(SerializerOptions.CSV).get(CsvOptions.FORMAT)) {
      case XQUERY    -> new CsvXQuerySerializer(os, so); // deprecated
      case W3        -> new CsvW3Serializer(os, so);
      case W3_ARRAYS -> new CsvW3ArraysSerializer(os, so);
      case W3_XML    -> new CsvW3XmlSerializer(os, so);
      default        -> new CsvDirectSerializer(os, so);
    };
  }

  /**
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @throws IOException I/O exception
   */
  CsvSerializer(final OutputStream os, final SerializerOptions sopts) throws IOException {
    super(os, sopts);
    copts = sopts.get(SerializerOptions.CSV);
    quotes = copts.get(CsvOptions.QUOTES);
    backslashes = copts.get(CsvOptions.BACKSLASHES);
    separator = character(sopts.get(SerializerOptions.CSV_SEPARATOR),
        SerializerOptions.CSV_SEPARATOR.name(), copts.separator());
    quoteCharacter = character(sopts.get(SerializerOptions.CSV_QUOTE_CHARACTER),
        SerializerOptions.CSV_QUOTE_CHARACTER.name(), copts.quoteCharacter());
    if(separator == quoteCharacter) throw SERPARAM_X.getIO(Util.info(
        "Parameters '%' and '%' must differ.", SerializerOptions.CSV_SEPARATOR.name(),
        SerializerOptions.CSV_QUOTE_CHARACTER.name()));
    selectColumns = copts.get(CsvOptions.SELECT_COLUMNS);
    maxCol = -1;
    for(final int col : selectColumns) {
      if(col > maxCol) maxCol = col;
    }
    final Value hdr = copts.get(CsvOptions.HEADER);
    if(Types.BOOLEAN_O.instance(hdr)) {
      header = ((Bln) hdr).bool(null);
    } else if(hdr instanceof final Str str) {
      final Boolean b = Strings.toBoolean(string(str.string()));
      if(b != null) header = b;
    }
    if(sopts.yes(SerializerOptions.CSV_HEADER)) header = true;
  }

  /**
   * Returns the character of a single-character serialization parameter.
   * @param value parameter value (can be {@code null})
   * @param name parameter name
   * @param fallback fallback character
   * @return character
   * @throws QueryIOException query I/O exception
   */
  private static int character(final String value, final String name, final int fallback)
      throws QueryIOException {
    if(value == null) return fallback;
    if(value.codePointCount(0, value.length()) == 1) {
      final int cp = value.codePointAt(0);
      if(cp != '\n') return cp;
    }
    throw SERPARAM_X.getIO(Util.info("Invalid value of '%' parameter: '%'.", name, value));
  }

  /**
   * Prints a record with the specified entries.
   * @param entries record entries to be printed (will be reset after serialization)
   * @throws IOException I/O exception
   */
  final void record(final TokenList entries) throws IOException {
    record(entries, true);
  }

  /**
   * Prints a record with the specified entries.
   * @param entries record entries to be printed
   * @param reset whether to reset the entries after serialization
   * @throws IOException I/O exception
   */
  final void record(final TokenList entries, final boolean reset) throws IOException {
    int f = 0;
    if(maxCol < 0) {
      for(final byte[] val : entries) field(f++, val);
    } else {
      final byte[][] row = new byte[maxCol][];
      int i = 0;
      for(final byte[] val : entries) {
        if(i == selectColumns.length) break;
        final int j = selectColumns[i++] - 1;
        if(row[j] == null) row[j] = val;
      }
      for(final byte[] val : row) {
        field(f++, val == null ? Token.EMPTY : val);
      }
    }
    out.print('\n');
    if(reset) entries.reset();
  }

  /**
   * Prints a field value.
   * @param seqNo field sequence number
   * @param value field value (can be {@code null})
   * @throws IOException I/O exception
   */
  final void field(final int seqNo, final byte[] value) throws IOException {
    // print fields, skip trailing empty contents
    if(seqNo != 0) out.print(separator);

    byte[] txt = value != null ? value : Token.EMPTY;
    if(form != null) txt = normalize(txt, form);
    final boolean delim = contains(txt, separator) || contains(txt, '\n');
    final boolean special = contains(txt, '\r') || contains(txt, quoteCharacter)
        || backslashes && contains(txt, '\t');
    if(delim || special || backslashes && contains(txt, '\\')) {
      final TokenBuilder tb = new TokenBuilder();
      if(delim && !backslashes && !quotes)
        throw CSV_SERIALIZE_X_X.getIO("Output must be put into quotes", txt);

      if(quotes && (delim || special)) tb.add(quoteCharacter);
      final TokenParser tp = new TokenParser(txt);
      while(tp.more()) {
        final int cp = tp.next();
        if(backslashes) {
          if(cp == '\n') tb.add("\\").add(separator == '\n' ? "n" : cp);
          else if(cp == '\r') tb.add("\\r");
          else if(cp == '\t') tb.add("\\t");
          else if(cp == quoteCharacter) tb.add("\\").add(cp);
          else if(cp == '\\') tb.add("\\\\");
          else if(cp == separator && !quotes) tb.add('\\').add(cp);
          else tb.add(cp);
        } else if(cp == '\r') {
          // line endings are replaced by the string that separates records
          tp.consume('\n');
          tb.add('\n');
        } else {
          if(cp == quoteCharacter) tb.add(quoteCharacter);
          tb.add(cp);
        }
      }
      if(quotes && (delim || special)) tb.add(quoteCharacter);
      txt = tb.finish();
    }
    printChars(txt);
  }

  /**
   * Serializes a map that conforms to the result format of fn:parse-csv.
   * @param map map
   * @throws IOException I/O exception
   */
  final void w3(final XQMap map) throws IOException {
    final TokenList tl = new TokenList();
    try {
      // print header
      if(header) {
        final Value columns = map.getOrNull(CsvConverter.COLUMNS);
        if(columns == null) throw SERCSV_X.getIO("Map has no 'columns' key");
        row(columns, tl);
      }
      // print rows
      final Value rows = map.getOrNull(CsvConverter.ROWS);
      if(rows == null) throw SERCSV_X.getIO("Map has no 'rows' key");
      for(final Item record : rows) {
        if(!(record instanceof final XQArray array)) throw typeError("Array", record);
        row(array.members(), tl);
      }
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  /**
   * Serializes an array that conforms to the result format of fn:csv-to-arrays.
   * @param array array
   * @throws IOException I/O exception
   */
  final void w3(final XQArray array) throws IOException {
    try {
      row(array.members(), new TokenList());
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  /**
   * Serializes a single line (header or contents).
   * @param line line to be serialized
   * @param tl token list
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private void row(final Iterable<? extends Value> line, final TokenList tl)
      throws QueryException, IOException {
    for(final Value value : line) {
      if(!(value instanceof final Item item) || item instanceof XNode || item instanceof FItem)
        throw typeError("Single atomic item", value);
      tl.add(item.string(null));
    }
    record(tl);
  }

  /**
   * Returns a type error.
   * @param expected expected type
   * @param found found value
   * @return error
   */
  static QueryIOException typeError(final String expected, final Value found) {
    return SERCSV_X_X.getIO(expected + " expected, " + found.seqType() + " found ", found);
  }

  @Override
  protected void atomic(final Item value) throws IOException {
    throw SERCSV_X.getIO("Atomic items cannot be serialized");
  }
}
