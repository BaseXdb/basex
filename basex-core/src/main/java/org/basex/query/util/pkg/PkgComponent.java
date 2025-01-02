package org.basex.query.util.pkg;

import java.io.*;

/**
 * Package component.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
final class PkgComponent {
  /** Namespace URI. */
  String uri;
  /** Component file. */
  String file;

  /**
   * Extracts the component file name from the component path.
   * @return name
   */
  String name() {
    return file.substring(file.lastIndexOf(File.separator) + 1);
  }
}
