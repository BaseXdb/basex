package org.basex.http.webdav;

import java.io.*;

import org.basex.server.*;
import org.basex.util.*;

import com.bradmcevoy.http.exceptions.*;

/**
 * Code container.
 *
 * @param <E> return type
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
abstract class BXCode<E> {
  /** Resource. */
  private final BXAbstractResource resource;

  /**
   * Constructor.
   * @param resource resource
   */
  BXCode(final BXAbstractResource resource) {
    this.resource = resource;
  }

  /**
   * Runs the contained code.
   * @return result
   * @throws BadRequestException bad request exception
   * @throws NotAuthorizedException not authorized exception
   */
  public final E eval() throws BadRequestException, NotAuthorizedException {
    try {
      final E ret = get();
      if (ret == null) run();
      return ret;
    } catch(final LoginException ex) {
      throw new NotAuthorizedException(resource);
    } catch(final IOException ex) {
      Util.errln(ex);
      throw new BadRequestException(resource, ex.getMessage());
    }
  }

  /**
   * Runs the contained code, throwing no exception.
   * @return result
   */
  public final E evalNoEx() {
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
  public E get() throws IOException {
    return null;
  }

  /**
   * Method to run.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void run() throws IOException {
  }
}
