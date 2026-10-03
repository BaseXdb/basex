package org.basex.build;

import java.io.*;
import java.util.*;

/**
 * This class parses multiple inputs into a single database instance.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MultiParser extends Parser {
  /** Parsers. */
  private final List<Parser> parsers;
  /** Current parser (can be {@code null}). */
  private Parser parser;

  /**
   * Constructor.
   * @param parsers parsers (the options of the first parser will be adopted)
   */
  public MultiParser(final List<Parser> parsers) {
    super(parsers.getFirst().source(), parsers.getFirst().options);
    this.parsers = parsers;
  }

  @Override
  public void parse(final Builder build) throws IOException {
    final String original = build.meta.original;
    long inputsize = 0;
    for(final Parser p : parsers) {
      parser = p;
      build.meta.inputsize = 0;
      p.parse(build);
      inputsize += build.meta.inputsize;
      p.close();
    }
    parser = null;
    build.meta.original = original;
    build.meta.inputsize = inputsize;
  }

  @Override
  public void close() throws IOException {
    if(parser != null) parser.close();
  }

  @Override
  public String detailedInfo() {
    return parser != null ? parser.detailedInfo() : super.detailedInfo();
  }

  @Override
  public double progressInfo() {
    return parser != null ? parser.progressInfo() : super.progressInfo();
  }
}
