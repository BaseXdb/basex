package org.basex.build.file;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.list.*;

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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CSVParser extends SingleParser {
  /** Separators. */
  public static final String[] SEPARATORS = { "comma", "semicolon", "tab", "space" };
  /** Separator mappings. */
  public static final char[] SEPMAPPINGS = { ',', ';', '\t', ' ' };
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

  /** Headers. */
  private final TokenList headers = new TokenList();
  /** Simple format. */
  private final boolean simple;
  /** Encoding. */
  private final String encoding;
  /** Column separator (see {@link ParserProp#SEPARATOR}). */
  private final int separator;

  /** Current row. */
  private int row;
  /** Current column. */
  private int col;

  /**
   * Constructor.
   * @param source document source
   * @param pr database properties
   * @throws IOException I/O exception
   */
  public CSVParser(final IO source, final Prop pr) throws IOException {
    super(source, pr);

    // set parser properties
    final ParserProp props = new ParserProp(pr.get(Prop.PARSEROPT));
    row = props.is(ParserProp.HEADER) ? 0 : 1;

    // set separator
    String val = props.get(ParserProp.SEPARATOR).toLowerCase(Locale.ENGLISH);
    int s = -1;
    for(int i = 0; i < SEPARATORS.length && s == -1; i++) {
      if(val.equals(SEPARATORS[i])) s = SEPMAPPINGS[i];
    }
    if(s == -1) {
      final int i = toInt(token(val));
      if(i > 0) s = i;
      else throw new BaseXException(INVALID_VALUE_X_X, ParserProp.SEPARATOR[0], val);
    }
    separator = s;

    // set XML format
    val = props.get(ParserProp.FORMAT).toLowerCase(Locale.ENGLISH);
    simple = val.equals(FORMATS[0]);
    if(!simple && !val.equals(FORMATS[1])) throw new BaseXException(
        INVALID_VALUE_X_X, ParserProp.FORMAT[0], val);
    encoding = props.get(ParserProp.ENCODING);
  }

  @SuppressWarnings("resource")
  @Override
  public void parse() throws IOException {
    builder.startElem(CSV, atts);

    final TokenBuilder tb = new TokenBuilder();
    final NewlineInput nli = new NewlineInput(src).encoding(encoding);

    boolean quoted = false, open = true;
    int ch = -1;
    while(true) {
      if(ch == -1) ch = nli.read();
      if(ch == -1) break;
      if(quoted) {
        if(ch == '"') {
          ch = nli.read();
          if(ch != '"') {
            quoted = false;
            continue;
          }
        }
        tb.add(ch);
      } else if(ch == separator) {
        if(open) {
          open();
          open = false;
        }
        add(tb);
      } else if(ch == '\n') {
        finish(tb, open);
        open = true;
      } else if(ch == '"') {
        quoted = true;
      } else {
        tb.add(XMLToken.valid(ch) ? ch : '?');
      }
      ch = -1;
    }
    nli.close();

    finish(tb, open);
    builder.endElem();
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
  private void finish(final TokenBuilder tb, final boolean open) throws IOException {
    boolean close = !open;
    if(open && !tb.isEmpty()) {
      open();
      close = true;
    }
    add(tb);
    if(close) {
      if(simple || row != 0) builder.endElem();
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

    final byte[] t;
    if(simple) {
      t = ENTRY;
    } else {
      if(col == headers.size()) addHeader(COLUMN);
      t = headers.get(col);
    }

    if(!tb.isEmpty() || simple) {
      builder.startElem(t, atts);
      builder.text(tb.finish());
      builder.endElem();
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
    if(nm.isEmpty()) nm.add(COLUMN);

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
