package org.basex.build;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.IO;

/**
 * This class defines an abstract parser for single files.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class FileParser extends Parser {
  /** Builder reference. */
  protected Builder builder;

  /**
   * Constructor.
   * @param io parser input
   * @param tar collection target
   */
  public FileParser(final IO io, final String tar) {
    super(io, tar);
  }

  @Override
  public final void parse(final Builder build) throws IOException {
    builder = build;
    builder.startDoc(token(target + file.name()));
    parse();
    builder.endDoc();
  }

  /**
   * Parses the current file.
   * @throws IOException I/O exception
   */
  protected abstract void parse() throws IOException;
}
