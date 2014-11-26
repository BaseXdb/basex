package org.basex.build;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;

/**
 * This class defines an abstract parser for single resources.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class SingleParser extends Parser {
  /** Builder reference. */
  protected Builder builder;

  /**
   * Constructor.
   * @param source input source
   * @param opts database options
   */
  protected SingleParser(final IO source, final MainOptions opts) {
    super(source, opts);
  }

  @Override
  public final void parse(final Builder build) throws IOException {
    builder = build;
    builder.openDoc(token(target + source.name()));
    parse();
    builder.closeDoc();
  }

  /**
   * Parses the current input.
   * @throws IOException I/O exception
   */
  protected abstract void parse() throws IOException;
}
