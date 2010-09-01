package org.basex.build;

import static org.basex.core.Text.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.IO;

/**
 * This class defines an abstract parser for single files.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class FileParser extends Parser {
  /** Builder reference. */
  protected Builder builder;

  /**
   * Constructor.
   * @param f file reference
   */
  public FileParser(final String f) {
    super(f);
  }

  /**
   * Constructor.
   * @param f file reference
   * @param tar target for collection adding
   */
  public FileParser(final IO f, final String tar) {
    super(f, tar);
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

  @Override
  public String tit() {
    return PROGCREATE;
  }
}
