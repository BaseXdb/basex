package org.basex.util;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

/**
 * Utility class to retrieve the manifest attributes of a JAR in the classpath.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public final class JarManifest {
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
          final InputStream in = url.openStream();
          try {
            m = new Manifest(in).getMainAttributes();
            break;
          } finally {
            in.close();
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
   * @return value, or {@code null}
   */
  public static Object get(final String key) {
    if(MAP != null) {
      for(final Object o : MAP.keySet()) {
        if(key.equals(o.toString())) return MAP.get(o);
      }
    }
    return null;
  }
}
