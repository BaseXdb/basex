package org.basex.io.serial.csv;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.io.parse.csv.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.fn.FnCsvToArrays.*;
import org.basex.query.func.fn.FnParseCsv.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
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
   * Constructor.
   * @param os output stream
   * @param sopts serialization parameters
   * @param copts csv options
   * @throws IOException I/O exception
   */
  CsvSerializer(final OutputStream os, final SerializerOptions sopts, final CsvOptions copts)
      throws IOException {
    super(os, sopts);
    this.copts = copts;
    quotes = copts.get(CsvOptions.QUOTES);
    backslashes = copts.get(CsvOptions.BACKSLASHES);
    header = copts.get(CsvOptions.HEADER);
    separator = copts.separator();
    rowDelimiter = copts.rowDelimiter();
    quoteCharacter = copts.quoteCharacter();
    selectColumns = copts.get(CsvOptions.SELECT_COLUMNS);
    maxCol = -1;
    for(final int col : selectColumns) if(col > maxCol) maxCol = col;
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
      final byte[][] row = new byte[maxCol + 1][];
      int i = 0;
      for(final byte[] val : entries) {
        final int j = selectColumns[i++] - 1;
        if(row[j] == null) row[j] = val;
      }
      for(final byte[] val : row) field(f++, val == null ? Token.EMPTY : val);
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
    if(seqNo != 0) out.print(separator);

    byte[] txt = value != null ? value : Token.EMPTY;
    final boolean delim = contains(txt, separator) || contains(txt, rowDelimiter);
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
          if(cp == '\n') tb.add("\\").add(separator == '\n' ? "n" : cp);
          else if(cp == '\r') tb.add("\\r");
          else if(cp == '\t') tb.add("\\t");
          else if(cp == quoteCharacter) tb.add("\\").add(cp);
          else if(cp == '\\') tb.add("\\\\");
          else if(cp == separator && !quotes) tb.add('\\').add(cp);
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

  /**
   * This delegator class allows lazy instantiation of the concrete CSV serializer, depending on
   * the item to be serialized.
   */
  public static class Delegator extends Serializer {
    /** Output stream. */
    private final OutputStream os;
    /** Serializer options. */
    private final SerializerOptions so;
    /** Concrete CSV serializer. */
    private CsvSerializer delegate;

    /**
     * Constructor.
     * @param os output stream
     * @param sopts serializer options
     */
    public Delegator(final OutputStream os, final SerializerOptions sopts) {
      this.os = os;
      this.so = sopts;
    }

    @Override
    public void serialize(final Item item) throws IOException {
      if(delegate == null) {
        try {
          final XQMap opts = (XQMap) so.get(SerializerOptions.CSV);
          if(item instanceof FNode) {
            final FElem root;
            if(item instanceof FElem) {
              root = (FElem) item;
            } else if(item instanceof FDoc) {
              final FDoc doc = (FDoc) item;
              root = doc.hasChildren() ? (FElem) doc.childIter().next() : null;
            } else {
              root = null;
            }
            if(root != null && root.qname().eq(CsvXmlConverter.Q_FN_CSV)) {
              final ParseCsvOptions popts = new ParseCsvOptions();
              popts.assign(opts, null);
              popts.validate(null);
              delegate = new CsvXmlSerializer(os, so, popts.toCsvParserOptions());
            } else {
              final CsvParserOptions copts = new CsvParserOptions();
              copts.assign(opts, null);
              delegate = new CsvDirectSerializer(os, so, copts);
            }
          } else if(item instanceof XQArray) {
            final CsvToArraysOptions aopts = new CsvToArraysOptions();
            aopts.assign(opts, null);
            aopts.validate(null);
            delegate = new CsvArraysSerializer(os, so, aopts.toCsvParserOptions());
          } else if(!(item instanceof XQMap)) {
            throw new UnsupportedOperationException(
                "Cannot serialize items of type " + item.getClass());
          } else if(((XQMap) item).contains(CsvXQueryConverter.RECORDS)) {
            final CsvParserOptions copts = new CsvParserOptions();
            copts.assign(opts, null);
            delegate = new CsvXQuerySerializer(os, so, copts);
          } else {
            final ParseCsvOptions popts = new ParseCsvOptions();
            popts.assign(opts, null);
            popts.validate(null);
            delegate = new CsvMapSerializer(os, so, popts.toCsvParserOptions());
          }
        } catch(final QueryException ex) {
          throw new QueryIOException(ex);
        }
      }
      delegate.serialize(item);
    }
  }
}
