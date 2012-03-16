package org.basex.query.item;

import java.io.*;

import org.basex.query.*;
import org.basex.util.*;

/**
 * Interface for streamable items.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface Streamable {
  /**
   * Returns an input stream.
   * @param ii input info
   * @return input stream
   * @throws QueryException query exception
   */
  InputStream input(final InputInfo ii) throws QueryException;
}
