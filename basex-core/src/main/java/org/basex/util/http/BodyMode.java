package org.basex.util.http;

import org.basex.util.*;

/**
 * Representations of an HTTP response body.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum BodyMode {
  /** Parsed by media type. */ PARSE,
  /** Returned as string. */ TEXT,
  /** Returned as binary item. */ BINARY,
  /** Discarded. */ NONE;

  @Override
  public String toString() {
    return Enums.string(this);
  }
}
