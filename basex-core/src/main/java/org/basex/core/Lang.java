package org.basex.core;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class loads language specific texts when the {@link #lang}
 * method is called for the first time.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Lang {
  /** Language suffix. */
  private static final String SUFFIX = "lang";
  /** Cached source files. */
  private static final HashMap<String, String> TEXTS = new HashMap<>();
  /** Checks which strings have been applied. */
  private static final HashMap<String, Boolean> CHECK = new HashMap<>();

  /** Private constructor. */
  private Lang() { }

  /* Reads the language file. */
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
      try(NewlineInput nli = new NewlineInput(is)) {
        for(String line; (line = nli.readLine()) != null;) {
          final int i = line.indexOf('=');
          if(i == -1 || Strings.startsWith(line, '#')) continue;
          final String key = line.substring(0, i).trim();
          String val = line.substring(i + 1).trim();
          if("langright".equals(key)) {
            Prop.langright = "true".equals(val);
          } else {
            if(val.contains("\\n")) val = val.replaceAll("\\\\n", Prop.NL);
            if(Prop.langkeys) val = '[' + key + ": " + val + ']';
            if(TEXTS.put(key, val) != null) {
              Util.errln("%." + SUFFIX + ": '%' is declared twice", lang, key);
            }
            CHECK.put(key, true);
          }
        }
      } catch(final IOException ex) {
        Util.errln(ex);
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
        Util.errln("%." + SUFFIX + ": '%' can be removed", Prop.language, s);
      }
      return "---";
    }

    final String val = TEXTS.get(key);
    if(val == null) {
      Util.errln("%." + SUFFIX + ": '%' is missing", Prop.language, key);
      return '[' + key + ']';
    }
    CHECK.remove(key);
    return val;
  }

  /**
   * Returns the specified string with some text extensions included.
   * @param key key
   * @param ext text text extensions
   * @return string
   */
  static synchronized String lang(final String key, final Object... ext) {
    return Util.info(lang(key), ext);
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
      if("jar".equals(url.getProtocol())) {
        final JarURLConnection conn = (JarURLConnection) url.openConnection();
        final String pre = conn.getEntryName();
        final JarFile jar = conn.getJarFile();

        final Enumeration<JarEntry> je = jar.entries();
        while(je.hasMoreElements()) {
          final JarEntry entry = je.nextElement();
          final String name = entry.getName();
          if(!name.startsWith(pre) || !name.endsWith(SUFFIX)) continue;
          final byte[] cont = new IOStream(jar.getInputStream(entry)).read();
          langs.add(name.replaceAll(".*/|." + SUFFIX, ""));
          creds.add(credits(cont));
        }
      } else {
        for(final IO f : ((IOFile) IO.get(url.toString())).children()) {
          langs.add(f.name().replace('.' + SUFFIX, ""));
          creds.add(credits(f.read()));
        }
      }
    } catch(final IOException ex) {
      Util.errln(ex);
    }
    return new String[][] { langs.finish(), creds.finish() };
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
  static void check() {
    read(Prop.language);
    final StringBuilder sb = new StringBuilder();
    final HashSet<String> set = new HashSet<>(TEXTS.keySet());

    final IOFile[] files = new IOFile("src/main/resources/lang").children();
    for(final IOFile file : files) {
      final String lang = file.name().replace('.' + SUFFIX, "");
      if(lang.equals(Prop.language)) continue;

      read(lang);
      for(final String text : set.toArray(new String[0])) {
        if(TEXTS.remove(text) == null) sb.append("- ").append(text).append('\n');
      }
      if(sb.length() != 0) {
        Util.err("Missing in %.lang:\n%", lang, sb);
        sb.setLength(0);
      }
      for(final String s : TEXTS.keySet()) {
        sb.append("- ").append(s).append('\n');
      }
      if(sb.length() != 0) {
        Util.err("Not defined in %.lang:\n%", lang, sb);
        sb.setLength(0);
      }
    }
  }
}
