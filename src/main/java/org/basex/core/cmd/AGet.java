package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.util.Locale;

import org.basex.core.Command;

/**
 * Abstract class for option commands.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class AGet extends Command {
  /**
   * Default constructor.
   * @param f command flags
   * @param a arguments
   */
  AGet(final int f, final String... a) {
    super(f, a);
  }

  /**
   * Creates an error message for an unknown key and returns {@code false}.
   * @return false
   */
  final boolean whichKey() {
    final String key = args[0].toUpperCase(Locale.ENGLISH);
    final String sim = prop.similar(key);
    return sim != null ? error(UNKNOWN_OPT_SIMILAR_X, key, sim) :
      error(UNKNOWN_OPTION_X, key);
  }
}
