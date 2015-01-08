package org.basex.build.text;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;

/**
 * This class parses files in the plain-text format
 * and converts them to XML.
 *
 * <p>The parser provides some options, which can be specified via the
 * {@link MainOptions#TEXTPARSER} option.</p>
 *
 * @author BaseX Team 2005-14, BSD License
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
   * @param opts database options
   */
  public TextParser(final IO source, final MainOptions opts) {
    super(source, opts);
    final TextOptions tp = opts.get(MainOptions.TEXTPARSER);
    lines = tp.get(TextOptions.LINES);
    encoding = tp.get(TextOptions.ENCODING);
  }

  @Override
  public void parse() throws IOException {
    builder.openElem(TEXT, atts, nsp);

    final TokenBuilder tb = new TokenBuilder();
    try(final NewlineInput nli = new NewlineInput(source).encoding(encoding)) {
      for(int ch; (ch = nli.read()) != -1;) {
        if(ch == '\n' && lines) {
          builder.openElem(LINE, atts, nsp);
          builder.text(tb.next());
          builder.closeElem();
        } else {
          tb.add(XMLToken.valid(ch) ? ch : '?');
        }
      }
    }
    if(!lines) builder.text(tb.finish());
    builder.closeElem();
  }
}
