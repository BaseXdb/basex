package org.basex.query.util.pkg;

import org.basex.util.list.TokenList;

/**
 * Jar descriptor.
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public final class JarDesc {
  /** List of jar files. */
  public final TokenList jars = new TokenList();
  /** List of public classes. */
  public final TokenList classes = new TokenList();
}
