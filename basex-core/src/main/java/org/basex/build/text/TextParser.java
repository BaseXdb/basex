package org.basex.build.text;

import static org.basex.core.Text.*;
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
 * @author BaseX Team 2005-21, BSD License
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
  /** Current line. */
  private int line;

  /**
   * Constructor.
   * @param source document source
   * @param opts database options
   */
  public TextParser(final IO source, final MainOptions opts) {
    super(source, opts);
    final TextOptions topts = opts.get(MainOptions.TEXTPARSER);
    lines = topts.get(TextOptions.LINES);
    encoding = topts.get(TextOptions.ENCODING);
  }

  @Override
  public void parse() throws IOException {
    builder.openElem(TEXT, atts, nsp);

    final TokenBuilder tb = new TokenBuilder();
    try(NewlineInput nli = new NewlineInput(source)) {
      nli.encoding(encoding);
      for(int ch; (ch = nli.read()) != -1;) {
        if(ch == '\n' && lines) {
          builder.openElem(LINE, atts, nsp);
          builder.text(tb.next());
          builder.closeElem();
          line++;
        } else {
          tb.add(XMLToken.valid(ch) ? ch : REPLACEMENT);
        }
      }
    }
    if(!lines) builder.text(tb.finish());
    builder.closeElem();
  }

  @Override
  public String detailedInfo() {
    return Util.info(LINE_X, line);
  }
}
