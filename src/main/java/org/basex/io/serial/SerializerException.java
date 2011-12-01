package org.basex.io.serial;

import java.io.IOException;
import org.basex.core.BaseXException;
import org.basex.query.util.Err;
import org.basex.util.TokenBuilder;

/**
 * This class signals that an exception occurred during query serialization.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class SerializerException extends IOException {
  /** Error reference. */
  private final Err err;

  /**
   * Default constructor.
   * @param er error reference
   * @param ext error extension
   */
  public SerializerException(final Err er, final Object... ext) {
    super(BaseXException.message(er.desc, ext));
    err = er;
  }

  /**
   * Returns the error.
   * @return error
   */
  public Err err() {
    return err;
  }

  @Override
  public String getLocalizedMessage() {
    return super.getMessage();
  }

  @Override
  public String getMessage() {
    return new TokenBuilder().add('[').add(err.qname().string()).
        add(']').add(getLocalizedMessage()).toString();
  }
}
