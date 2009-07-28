package org.basex.index;

import java.io.IOException;

import org.basex.BaseX;
import org.basex.core.Progress;
import org.basex.core.ProgressException;
import org.basex.data.Data;

/**
 * This interface defines the functions which are needed for building
 * new index structures.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class IndexBuilder extends Progress {
  /** Data reference. */
  protected final Data data;

  /**
   * Builds the index structure and returns an index instance.
   * @return index instance
   * @throws IOException IO Exception
   */
  public abstract Index build() throws IOException;

  /**
   * Constructor.
   * @param d reference
   */
  public IndexBuilder(final Data d) {
    data = d;
  }

  @Override
  public void checkStop() {
    if(stopped) {
      try {
        data.close();
      } catch(final Exception ex) {
        BaseX.debug(ex);
      }
      throw new ProgressException();
    }
  }
}
