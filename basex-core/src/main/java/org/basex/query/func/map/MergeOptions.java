package org.basex.query.func.map;

import org.basex.query.value.map.*;
import org.basex.util.options.*;

/**
 * Options for parsing JSON documents.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class MergeOptions extends Options {
  /** Option: handle duplicates. */
  public static final EnumOption<MergeDuplicates> DUPLICATES =
      new EnumOption<>("duplicates", MergeDuplicates.USE_FIRST);
}
