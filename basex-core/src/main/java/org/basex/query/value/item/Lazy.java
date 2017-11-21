package org.basex.query.value.item;

/**
 * Lazy item.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public interface Lazy {
  /**
   * Indicates if the contents of this item have been cached.
   * @return result of check
   */
  boolean isCached();
}
