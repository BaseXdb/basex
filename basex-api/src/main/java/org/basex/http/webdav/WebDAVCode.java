package org.basex.http.webdav;

import java.io.*;

import org.basex.server.*;
import org.basex.util.*;

import com.bradmcevoy.http.exceptions.*;

/**
 * Code container.
 *
 * @author BaseX Team 2005-17, BSD License
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
   * @throws NotAuthorizedException not authorized exception
   */
  final E eval() throws BadRequestException, NotAuthorizedException {
    try {
      final E ret = get();
      if(ret == null) run();
      return ret;
    } catch(final LoginException ex) {
      throw new NotAuthorizedException(Util.message(ex), resource);
    } catch(final IOException ex) {
      throw new BadRequestException(resource, Util.message(ex));
    }
  }

  /**
   * Runs the contained code, throwing no exception.
   * @return result (can be {@code null})
   */
  final E evalNoEx() {
    try {
      return eval();
    } catch(final Exception ex) {
      Util.errln(ex);
      return null;
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
