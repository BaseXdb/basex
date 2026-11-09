package org.basex.query.value.item;

/**
 * Lazy item.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public interface Lazy {
  /**
   * Indicates if the contents of this item have been cached.
   * @return result of check
   */
  boolean isCached();

  /**
   * Indicates that the item will be accessed only once more, allowing its source to be streamed.
   */
  void once();
}
