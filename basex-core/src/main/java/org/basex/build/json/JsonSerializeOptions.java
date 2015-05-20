package org.basex.build.json;

import org.basex.util.options.*;

/**
 * Options for processing JSON documents.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public class JsonSerializeOptions extends Options {
  /** Option: merge type information. */
  public static final BooleanOption INDENT = new BooleanOption("indent", false);
}
