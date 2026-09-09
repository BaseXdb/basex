package org.basex.io.in;

import java.io.*;
import java.lang.reflect.*;

import org.basex.util.*;

/**
 * Zstandard input stream, based on the optional Aircompressor library.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ZstdInputStream extends FilterInputStream {
  /** Constructor of the Zstandard implementation (can be {@code null}). */
  private static final Constructor<?> CONSTRUCTOR;

  static {
    Constructor<?> constructor = null;
    final Class<?> clazz = Reflect.find(ExternalLib.AIRCOMPRESSOR.clazz());
    if(clazz != null) {
      try {
        constructor = clazz.getConstructor(InputStream.class);
      } catch(final NoSuchMethodException ex) {
        Util.debug(ex);
      }
    }
    CONSTRUCTOR = constructor;
  }

  /**
   * Constructor.
   * @param is input stream
   * @throws IOException I/O exception
   */
  public ZstdInputStream(final InputStream is) throws IOException {
    super(stream(is));
  }

  /**
   * Returns a Zstandard input stream.
   * @param is input stream
   * @return stream
   * @throws IOException I/O exception
   */
  private static InputStream stream(final InputStream is) throws IOException {
    if(CONSTRUCTOR == null) {
      throw new IOException(Util.info("Zstandard support requires missing class: %.",
          ExternalLib.AIRCOMPRESSOR.clazz()));
    }
    try {
      return (InputStream) CONSTRUCTOR.newInstance(is);
    } catch(final ReflectiveOperationException ex) {
      throw new IOException(ex);
    }
  }
}
