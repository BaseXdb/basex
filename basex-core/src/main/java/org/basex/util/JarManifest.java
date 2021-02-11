package org.basex.util;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;
import java.util.jar.Attributes.Name;

/**
 * Utility class to retrieve the manifest attributes of a JAR in the classpath.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
final class JarManifest {
  /** The main attributes of the JAR manifest. */
  private static final Attributes MAP;

  /** Private constructor. */
  private JarManifest() { }

  static {
    Attributes map = null;
    final URL location = Prop.LOCATION;
    if(location != null) {
      final String jar = location.getFile();
      try {
        final ClassLoader cl = JarManifest.class.getClassLoader();
        final Enumeration<URL> list = cl.getResources("META-INF/MANIFEST.MF");
        while(list.hasMoreElements()) {
          final URL url = list.nextElement();
          if(!url.getFile().contains(jar)) continue;
          try(InputStream in = url.openStream()) {
            map = new Manifest(in).getMainAttributes();
            break;
          }
        }
      } catch(final IOException ex) {
        Util.stack(ex);
      }
    }
    MAP = map != null ? map : new Attributes(0);
  }

  /**
   * Returns the manifest value for the specified key.
   * @param key key
   * @return value or {@code null}
   */
  public static String get(final String key) {
    return MAP.getValue(key);
  }

  /**
   * Returns the manifest value for the specified key.
   * @param key key
   * @return value or {@code null}
   */
  public static String get(final Name key) {
    return MAP.getValue(key);
  }
}
