package org.basex.io.serial.csv;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.io.serial.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
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
  final int fieldDelimiter;
  /** Generate quotes. */
  final boolean quotes;
  /** Generate backslashes. */
  final boolean backslashes;
  /** Row delimiter (see {@link CsvOptions#ROW_DELIMITER}). */
  private final int rowDelimiter;
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
    switch(so.get(SerializerOptions.CSV).get(CsvOptions.FORMAT)) {
      case XQUERY:    return new CsvXQuerySerializer(os, so); // deprecated
      case W3:        return new CsvW3Serializer(os, so);
      case W3_ARRAYS: return new CsvW3ArraysSerializer(os, so);
      case W3_XML:    return new CsvW3XmlSerializer(os, so);
      default:        return new CsvDirectSerializer(os, so);
    }
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
    fieldDelimiter = copts.fieldDelimiter();
    rowDelimiter = copts.rowDelimiter();
    quoteCharacter = copts.quoteCharacter();
    selectColumns = copts.get(CsvOptions.SELECT_COLUMNS);
    maxCol = -1;
    for(final int col : selectColumns) {
      if(col > maxCol) maxCol = col;
    }
    final Value hdr = copts.get(CsvOptions.HEADER);
    if(SeqType.BOOLEAN_O.instance(hdr)) {
      header = ((Bln) hdr).bool(null);
    } else if(hdr instanceof final Str str) {
      final Boolean b = Strings.toBoolean(string(str.string()));
      if(b != null) header = b;
    }
  }

  /**
   * Prints a record with the specified entries.
   * @param entries record entries to be printed (will be reset after serialization)
   * @throws IOException I/O exception
   */
  final void record(final TokenList entries) throws IOException {
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
    out.print(rowDelimiter);
    entries.reset();
  }

  /**
   * Prints a field value.
   * @param seqNo field sequence number
   * @param value field value
   * @throws IOException I/O exception
   */
  final void field(final int seqNo, final byte[] value) throws IOException {
    // print fields, skip trailing empty contents
    if(seqNo != 0) out.print(fieldDelimiter);

    byte[] txt = value != null ? value : Token.EMPTY;
    final boolean delim = contains(txt, fieldDelimiter) || contains(txt, rowDelimiter);
    final boolean special = contains(txt, '\r') || contains(txt, '\t')
        || contains(txt, quoteCharacter);
    if(delim || special || backslashes && contains(txt, '\\')) {
      final TokenBuilder tb = new TokenBuilder();
      if(delim && !backslashes && !quotes)
        throw CSV_SERIALIZE_X_X.getIO("Output must be put into quotes", txt);

      if(quotes && (delim || special)) tb.add(quoteCharacter);
      final TokenParser tp = new TokenParser(txt);
      while(tp.more()) {
        final int cp = tp.next();
        if(backslashes) {
          if(cp == '\n') tb.add("\\").add(fieldDelimiter == '\n' ? "n" : cp);
          else if(cp == '\r') tb.add("\\r");
          else if(cp == '\t') tb.add("\\t");
          else if(cp == quoteCharacter) tb.add("\\").add(cp);
          else if(cp == '\\') tb.add("\\\\");
          else if(cp == fieldDelimiter && !quotes) tb.add('\\').add(cp);
          else tb.add(cp);
        } else {
          if(cp == quoteCharacter) tb.add(quoteCharacter);
          tb.add(cp);
        }
      }
      if(quotes && (delim || special)) tb.add(quoteCharacter);
      txt = tb.finish();
    }
    out.print(txt);
  }

  @Override
  protected void atomic(final Item value) throws IOException {
    throw CSV_SERIALIZE_X.getIO("Atomic items cannot be serialized");
  }
}
