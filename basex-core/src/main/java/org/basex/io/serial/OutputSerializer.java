package org.basex.io.serial;

import static org.basex.io.serial.SerializerOptions.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;
import java.text.Normalizer.*;
import java.util.*;

import org.basex.io.out.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.options.*;

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
  /** Checked output ({@code null} for Unicode encodings, which can represent all characters). */
  private final CheckedOutput checked;
  /** Item separator. */
  protected byte[] itemsep;
  /** Normalization form (can be {@code null}). */
  protected final Form form;
  /** Character map (can be {@code null}). */
  protected final IntObjectMap<byte[]> cmap;

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
      indentUnit = token(unescape(INDENT_UNIT.name(), unit, "\t*| *"));
    } else {
      final byte ch = (byte) (sopts.yes(TABULATOR) ? '\t' : ' ');
      indentUnit = new byte[sopts.get(INDENTS)];
      Arrays.fill(indentUnit, ch);
    }

    encoding = encoding(sopts);
    PrintOutput po = PrintOutput.get(os);
    final int limit = sopts.get(LIMIT);
    if(limit != -1) po.setLimit(limit);

    // line ending: standard 'line-ending' takes precedence over 'newline'
    final String le = sopts.get(LINE_ENDING);
    final String newline = le != null ? unescape(LINE_ENDING.name(), le, "\r\n|\r|\n") :
      sopts.get(NEWLINE).newline();
    if(!newline.equals("\n")) po = new NewlineOutput(po, token(newline));

    // Unicode encodings: print byte order mark; others: check characters before printing
    final Charset charset = encoding == Strings.UTF8 ? StandardCharsets.UTF_8 :
      Charset.forName(encoding);
    if(charset.name().startsWith("UTF-")) {
      checked = null;
      out = po;
      if(sopts.yes(BYTE_ORDER_MARK)) out.print(0xFEFF);
    } else {
      checked = new CheckedOutput(po, charset);
      out = checked;
    }

    final String is = sopts.get(ITEM_SEPARATOR);
    if(is != null) itemsep = token(is);

    final String norm = sopts.get(NORMALIZATION_FORM);
    if(norm.equals(NORMALIZATION_FORM.value())) {
      form = null;
    } else {
      try {
        form = Form.valueOf(norm);
      } catch(final IllegalArgumentException ex) {
        throw SERNORM_X.getIO(norm).cause(ex);
      }
    }

    final String maps = sopts.get(USE_CHARACTER_MAPS);
    if(maps.isEmpty()) {
      cmap = null;
    } else {
      cmap = new IntObjectMap<>();
      final Map<String, String> map = Options.toMap(maps, new LinkedHashMap<>(), Options::unescape);
      for(final Map.Entry<String, String> entry : map.entrySet()) {
        final String key = entry.getKey();
        if(key.codePoints().count() != 1) throw SERPARAM_X.getIO(
            Util.info("Key in character map is not a single character: %.", key));
        cmap.put(key.codePointAt(0), token(entry.getValue()));
      }
    }
  }

  /**
   * Applies character mapping, and Unicode normalization to the characters that were not mapped.
   * @param value value
   * @param printer printer for normalized runs of characters that were not mapped
   * @throws IOException I/O exception
   */
  protected final void expand(final byte[] value, final TextPrinter printer) throws IOException {
    expand(value, printer, out::print);
  }

  /**
   * Applies character mapping, and Unicode normalization to the characters that were not mapped.
   * @param value value
   * @param printer printer for normalized runs of characters that were not mapped
   * @param mapped printer for the strings of mapped characters
   * @throws IOException I/O exception
   */
  protected final void expand(final byte[] value, final TextPrinter printer,
      final TextPrinter mapped) throws IOException {
    final int vl = value.length;
    int s = 0;
    if(cmap != null) {
      for(int v = 0; v < vl;) {
        final int l = cl(value, v);
        final byte[] string = cmap.get(cp(value, v));
        if(string != null) {
          if(s < v) printer.print(normalize(substring(value, s, v), form));
          mapped.print(string);
          s = v + l;
        }
        v += l;
      }
    }
    if(s < vl) printer.print(normalize(substring(value, s, vl), form));
  }

  /**
   * Printer for characters.
   */
  @FunctionalInterface
  protected interface TextPrinter {
    /**
     * Prints characters.
     * @param text characters
     * @throws IOException I/O exception
     */
    void print(byte[] text) throws IOException;
  }

  /**
   * Returns the normalized output encoding.
   * @param sopts serializer options
   * @return encoding
   * @throws IOException I/O exception
   */
  static String encoding(final SerializerOptions sopts) throws IOException {
    final String encoding = Strings.normEncoding(sopts.get(ENCODING), true);
    final String error = encoding == Strings.UTF8 ? null : Strings.checkEncoding(encoding);
    if(error != null) throw SERENCODING_X.getIO(error);
    return encoding;
  }

  /**
   * Checks if a character can be represented in the output encoding.
   * @param cp codepoint
   * @return result of check
   */
  protected final boolean encodable(final int cp) {
    return checked == null || checked.encodable(cp);
  }

  /**
   * Replaces the escapes {@code \t}, {@code \r} and {@code \n} with the corresponding characters
   * and checks the result against the pattern of allowed values.
   * @param name parameter name
   * @param value parameter value
   * @param pattern allowed values
   * @return resulting string
   * @throws IOException I/O exception
   */
  private static String unescape(final String name, final String value, final String pattern)
      throws IOException {
    final String unescaped = value.replace("\\t", "\t").replace("\\r", "\r").replace("\\n", "\n");
    if(!unescaped.matches(pattern))
      throw SERPARAM_X.getIO(Util.info("Invalid value for '%': '%'.", name, value));
    return unescaped;
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
