package org.basex.build.file;

import static org.basex.util.Token.*;

import java.io.IOException;
import org.basex.build.SingleParser;
import org.basex.core.Prop;
import org.basex.io.IO;
import org.basex.io.in.NewlineInput;
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
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TextParser extends SingleParser {
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
    // set parser properties
    final ParserProp props = new ParserProp(prop.get(Prop.PARSEROPT));
    lines = props.is(ParserProp.LINES);
    encoding = props.get(ParserProp.ENCODING);
  }

  @Override
  public void parse() throws IOException {
    builder.startElem(TEXT, atts);

    final TokenBuilder tb = new TokenBuilder();
    final NewlineInput ti = new NewlineInput(src, encoding);
    try {
      for(int ch; (ch = ti.read()) != -1;) {
        if(ch == '\n' && lines) {
          builder.startElem(LINE, atts);
          builder.text(tb.finish());
          builder.endElem();
          tb.reset();
        } else {
          tb.add(ch);
        }
      }
    } finally {
      ti.close();
    }
    if(!lines) builder.text(tb.finish());
    builder.endElem();
  }
}
