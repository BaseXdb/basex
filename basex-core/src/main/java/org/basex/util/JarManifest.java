package org.basex.util;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

import java.io.InputStream;

import java.net.URL;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Manifest;

/**
 * Utility class to retrieve the manifest attributes of a JAR in the classpath.
 *
 * @author BaseX Team 2005-13, BSD License
 */
public final class JarManifest {
  /**
   * The main attributes of the BaseX JAR manifest.
   */
  public static final Map<String, String> basexManifestAttributes = unmodifiableMap(mainAttributes(JarManifest.class));

  private JarManifest() { }

  /**
   * Get the main attributes of the JAR containing the given class.
   *
   * @param cls class belonging to the JAR which manifest attributes should be read
   * @return empty map if no manifest is present
   */
  public static Map<String, String> mainAttributes(Class<?> cls) {
    final Manifest manifest = getManifest(cls);
    if (manifest == null) return emptyMap();
    final Map<String, String> result = new LinkedHashMap<String, String>();
    for (Map.Entry<Object, Object> attribute : manifest.getMainAttributes().entrySet())
      result.put(attribute.getKey().toString(), attribute.getValue().toString());
    return result;
  }

  /**
   * Retrieve the manifest of the JAR file where the given class is stored.
   *
   * @param cls class belonging to the JAR which manifest should be read
   * @return {@code null} if manifest cannot be read
   */
  public static Manifest getManifest(Class<?> cls) {
    try {
      final String jar = cls.getProtectionDomain().getCodeSource().getLocation().getFile();
      final Enumeration<URL> list = cls.getClassLoader().getResources("META-INF/MANIFEST.MF");
      while (list.hasMoreElements()) {
        final URL url = list.nextElement();
        if (url.getFile().contains(jar)) {
          final InputStream in = url.openStream();
          try {
            if (in != null) return new Manifest(in);
          } finally {
            in.close();
          }
        }
      }
    } catch (Exception e) {
      System.err.println(e);
    }
    return null;
  }
}
