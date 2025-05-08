package org.basex.query.util;

import org.basex.io.*;
import org.basex.query.value.item.*;

/**
 * Interface for resolving URIs in query modules.
 */
@FunctionalInterface
public interface UriResolver {
  /**
   * Locates a file, given the optional namespace URI and a path to the location.
   * @param path path (relative or absolute)
   * @param uri URI (can be {@code null})
   * @param base base URI (can be {@code null})
   * @return reference to resources
   */
  IO resolve(String path, String uri, Uri base);
}
