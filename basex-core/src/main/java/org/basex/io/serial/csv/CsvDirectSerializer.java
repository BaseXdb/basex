package org.basex.io.serial.csv;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.build.CsvOptions.*;
import org.basex.io.serial.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class serializes data as CSV.
 *
 * @author BaseX Team 2005-13, BSD License
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
    lax = copts.get(CsvOptions.LAX);
    atts = copts.get(CsvOptions.FORMAT) == CsvFormat.ATTRIBUTES;
  }

  @Override
  protected void startOpen(final byte[] name) {
    if(level == 1) data = new TokenMap();
    attv = null;
  }

  @Override
  protected void finishOpen() throws IOException {
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    if(level == 2) cache(EMPTY);
    finishClose();
  }

  @Override
  protected void finishText(final byte[] text) {
    if(level == 3) cache(text);
  }

  @Override
  protected void finishClose() throws IOException {
    if(level != 1) return;

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
  protected void attribute(final byte[] n, final byte[] v) {
    attv = v;
  }

  @Override
  protected void finishComment(final byte[] n) { }

  @Override
  protected void finishPi(final byte[] n, final byte[] v) { }

  @Override
  protected void atomic(final Item value) throws IOException {
    error("Atomic values cannot be serialized");
  }

  @Override
  protected void encode(final int ch) throws IOException {
    printChar(ch);
  }

  /**
   * Caches the specified text and its header.
   * @param text text to be cached
   */
  private void cache(final byte[] text) {
    if(headers != null) {
      final byte[] name = XMLToken.decode(atts && attv != null ? attv : tag, lax);
      if(!headers.contains(name)) headers.add(name);
      final byte[] old = data.get(name);
      final byte[] txt = old == null || old.length == 0 ? text :
        text.length == 0 ? old : new TokenBuilder(old).add(',').add(text).finish();
      data.put(name, txt);
    } else {
      data.put(token(data.size()), text);
    }
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @throws IOException I/O exception
   */
  private static void error(final String msg) throws IOException {
    throw BXCS_SERIAL.thrwIO(msg);
  }
}
