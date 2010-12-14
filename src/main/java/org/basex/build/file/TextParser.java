package org.basex.build.file;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.build.FileParser;
import org.basex.io.BufferInput;
import org.basex.io.IO;
import org.basex.util.TokenBuilder;

/**
 * This class parses files in the plain-text format
 * and sends events to the specified database builder.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class TextParser extends FileParser {
  /** Root element. */
  private static final byte[] TEXT = token("text");
  /** Line element. */
  private static final byte[] LINE = token("line");

  /**
   * Constructor.
   * @param path file path
   * @param ta database target
   */
  public TextParser(final IO path, final String ta) {
    super(path, ta);
  }

  @Override
  public void parse() throws IOException {
    builder.startElem(TEXT, atts);

    final BufferInput bi = new BufferInput(file.path());
    bi.encoding();

    final TokenBuilder tb = new TokenBuilder();
    while(true) {
      final int ch = bi.readChar();
      if(ch == 0) break;
      if(ch == 0x0A) {
        builder.startElem(LINE, atts);
        builder.text(tb);
        builder.endElem(LINE);
        tb.reset();
      } else if(ch != 0x0D) {
        tb.add(ch);
      }
    }
    bi.close();
    builder.endElem(TEXT);
  }
}
