package org.basex.query.util.pkg;

import java.io.*;

/**
 * Package component.
 *
 * @author BaseX Team 2005-17, BSD License
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
    final int i = file.lastIndexOf(File.separator);
    return i == -1 ? file : file.substring(i + 1, file.length());
  }
}
