package org.basex.build;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.IO;

/**
 * This class defines an abstract parser for single documents.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class SingleParser extends TargetParser {
  /** Builder reference. */
  public Builder builder;

  /**
   * Constructor.
   * @param source document source
   * @param path target path
   */
  public SingleParser(final IO source, final String path) {
    super(source, path);
  }

  @Override
  public final void parse(final Builder build) throws IOException {
    builder = build;
    builder.startDoc(token(trg + src.name()));
    parse();
    builder.endDoc();
  }

  /**
   * Parses the current file.
   * @throws IOException I/O exception
   */
  protected abstract void parse() throws IOException;
}
