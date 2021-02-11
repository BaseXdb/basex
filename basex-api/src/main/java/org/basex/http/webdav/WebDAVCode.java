package org.basex.http.webdav;

import java.io.*;

import org.basex.util.*;

import com.bradmcevoy.http.exceptions.*;

/**
 * Code container.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 * @param <E> return type
 */
abstract class WebDAVCode<E> {
  /** Resource. */
  private final WebDAVResource resource;

  /**
   * Constructor.
   * @param resource resource
   */
  WebDAVCode(final WebDAVResource resource) {
    this.resource = resource;
  }

  /**
   * Runs the contained code.
   * @return result (can be {@code null})
   * @throws BadRequestException bad request exception
   */
  final E eval() throws BadRequestException {
    try {
      final E ret = get();
      if(ret == null) run();
      return ret;
    } catch(final IOException ex) {
      throw new BadRequestException(resource, Util.message(ex));
    }
  }

  /**
   * Method to run, returning some output.
   * @return result
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  E get() throws IOException {
    return null;
  }

  /**
   * Method to run.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  void run() throws IOException {
  }
}
