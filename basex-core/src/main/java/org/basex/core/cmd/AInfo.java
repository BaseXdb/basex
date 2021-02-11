package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.util.*;

/**
 * Abstract class for database info.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class AInfo extends Command {
  /**
   * Protected constructor.
   * @param openDB requires opened database
   * @param args arguments
   */
  AInfo(final boolean openDB, final String... args) {
    super(Perm.READ, openDB, args);
  }

  /**
   * Formats the specified input.
   * @param tb token builder
   * @param key key
   * @param val value
   */
  static void info(final TokenBuilder tb, final Object key, final Object val) {
    tb.add(' ').add(key).add(COLS).add(val.toString().replaceAll("\r\n?|\n", " ")).add(NL);
  }

  /**
   * Formats the specified input.
   * @param tb token builder
   * @param prop meta property
   * @param meta meta data
   */
  static void info(final TokenBuilder tb, final MetaProp prop, final MetaData meta) {
    info(tb, prop.name(), prop.value(meta));
  }
}
