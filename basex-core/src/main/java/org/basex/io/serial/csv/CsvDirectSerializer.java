package org.basex.io.serial.csv;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.*;
import org.basex.data.*;
import org.basex.io.serial.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class serializes data as CSV.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class CsvDirectSerializer extends CsvSerializer {
  /** Names of header elements. */
  private final TokenList headers;
  /** Attribute format. */
  private final boolean atts;
  /** Lax flag. */
  private final boolean lax;

  /** Header flag. */
  private boolean header;
  /** Contents of current row. */
  private TokenMap data;
  /** Current attribute value. */
  private byte[] attv;

  /**
   * Constructor.
   * @param os output stream reference
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  public CsvDirectSerializer(final OutputStream os, final SerializerOptions opts)
      throws IOException {

    super(os, opts);
    header = copts.get(CsvOptions.HEADER);
    headers = header ? new TokenList() : null;
    atts = copts.get(CsvOptions.FORMAT) == CsvFormat.ATTRIBUTES;
    lax = copts.get(CsvOptions.LAX) || atts;
  }

  @Override
  protected void startOpen(final byte[] name) {
    if(lvl == 1) data = new TokenMap();
    attv = null;
  }

  @Override
  protected void finishOpen() {
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    if(lvl == 2) cache(EMPTY);
    finishClose();
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    if(lvl == 3) cache(value);
  }

  @Override
  protected void finishClose() throws IOException {
    if(lvl != 1) return;

    if(headers != null) {
      final int s = headers.size();
      // print header
      if(header) {
        final TokenList tl = new TokenList();
        for(int i = 0; i < s; i++) tl.add(headers.get(i));
        record(tl);
        header = false;
      }
      // print data, sorted by headers
      final TokenList tl = new TokenList();
      for(int i = 0; i < s; i++) tl.add(data.get(headers.get(i)));
      record(tl);
    } else {
      // no headers available: print data
      final TokenList tl = new TokenList();
      for(final byte[] v : data.values()) tl.add(v);
      record(tl);
    }
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value) {
    attv = value;
  }

  @Override
  protected void comment(final byte[] value) { }

  @Override
  protected void pi(final byte[] name, final byte[] value) { }

  @Override
  protected void atomic(final Item value, final boolean iter) throws IOException {
    error("Atomic values cannot be serialized");
  }

  @Override
  protected void encode(final int cp) throws IOException {
    printChar(cp);
  }

  /**
   * Caches the specified text and its header.
   * @param value text to be cached
   * @throws IOException I/O exception
   */
  private void cache(final byte[] value) throws IOException {
    if(headers != null) {
      final byte[] key = atts && attv != null ? attv : elem;
      final byte[] name = XMLToken.decode(key, lax);
      if(name == null) error("Invalid element name <%>", key);
      if(!headers.contains(name)) headers.add(name);
      final byte[] old = data.get(name);
      final byte[] txt = old == null || old.length == 0 ? value :
        value.length == 0 ? old : new TokenBuilder(old).add(',').add(value).finish();
      data.put(name, txt);
    } else {
      data.put(token(data.size()), value);
    }
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @throws IOException I/O exception
   */
  private static void error(final String msg, final Object... ext) throws IOException {
    throw BXCS_SERIAL_X.getIO(Util.inf(msg, ext));
  }
}
