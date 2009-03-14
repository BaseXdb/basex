package org.basex.index;

import java.io.IOException;
import org.basex.data.Data;

/**
 * This interface defines the functions which are needed for building
 * new index structures.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public interface IndexBuilder {
  /**
   * Builds the index structure and returns an index instance.
   * @param data data reference
   * @return index instance
   * @throws IOException IO Exception
   */
  Index build(final Data data) throws IOException;
}
