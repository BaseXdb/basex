package org.basex.io.serial;

import org.basex.util.options.Options.*;

/**
 * Pre-defined serialization parameters.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public enum SerializerMode {
  /** Default serialization. */
  DEFAULT {
    @Override
    void init(final SerializerOptions options) {
    }
  },
  /** Default serialization with indentation. */
  INDENT {
    @Override
    void init(final SerializerOptions options) {
      options.set(SerializerOptions.INDENT, YesNo.YES);
    }
  },
  /** Debugging (adaptive method). */
  DEBUG {
    @Override
    void init(final SerializerOptions options) {
      options.set(SerializerOptions.METHOD, SerialMethod.ADAPTIVE);
    }
  },
  /** API result retrieval. Also used by XQJ. */
  API {
    @Override
    void init(final SerializerOptions options) {
      options.set(SerializerOptions.BINARY, YesNo.NO);
    }
  };

  /** Options (lazy instantiation). */
  private SerializerOptions options;

  /**
   * Initializes serialization parameters.
   * @param sopts options
   */
  abstract void init(SerializerOptions sopts);

  /**
   * Returns serialization parameters.
   * @return parameters
   */
  public final SerializerOptions get() {
    if(options == null) {
      options = new SerializerOptions();
      init(options);
    }
    return options;
  }
}
