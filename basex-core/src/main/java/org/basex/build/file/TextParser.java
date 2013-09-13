package org.basex.build.file;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;

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
   * @param source document source
   * @param pr database properties
   * @throws IOException I/O exception
   */
  public TextParser(final IO source, final Prop pr) throws IOException {
    super(source, pr);
    // set parser properties
    final ParserProp props = new ParserProp(pr.get(Prop.PARSEROPT));
    lines = props.is(ParserProp.LINES);
    encoding = props.get(ParserProp.ENCODING);
  }

  @Override
  public void parse() throws IOException {
    builder.openElem(TEXT, atts, nsp);

    final TokenBuilder tb = new TokenBuilder();
    final NewlineInput nli = new NewlineInput(src).encoding(encoding);
    try {
      for(int ch; (ch = nli.read()) != -1;) {
        if(ch == '\n' && lines) {
          builder.openElem(LINE, atts, nsp);
          builder.text(tb.finish());
          builder.closeElem();
          tb.reset();
        } else {
          tb.add(XMLToken.valid(ch) ? ch : '?');
        }
      }
    } finally {
      nli.close();
    }
    if(!lines) builder.text(tb.finish());
    builder.closeElem();
  }
}
