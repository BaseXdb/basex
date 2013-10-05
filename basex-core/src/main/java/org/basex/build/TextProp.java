package org.basex.build;

import java.io.*;

import org.basex.core.*;
import org.basex.util.*;

/**
 * This class contains parser properties.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TextProp extends AProp {
  /** Parser option: encoding. */
  public static final Object[] ENCODING = { "encoding", Token.UTF8 };
  /** Parser option: line-wise parsing. */
  public static final Object[] LINES = { "lines", true };

  /**
   * Constructor.
   */
  public TextProp() {
    super();
  }

  /**
   * Constructor, specifying initial properties.
   * @param s property string
   * @throws IOException I/O exception
   */
  public TextProp(final String s) throws IOException {
    parse(s);
  }
}
