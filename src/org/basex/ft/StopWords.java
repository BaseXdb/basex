package org.basex.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.IO;
import org.basex.util.Set;

/**
 * Simple stop words set for fulltext requests.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class StopWords extends Set {
  /**
   * Reads a stop words file.
   * @param fl file reference
   * @param e except flag
   * @return true if everything went alright
   */
  public boolean read(final IO fl, final boolean e) {
    try {
      for(final byte[] sl : split(norm(fl.content()), ' ')) {
        if(e) delete(sl);
        else if(id(sl) == 0) add(sl);
      }
      return true;
    } catch(final IOException ex) {
      return false;
    }
  }
}
