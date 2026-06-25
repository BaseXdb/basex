package org.basex.io.serial;

import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

import org.basex.io.out.*;
import org.basex.util.*;

/**
 * This class serializes items to an output stream.
 *
 * @author BaseX Team, BSD License
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

  /** Indentation unit (whitespace string emitted per nesting level). */
  private final byte[] indentUnit;

  /**
   * Constructor.
   * @param os output stream
   * @param sopts serializer options
   * @throws IOException I/O exception
   */
  protected OutputSerializer(final OutputStream os, final SerializerOptions sopts)
      throws IOException {

    this.sopts = sopts;
    indent = sopts.yes(INDENT);
    canonical = sopts.yes(CANONICAL);

    // indentation unit: standard 'indent-unit' takes precedence over 'tabulator'/'indents'
    final String unit = sopts.get(INDENT_UNIT);
    if(unit != null) {
      indentUnit = token(unescape(unit));
    } else {
      final byte ch = (byte) (sopts.yes(TABULATOR) ? '\t' : ' ');
      indentUnit = new byte[sopts.get(INDENTS)];
      Arrays.fill(indentUnit, ch);
    }

    encoding = Strings.normEncoding(sopts.get(ENCODING), true);
    PrintOutput po;
    if(encoding == Strings.UTF8) {
      po = PrintOutput.get(os);
    } else {
      final String error = Strings.checkEncoding(encoding);
      if(error != null) throw SERENCODING_X.getIO(error);
      po = new EncoderOutput(os, Charset.forName(encoding));
    }
    final int limit = sopts.get(LIMIT);
    if(limit != -1) po.setLimit(limit);

    // line ending: standard 'line-ending' takes precedence over 'newline'
    final String le = sopts.get(LINE_ENDING);
    final String newline = le != null ? unescape(le) : sopts.get(NEWLINE).newline();
    if(!newline.equals("\n")) po = new NewlineOutput(po, token(newline));
    out = po;

    final String is = sopts.get(ITEM_SEPARATOR);
    if(is != null) itemsep = token(is);

    if(sopts.yes(BYTE_ORDER_MARK)) {
      switch(encoding) {
        case Strings.UTF8:    out.write(0xEF); out.write(0xBB); out.write(0xBF); break;
        case Strings.UTF16LE: out.write(0xFF); out.write(0xFE); break;
        case Strings.UTF16BE: out.write(0xFE); out.write(0xFF); break;
      }
    }
  }

  /**
   * Replaces the escapes {@code \t}, {@code \r} and {@code \n} with the corresponding characters.
   * @param value parameter value
   * @return resulting string
   */
  private static String unescape(final String value) {
    return value.replace("\\t", "\t").replace("\\r", "\r").replace("\\n", "\n");
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
   * Prints indentation whitespace.
   * @throws IOException I/O exception
   */
  protected void indent() throws IOException {
    if(indent) {
      out.print('\n');
      for(int l = 0; l < level; l++) out.print(indentUnit);
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
   * Encodes and prints characters.
   * @param text characters to be printed
   * @throws IOException I/O exception
   */
  protected final void printChars(final byte[] text) throws IOException {
    final int tl = text.length;
    for(int t = 0; t < tl; t += cl(text, t)) printChar(cp(text, t));
  }

  /**
   * Encodes and prints a character.
   * @param cp codepoint to be printed
   * @throws IOException I/O exception
   */
  protected abstract void printChar(int cp) throws IOException;

  /**
   * Returns a hex entity for the specified codepoint.
   * @param cp codepoint
   * @throws IOException I/O exception
   */
  protected final void printHex(final int cp) throws IOException {
    out.print('&');
    out.print('#');
    out.print('x');
    boolean o = false;
    for(int i = 3; i >= 0; i--) {
      final int b = cp >> (i << 3) & 0xFF;
      if(o || b > 0x0F) {
        out.print(HEX_TABLE[b >> 4]);
      }
      if(o || b != 0) {
        out.print(HEX_TABLE[b & 0xF]);
        o = true;
      }
    }
    out.print(';');
  }
}
