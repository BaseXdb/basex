package org.basex.io;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.zip.*;

import org.basex.util.*;

/**
 * Compression formats for single streams.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum Compression {
  /** GZIP. */
  GZIP(ZipEntry.DEFLATED, null, IO.GZSUFFIX, IO.TGZSUFFIX, 0x1F, 0x8B),
  /** Zstandard. */
  ZSTD(93, ExternalLib.AIRCOMPRESSOR, IO.ZSTSUFFIX, IO.TZSTSUFFIX, 0x28, 0xB5, 0x2F, 0xFD),
  /** XZ. */
  XZ(95, ExternalLib.XZ, IO.XZSUFFIX, IO.TXZSUFFIX, 0xFD, 0x37, 0x7A, 0x58, 0x5A, 0x00);

  /** Maximum length of a magic number. */
  public static final int MAGIC = 6;

  /** Compression method in ZIP archives. */
  public final int method;
  /** Required library ({@code null} if supported by the JDK). */
  private final ExternalLib lib;
  /** File suffix. */
  private final String suffix;
  /** File suffix of compressed TAR archives. */
  private final String tarSuffix;
  /** Magic number. */
  private final byte[] magic;

  /**
   * Constructor.
   * @param method compression method in ZIP archives
   * @param lib required library ({@code null} if supported by the JDK)
   * @param suffix file suffix
   * @param tarSuffix file suffix of compressed TAR archives
   * @param magic magic number
   */
  Compression(final int method, final ExternalLib lib, final String suffix, final String tarSuffix,
      final int... magic) {
    this.method = method;
    this.lib = lib;
    this.suffix = suffix;
    this.tarSuffix = tarSuffix;
    this.magic = new byte[magic.length];
    for(int m = 0; m < magic.length; m++) this.magic[m] = (byte) magic[m];
  }

  /**
   * Returns the compression with the specified name.
   * @param name name
   * @return compression, or {@code null} if the name is unknown
   */
  public static Compression get(final String name) {
    for(final Compression compr : values()) {
      if(compr.toString().equals(name)) return compr;
    }
    return null;
  }

  /**
   * Returns the compression with the specified ZIP method.
   * @param method compression method in ZIP archives
   * @return compression, or {@code null} if the method is unknown
   */
  public static Compression get(final int method) {
    for(final Compression compr : values()) {
      if(compr.method == method) return compr;
    }
    return null;
  }

  /**
   * Returns the compression whose magic number starts the specified header.
   * @param header header bytes
   * @return compression, or {@code null} if no magic number matches
   */
  public static Compression get(final byte[] header) {
    for(final Compression compr : values()) {
      if(Token.startsWith(header, compr.magic)) return compr;
    }
    return null;
  }

  /**
   * Returns the compression indicated by the suffix of a file name.
   * @param name lower-cased file name
   * @return compression, or {@code null} if the suffix is unknown
   */
  public static Compression file(final String name) {
    for(final Compression compr : values()) {
      if(name.endsWith(compr.suffix) || name.endsWith(compr.tarSuffix)) {
        return compr;
      }
    }
    return null;
  }

  /**
   * Checks if the file name indicates a compressed TAR archive.
   * @param name lower-cased file name
   * @return result of check
   */
  public boolean tar(final String name) {
    return name.endsWith(IO.TARSUFFIX + suffix) || name.endsWith(tarSuffix);
  }

  /**
   * Returns the first required class that is not found in the classpath.
   * @return class name, or {@code null} if the compression is available
   */
  public String missing() {
    return lib == null || lib.available() ? null : lib.missing();
  }

  /**
   * Returns a decompressing input stream.
   * @param is input stream
   * @return decompressing stream
   * @throws IOException I/O exception
   */
  public InputStream input(final InputStream is) throws IOException {
    return this == GZIP ? new GZIPInputStream(is) :
      (InputStream) create(lib.clazz(), new Class<?>[] { InputStream.class }, is);
  }

  /**
   * Returns a compressing output stream.
   * @param os output stream
   * @return compressing stream
   * @throws IOException I/O exception
   */
  public OutputStream output(final OutputStream os) throws IOException {
    return switch(this) {
      case GZIP -> new GZIPOutputStream(os);
      case ZSTD -> (OutputStream) create("io.airlift.compress.zstd.ZstdOutputStream",
          new Class<?>[] { OutputStream.class }, os);
      case XZ -> {
        final Object options = create("org.tukaani.xz.LZMA2Options", new Class<?>[0]);
        yield (OutputStream) create("org.tukaani.xz.XZOutputStream",
            new Class<?>[] { OutputStream.class, options.getClass().getSuperclass() }, os, options);
      }
    };
  }

  /**
   * Creates an instance of a library class.
   * @param name class name
   * @param types constructor parameter types
   * @param args constructor arguments
   * @return instance
   * @throws IOException I/O exception
   */
  private Object create(final String name, final Class<?>[] types, final Object... args)
      throws IOException {
    final String missing = missing();
    final Class<?> clazz = missing == null ? Reflect.find(name) : null;
    if(clazz == null) throw new IOException(Util.info("% compression requires missing class: %",
        this, missing != null ? missing : name));
    try {
      return clazz.getConstructor(types).newInstance(args);
    } catch(final InvocationTargetException ex) {
      final Throwable cause = ex.getCause();
      throw cause instanceof final IOException io ? io : new IOException(cause);
    } catch(final ReflectiveOperationException ex) {
      throw new IOException(ex);
    }
  }

  @Override
  public String toString() {
    return name().toLowerCase(Locale.ENGLISH);
  }
}
