package org.basex.io.serial.csv;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.regex.*;

import org.basex.build.csv.*;
import org.basex.io.serial.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class serializes items as CSV.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
abstract class CsvSerializer extends StandardSerializer {
  /** CSV options. */
  final CsvSerialOptions copts;
  /** Separator. */
  final int separator;
  /** Generate quotes. */
  final boolean quotes;
  /** Generate backslashes. */
  final boolean backslashes;

  /** Data pattern. */
  Pattern allow;
  /** Header flag. */
  boolean header;

  /**
   * Constructor.
   * @param os output stream
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  CsvSerializer(final OutputStream os, final SerializerOptions opts) throws IOException {
    super(os, opts);
    copts = opts.get(SerializerOptions.CSV);
    quotes = copts.get(CsvOptions.QUOTES);
    backslashes = copts.get(CsvOptions.BACKSLASHES);
    header = copts.get(CsvOptions.HEADER);
    final String allw = copts.get(CsvSerialOptions.ALLOW);
    if(!allw.isEmpty()) {
      try {
        allow = Pattern.compile(allw);
      } catch(final PatternSyntaxException ex) {
        Util.debug(ex);
        throw CSV_SERIALIZE_X_X.getIO("Invalid pattern", allw);
      }
    }
    separator = copts.separator();
  }

  /**
   * Prints a record with the specified entries.
   * @param entries record entries to be printed (will be reset after serialization)
   * @throws IOException I/O exception
   */
  final void record(final TokenList entries) throws IOException {
    // print fields, skip trailing empty contents
    final int fs = entries.size();
    for(int i = 0; i < fs; i++) {
      final byte[] v = entries.get(i);
      if(i != 0) out.print(separator);

      byte[] txt = EMPTY;
      if(v != null) {
        txt = v;
        if(allow != null && !allow.matcher(string(v)).matches())
          throw CSV_SERIALIZE_X_X.getIO("Value is not allowed", v);
      }
      final boolean delim = contains(txt, separator) || contains(txt, '\n');
      final boolean special = contains(txt, '\r') || contains(txt, '\t') || contains(txt, '"');
      if(delim || special || backslashes && contains(txt, '\\')) {
        final TokenBuilder tb = new TokenBuilder();
        if(delim && !backslashes && !quotes)
          throw CSV_SERIALIZE_X_X.getIO("Output must be put into quotes", txt);

        if(quotes && (delim || special)) tb.add('"');
        final int len = txt.length;
        for(int c = 0; c < len; c += cl(txt, c)) {
          final int cp = cp(txt, c);
          if(backslashes) {
            if(cp == '\n') tb.add("\\n");
            else if(cp == '\r') tb.add("\\r");
            else if(cp == '\t') tb.add("\\t");
            else if(cp == '"') tb.add("\\\"");
            else if(cp == '\\') tb.add("\\\\");
            else if(cp == separator && !quotes) tb.add('\\').add(cp);
            else tb.add(cp);
          } else {
            if(cp == '"') tb.add('"');
            tb.add(cp);
          }
        }
        if(quotes && (delim || special)) tb.add('"');
        txt = tb.finish();
      }
      out.print(txt);
    }
    out.print('\n');
    entries.reset();
  }

  @Override
  protected void atomic(final Item value) throws IOException {
    throw CSV_SERIALIZE_X.getIO("Atomic values cannot be serialized");
  }
}
