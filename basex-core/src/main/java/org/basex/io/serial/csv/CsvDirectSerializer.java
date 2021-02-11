package org.basex.io.serial.csv;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.csv.*;
import org.basex.build.csv.CsvOptions.CsvFormat;
import org.basex.io.serial.*;
import org.basex.query.util.ft.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class serializes items as CSV.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CsvDirectSerializer extends CsvSerializer {
  /** Names of header elements. */
  private final TokenList headers;
  /** Attribute format. */
  private final boolean atts;
  /** Lax flag. */
  private final boolean lax;

  /** Contents of current row. */
  private TokenMap data;
  /** Current attribute value. */
  private byte[] attv;

  /**
   * Constructor.
   * @param os output stream
   * @param opts serialization parameters
   * @throws IOException I/O exception
   */
  public CsvDirectSerializer(final OutputStream os, final SerializerOptions opts)
      throws IOException {

    super(os, opts);
    headers = header ? new TokenList() : null;
    atts = copts.get(CsvOptions.FORMAT) == CsvFormat.ATTRIBUTES;
    lax = copts.get(CsvOptions.LAX) || atts;
  }

  @Override
  protected void startOpen(final QNm name) {
    if(level == 1) data = new TokenMap();
    attv = null;
  }

  @Override
  protected void finishEmpty() throws IOException {
    finishOpen();
    if(level == 2) cache(EMPTY);
    finishClose();
  }

  @Override
  protected void text(final byte[] value, final FTPos ftp) throws IOException {
    if(level == 3) cache(value);
  }

  @Override
  protected void finishClose() throws IOException {
    if(level != 1) return;

    final TokenList tl = new TokenList();
    if(headers != null) {
      final int size = headers.size();
      // print header
      if(header) {
        for(int i = 0; i < size; i++) tl.add(headers.get(i));
        record(tl);
        header = false;
      }
      // print data, sorted by headers
      for(int i = 0; i < size; i++) tl.add(data.get(headers.get(i)));
    } else {
      // no headers available: print data
      for(final byte[] value : data.values()) tl.add(value);
    }
    record(tl);
  }

  @Override
  protected void attribute(final byte[] name, final byte[] value, final boolean standalone) {
    attv = value;
  }

  /**
   * Caches the specified text and its header.
   * @param value text to be cached
   * @throws IOException I/O exception
   */
  private void cache(final byte[] value) throws IOException {
    if(headers != null) {
      final byte[] key = atts && attv != null ? attv : elem.string();
      final byte[] name = XMLToken.decode(key, lax);
      if(name == null) throw CSV_SERIALIZE_X.getIO(Util.inf("Invalid element name <%>", key));
      if(!headers.contains(name)) headers.add(name);
      final byte[] old = data.get(name);
      data.put(name, old == null || old.length == 0 ? value :
        value.length == 0 ? old : concat(old, ',', value));
    } else {
      data.put(token(data.size()), value);
    }
  }
}
