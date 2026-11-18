package org.basex.query.util;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Interface for resolving URIs in query modules.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
@FunctionalInterface
public interface UriResolver {
  /**
   * Locates a file, given the optional namespace URI and a path to the location.
   * @param path path (relative or absolute)
   * @param uri URI (can be {@code null})
   * @param base base URI (can be {@code null})
   * @return reference to resources, or {@code null} to apply the default resolution
   * @throws QueryException query exception
   */
  IO resolve(String path, String uri, Uri base) throws QueryException;
}
