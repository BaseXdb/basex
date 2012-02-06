package org.basex.query.util;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.util.*;

/**
 * <p>This class provides a parse method to convert data to XML nodes.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class XMLConverter {
  /** Input info. */
  protected final InputInfo input;

  /**
   * Constructor.
   * @param ii input info
   */
  public XMLConverter(final InputInfo ii) {
    input = ii;
  }

  /**
   * Parses the input.
   * @param q query
   * @return resulting node
   * @throws QueryException query exception
   */
  public abstract ANode parse(final byte[] q) throws QueryException;
}
