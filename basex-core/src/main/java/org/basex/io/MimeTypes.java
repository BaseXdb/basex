package org.basex.io;

import org.basex.util.http.*;

/**
 * Obsolete class for retrieving mime types (new: {@link MediaType}).
 * Will be removed in Version 8.2 or later.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class MimeTypes {
  /** Private constructor. */
  private MimeTypes() { }

  /**
   * Returns the mime type for the suffix of the specified file path.
   * {@code application/octet-stream} is returned if no type is found.
   * @param path path to be checked
   * @return mime-type
   */
  @Deprecated
  public static String get(final String path) {
    return MediaType.get(path).toString();
  }
}
