package org.basex.build.file;

import static org.basex.util.Token.*;

import java.io.IOException;
import org.basex.build.FileParser;
import org.basex.build.ParserProp;
import org.basex.core.Prop;
import org.basex.io.BufferInput;
import org.basex.io.IO;
import org.basex.util.TokenBuilder;

/**
 * This class parses files in the plain-text format
 * and sends events to the specified database builder.
 *
 * <p>The parser provides one option, which can be specified via
 * <code>SET PARSEROPT ...</code>:</p>
 *
 * <ul>
 *   <li><code>encoding</code> specifies the input encoding
 *   (default: <code>UTF-8</code>).</li>
 *   <li><code>lines</code> specified if the resulting XML splits the input
 *   into lines. Can be set to <code>yes</code> or <code>no</code>
 *   (default: <code>yes</code>).</li>
 * </ul>
 *
 * <p><b>Example</b>:
 * <code>SET PARSEROPT lines=no; CREATE DB ...</code><br/>
 * <b>Description</b>: Puts complete input into one text node.</p>
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class TextParser extends FileParser {
  /** Text element. */
  private static final byte[] TEXT = token("text");
  /** Line element. */
  private static final byte[] LINE = token("line");

  /** Lines format. */
  private final boolean lines;
  /** Encoding. */
  private final String encoding;

  /**
   * Constructor.
   * @param path file path
   * @param ta database target
   * @param prop database properties
   * @throws IOException I/O exception
   */
  public TextParser(final IO path, final String ta, final Prop prop)
      throws IOException {

    super(path, ta);

    // parser properties
    final ParserProp props = new ParserProp(prop.get(Prop.PARSEROPT));
    lines = props.is(ParserProp.LINES);
    final String e = props.get(ParserProp.ENCODING);
    encoding = e != null ? e : UTF8;
  }

  @Override
  public void parse() throws IOException {
    builder.startElem(TEXT, atts);

    final BufferInput bi = new BufferInput(file.path());
    bi.encoding(encoding);

    final TokenBuilder tb = new TokenBuilder();
    while(true) {
      final int ch = bi.readChar();
      if(ch == 0) break;
      if(ch == 0x0A && lines) {
        builder.startElem(LINE, atts);
        builder.text(tb);
        builder.endElem(LINE);
        tb.reset();
      } else if(ch != 0x0D) {
        tb.add(ch);
      }
    }
    bi.close();
    if(!lines) builder.text(tb);
    builder.endElem(TEXT);
  }
}
