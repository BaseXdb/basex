package org.basex.io.serial;

import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;

import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * This class serializes items to an output stream.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public abstract class OutputSerializer extends Serializer {
  /** Output stream. */
  protected final PrintOutput out;
  /** Serializer options. */
  protected final SerializerOptions sopts;
  /** Encoding. */
  protected final String encoding;
  /** Item separator. */
  protected byte[] itemsep;

  /** Number of spaces to indent. */
  private final int indents;
  /** Tabular character. */
  private final char tab;

  /**
   * Constructor.
   * @param os output stream
   * @param sopts serializer options
   * @throws QueryIOException query I/O exception
   */
  protected OutputSerializer(final OutputStream os, final SerializerOptions sopts)
      throws QueryIOException {

    this.sopts = sopts;
    indent = sopts.yes(INDENT);

    // project-specific options
    indents = sopts.get(INDENTS);
    tab = sopts.yes(TABULATOR) ? '\t' : ' ';

    encoding = Strings.normEncoding(sopts.get(ENCODING), true);
    PrintOutput po;
    if(encoding == Strings.UTF8) {
      po = PrintOutput.get(os);
    } else {
      try {
        po = new EncoderOutput(os, Charset.forName(encoding));
      } catch(final Exception ex) {
        Util.debug(ex);
        throw SERENCODING_X.getIO(encoding);
      }
    }
    final int limit = sopts.get(LIMIT);
    if(limit != -1) po.setLimit(limit);

    final Newline nl = sopts.get(NEWLINE);
    if(nl != Newline.NL) po = new NewlineOutput(po, token(nl.newline()));
    out = po;

    final String is = sopts.get(ITEM_SEPARATOR);
    if(is != null) itemsep = token(is);
  }

  @Override
  public void reset() {
    more = false;
  }

  @Override
  public final boolean finished() {
    return out.finished();
  }

  @Override
  public void close() throws IOException {
    out.flush();
  }

  /**
   * Prints indentation whitespaces.
   * @throws IOException I/O exception
   */
  protected void indent() throws IOException {
    if(indent) {
      out.print('\n');
      final int ls = level * indents;
      for(int l = 0; l < ls; l++) out.print(tab);
    }
  }

  /**
   * Prints an item separator.
   * @throws IOException I/O exception
   * @return boolean indicating if separator was printed
   */
  protected boolean separate() throws IOException {
    if(!more || itemsep == null) return false;
    out.print(itemsep);
    return true;
  }

  /**
   * Encodes the specified characters before printing.
   * @param text characters to be encoded and printed
   * @throws IOException I/O exception
   */
  protected final void printChars(final byte[] text) throws IOException {
    final int al = text.length;
    for(int a = 0; a < al; a += cl(text, a)) printChar(cp(text, a));
  }

  /**
   * Encodes the specified codepoint before printing.
   * @param cp codepoint to be encoded and printed
   * @throws IOException I/O exception
   */
  protected void printChar(final int cp) throws IOException {
    out.print(cp);
  }

  /**
   * Returns a hex entity for the specified codepoint.
   * @param cp codepoint
   * @throws IOException I/O exception
   */
  protected final void printHex(final int cp) throws IOException {
    out.print("&#x");
    boolean o = false;
    for(int i = 3; i >= 0; i--) {
      final int b = (cp >> (i << 3)) & 0xFF;
      if(o || b > 0x0F) {
        out.print(HEX[b >> 4]);
      }
      if(o || b != 0) {
        out.print(HEX[b & 0xF]);
        o = true;
      }
    }
    out.print(';');
  }
}
