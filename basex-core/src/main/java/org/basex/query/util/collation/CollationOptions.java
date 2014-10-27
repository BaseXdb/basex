package org.basex.query.util.collation;

import java.text.*;

import org.basex.util.options.*;

/**
 * Collation options.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class CollationOptions extends Options {
  /**
   * Assigns the collation options to the specified collator.
   * @param coll collator
   * @return success flag
   */
  abstract boolean assign(final Collator coll);
}
