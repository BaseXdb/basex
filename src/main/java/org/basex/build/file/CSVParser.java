package org.basex.build.file;

import static org.basex.util.Token.*;
import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.build.FileParser;
import org.basex.build.ParserProp;
import org.basex.core.Prop;
import org.basex.io.BufferInput;
import org.basex.io.IO;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenList;
import org.basex.util.Util;
import org.basex.util.XMLToken;

/**
 * This class parses files in the CSV format
 * and sends events to the specified database builder.
 *
 * <p>The parser provides some options, which can be specified via
 * <code>SET PARSEROPT ...</code>:</p>
 *
 * <ul>
 *   <li><code>separator</code> defines the column separator, which can be
 *   <code>comma</code>, <code>semicolon</code>, or <code>tab</code>
 *   (default: <code>comma</code>).</li>
 *   <li><code>header</code> specifies if the input file contains a header.
 *   Can be set to <code>yes</code> or <code>no</code>
 *   (default: <code>yes</code>)</li>
 *   <li><code>format</code> specifies the XML format, which can be
 *   <code>simple</code> or <code>verbose</code>
 *   (default: <code>verbose</code>).</li>
 * </ul>
 *
 * <p>All options are separated by commas, and the keys and values are
 * separated by equality sign (=).</p>
 *
 * <p><b>Example</b>:
 * <code>SET PARSEROPT separator=tab,format=simple,header=no; CREATE DB ...
 * </code><br/>
 * <b>Description</b>: Use tabs as separator, choose simple XML format,
 * and indicate that the file contains no header.</p>
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class CSVParser extends FileParser {
  /** Separators. */
  public static final String[] SEPARATORS = { "comma", "semicolon", "tab" };
  /** Formats. */
  public static final String[] FORMATS = { "simple", "verbose" };

  /** CSV root element. */
  private static final byte[] CSV = token("csv");
  /** CSV header element. */
  private static final byte[] HEADER = token("header");
  /** CSV record element. */
  private static final byte[] RECORD = token("record");
  /** CSV field element. */
  private static final byte[] ENTRY = token("entry");
  /** CSV column attribute. */
  private static final byte[] COLUMN = token("col");

  /** Column separator (see {@link ParserProp#SEPARATOR}). */
  private final int separator;
  /** Headers. */
  private final TokenList headers = new TokenList();
  /** Simple format. */
  private final boolean simple;
  /** Encoding. */
  private final String encoding;

  /** Current row. */
  private int row;
  /** Current column. */
  private int col;

  /**
   * Constructor.
   * @param path file path
   * @param ta database target
   * @param prop database properties
   * @throws IOException I/O exception
   */
  public CSVParser(final IO path, final String ta, final Prop prop)
      throws IOException {

    super(path, ta);

    // set parser properties
    final ParserProp props = new ParserProp(prop.get(Prop.PARSEROPT));
    row = props.is(ParserProp.HEADER) ? 0 : 1;

    // set separator
    String s = props.get(ParserProp.SEPARATOR).toLowerCase();
    separator = s.equals(SEPARATORS[0]) ? ',' : s.equals(SEPARATORS[1]) ? ';' :
      s.equals(SEPARATORS[2]) ? '\t' : -1;
    if(separator == -1) throw new IOException(
        Util.info(SETVAL, ParserProp.SEPARATOR[0], s));

    // set XML format
    s = props.get(ParserProp.FORMAT).toLowerCase();
    simple = s.equals(FORMATS[0]);
    if(!simple && !s.equals(FORMATS[1])) throw new IOException(
        Util.info(SETVAL, ParserProp.FORMAT[0], s));

    s = props.get(ParserProp.ENCODING);
    encoding = s != null ? s : UTF8;
  }

  @Override
  public void parse() throws IOException {
    builder.startElem(CSV, atts);

    final TokenBuilder tb = new TokenBuilder();
    final BufferInput bi = new BufferInput(file.path());
    bi.encoding(encoding);

    boolean quoted = false, open = true;
    int ch = 0;
    while(true) {
      if(ch == 0) ch = bi.readChar();
      if(ch == 0) break;
      if(quoted) {
        if(ch == '"') {
          ch = bi.readChar();
          if(ch != '"') {
            quoted = false;
            continue;
          }
        }
        if(ch != 0x0D) tb.add(ch);
      } else if(ch == separator) {
        if(open) {
          open();
          open = false;
        }
        add(tb);
      } else if(ch == 0x0A) {
        finish(tb, open);
        open = true;
      } else if(ch == '"') {
        quoted = true;
      } else if(ch != 0x0D) {
        tb.add(ch);
      }
      ch = 0;
    }
    bi.close();

    finish(tb, open);
    builder.endElem(CSV);
  }

  /**
   * Opens a new record.
   * @throws IOException I/O exception
   */
  private void open() throws IOException {
    if(row == 0) {
      if(simple) builder.startElem(HEADER, atts);
    } else {
      builder.startElem(RECORD, atts);
    }
  }

  /**
   * Finishes the current record.
   * @param tb token builder
   * @param open open flag
   * @throws IOException I/O exception
   */
  private void finish(final TokenBuilder tb, final boolean open)
      throws IOException {

    boolean close = !open;
    if(open && tb.size() != 0) {
      open();
      close = true;
    }
    add(tb);
    if(close) {
      if(simple || row != 0) {
        builder.endElem(row == 0 ? HEADER : RECORD);
      }
      ++row;
    }
    col = 0;
  }

  /**
   * Adds a field.
   * @param tb token builder
   * @throws IOException I/O exception
   */
  private void add(final TokenBuilder tb) throws IOException {
    if(row == 0 && !simple) {
      addHeader(tb.finish());
      tb.reset();
      return;
    }

    byte[] tag = simple ? ENTRY : headers.get(col);
    if(tag == null) {
      addHeader(COLUMN);
      tag = headers.get(col);
    }

    if(tb.size() != 0 || simple) {
      builder.startElem(tag, atts);
      builder.text(tb.finish());
      builder.endElem(tag);
      tb.reset();
    }
    ++col;
  }

  /**
   * Adds a field header.
   * @param f field name
   */
  private void addHeader(final byte[] f) {
    // create tag name
    final TokenBuilder nm = new TokenBuilder();
    for(int p = 0; p < f.length; p += cl(f, p)) {
      final int cp = cp(f, p);
      nm.add((p == 0 ? XMLToken.isNCStartChar(cp) :
        XMLToken.isNCChar(cp)) ? cp : '_');
    }
    // no valid characters found: add default column name
    if(nm.size() == 0) nm.add(COLUMN);

    // tag exists: attach enumerator
    byte[] fb = nm.finish();
    if(headers.contains(fb)) {
      int c = 2;
      do {
        fb = concat(nm.finish(), token(c++));
      } while(headers.contains(fb));
    }
    // add header
    headers.add(fb);
  }
}
