package org.basex.util;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.*;

/**
 * Utility class to retrieve the manifest attributes of a JAR in the classpath.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Dimitar Popov
 */
final class JarManifest {
  /** The main attributes of the JAR manifest. */
  private static final Attributes MAP;

  /** Private constructor. */
  private JarManifest() { }

  static {
    Attributes m = null;
    final URL loc = Prop.LOCATION;
    if(loc != null) {
      final String jar = loc.getFile();
      try {
        final ClassLoader cl = JarManifest.class.getClassLoader();
        final Enumeration<URL> list = cl.getResources("META-INF/MANIFEST.MF");
        while(list.hasMoreElements()) {
          final URL url = list.nextElement();
          if(!url.getFile().contains(jar)) continue;
          try(final InputStream in = url.openStream()) {
            m = new Manifest(in).getMainAttributes();
            break;
          }
        }
      } catch(final IOException ex) {
        Util.errln(ex);
      }
    }
    MAP = m;
  }

  /**
   * Returns the manifest value for the specified key.
   * @param key key
   * @return value or {@code null}
   */
  public static Object get(final String key) {
    if(MAP != null) {
      for(final Entry<Object, Object> entry : MAP.entrySet()) {
        if(key.equals(entry.getKey().toString())) return entry.getValue();
      }
    }
    return null;
  }
}
