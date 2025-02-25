package org.basex.query.util.parse;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Information required for parsing a module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ModInfo {
  /** Paths. */
  public final TokenList paths = new TokenList(1);
  /** URI. */
  public byte[] uri;
  /** Input info (can be {@code null}). */
  public InputInfo info;
}
