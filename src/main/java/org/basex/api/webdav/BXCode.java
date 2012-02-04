package org.basex.api.webdav;

import java.io.IOException;

import org.basex.server.Session;
import org.basex.util.Util;

import com.bradmcevoy.http.exceptions.BadRequestException;


/**
 * Code container.
 * @param <E> return type
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class BXCode<E> {
  /** Resource. */
  private final BXResource resource;
  /** Session instance. */
  protected Session s;

  /**
   * Constructor.
   * @param res resource
   */
  protected BXCode(final BXResource res) {
    resource = res;
  }

  /**
   * Runs the contained code.
   * @return result
   * @throws BadRequestException bad request exception
   */
  public E eval() throws BadRequestException {
    try {
      s = resource.session.login();
      try {
        final E ret = get();
        if(ret == null) run();
        return ret;
      } finally {
        s.close();
      }
    } catch(final IOException ex) {
      Util.errln(ex);
      throw new BadRequestException(resource, ex.getMessage());
    }
  }

  /**
   * Runs the contained code, throwing no exception.
   * @return result
   */
  public E evalNoEx() {
    try {
      return eval();
    } catch(final BadRequestException ex) {
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
