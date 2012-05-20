package org.basex.core;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class loads language specific texts when the {@link #lang}
 * method is called for the first time.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public final class Lang {
  /** Language suffix. */
  private static final String SUFFIX = "lang";
  /** Cached source files. */
  private static final HashMap<String, String> TEXTS = new HashMap<String, String>();
  /** Checks which strings have been applied. */
  private static final HashMap<String, Boolean> CHECK = new HashMap<String, Boolean>();

  /** Private constructor. */
  private Lang() { }

  /** Reads the language file. */
  static { read(Prop.language); }

  /**
   * Reads the specified language file.
   * @param lang language
   */
  private static synchronized void read(final String lang) {
    TEXTS.clear();
    CHECK.clear();
    final String path = '/' + SUFFIX + '/' + lang + '.' + SUFFIX;
    final InputStream is = Lang.class.getResourceAsStream(path);
    if(is == null) {
      Util.errln(path + " not found.");
    } else {
      BufferedReader br = null;
      try {
        br = new BufferedReader(new InputStreamReader(is, Token.UTF8));
        for(String line; (line = br.readLine()) != null;) {
          final int i = line.indexOf('=');
          if(i == -1 || line.startsWith("#")) continue;
          final String key = line.substring(0, i).trim();
          String val = line.substring(i + 1).trim();
          if(val.contains("\\n")) val = val.replaceAll("\\\\n", Prop.NL);
          if(Prop.langkeys) val = '[' + key + ": " + val + ']';
          if(TEXTS.put(key, val) != null) {
            Util.errln("%." + SUFFIX + ": '%' found twice in language file", lang, key);
            Util.stack(10);
            System.exit(0);
          }
          CHECK.put(key, true);
        }
      } catch(final IOException ex) {
        Util.errln(ex);
      } finally {
        if(br != null) try { br.close(); } catch(final IOException ex) { }
      }
    }
  }

  /**
   * Returns the specified string.
   * @param key key
   * @return string
   */
  static synchronized String lang(final String key) {
    if(key == null) {
      for(final String s : CHECK.keySet()) {
        Util.errln("%." + SUFFIX + ": '%' not defined in Text class", Prop.language, s);
        Util.stack(10);
        System.exit(0);
      }
      return null;
    }

    final String val = TEXTS.get(key);
    if(val == null) {
      Util.errln("%." + SUFFIX + ": '%' missing in Text class", Prop.language, key);
      Util.stack(10);
      System.exit(0);
      return '[' + key + ']';
    }
    CHECK.remove(key);
    return val;
  }

  /**
   * Returns the specified string with some text extensions included.
   * @param key key
   * @param e text text extensions
   * @return string
   */
  static synchronized String lang(final String key, final Object... e) {
    return Util.info(lang(key), e);
  }

  /**
   * Parses all available language files and returns the names and credits.
   * @return language arrays
   */
  public static synchronized String[][] parse() {
    final StringList langs = new StringList();
    final StringList creds = new StringList();

    try {
      // supported protocols: jar and file
      final URL url = Lang.class.getResource('/' + SUFFIX);
      if(url.getProtocol().equals("jar")) {
        final JarURLConnection conn = (JarURLConnection) url.openConnection();
        final String pre = conn.getEntryName();
        final JarFile jar = conn.getJarFile();

        final Enumeration<JarEntry> je = jar.entries();
        while(je.hasMoreElements()) {
          final JarEntry entry = je.nextElement();
          final String name = entry.getName();
          if(!name.startsWith(pre) || !name.endsWith(SUFFIX)) continue;

          final byte[] cont = new byte[(int) entry.getSize()];
          new DataInputStream(jar.getInputStream(entry)).readFully(cont);
          langs.add(name.replaceAll(".*/|." + SUFFIX, ""));
          creds.add(credits(cont));
        }
      } else {
        for(final IO f : new IOFile(IOUrl.file(url.toString())).children()) {
          langs.add(f.name().replaceAll('.' + SUFFIX, ""));
          creds.add(credits(f.read()));
        }
      }
    } catch(final IOException ex) {
      Util.errln(ex);
    }
    return new String[][] { langs.toArray(), creds.toArray() };
  }

  /**
   * Returns the credits from the specified file.
   * @param cont content
   * @return credits
   */
  private static synchronized String credits(final byte[] cont) {
    final StringTokenizer st = new StringTokenizer(Token.string(cont), "\n");
    st.nextToken();
    return st.nextToken().replaceAll("# ", "");
  }

  /**
   * Checks the existing language files for correctness and completeness.
   */
  public static void check() {
    read("English");
    final StringBuilder sb = new StringBuilder();
    final HashSet<String> set = new HashSet<String>();
    for(final String s : TEXTS.keySet()) set.add(s);

    final IOFile[] files = new IOFile("src/main/resources/lang").children();
    for(final IOFile f : files) {
      final String lang = f.name().replace("." + SUFFIX, "");
      if(lang.equals("English")) continue;
      read(lang);
      for(final Object o : set.toArray()) {
        if(TEXTS.remove(o) == null) sb.append("- " + o + "\n");
      }
      if(sb.length() != 0) {
        Util.err("Missing in %.lang:\n%", lang, sb);
        sb.setLength(0);
      }
      for(final String s : TEXTS.keySet()) {
        sb.append("- " + s + "\n");
      }
      if(sb.length() != 0) {
        Util.err("Not defined in %.lang:\n%", lang, sb);
        sb.setLength(0);
      }
    }
  }
}
