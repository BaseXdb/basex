package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.util.Util.*;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.basex.io.*;
import org.basex.util.Token;
import org.basex.util.list.StringList;

/**
 * This class loads language specific texts when the {@link #lang}
 * method is called for the first time.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public final class Lang {
  /** Throws an error if this class is loaded. This flag is used to check
   * if non-printing processes access the language files. */
  private static final boolean DISALLOW = false;
  /** Checks which strings of the language file are not used. */
  private static final boolean CHECK = true;
  /** Language suffix. */
  private static final String SUFFIX = "lang";

  /** Cached source files. */
  private static final HashMap<String, String> TETXTS =
    new HashMap<String, String>();
  /** Checks which strings have been applied. */
  private static HashMap<String, Boolean> check;

  /** Private constructor. */
  private Lang() { }

  /** Reads the language file. */
  static { read(language, CHECK); }

  /**
   * Reads the specified language file.
   * @param lang language
   * @param chk check flag
   */
  private static synchronized void read(final String lang, final boolean chk) {
    BufferedReader br = null;
    try {
      if(DISALLOW) throw new Error("Language file was accessed.");
      if(chk) check = new HashMap<String, Boolean>();

      final String path = '/' + SUFFIX + '/' + lang + '.' + SUFFIX;
      final InputStream is = Lang.class.getResourceAsStream(path);
      if(is == null) {
        errln(path + " not found.");
      } else {
        br = new BufferedReader(new InputStreamReader(is, Token.UTF8));
        for(String line; (line = br.readLine()) != null;) {
          final int i = line.indexOf('=');
          if(i == -1 || line.startsWith("#")) continue;
          final String key = line.substring(0, i).trim();
          String val = line.substring(i + 1).trim();
          if(val.contains("\\n")) val = val.replaceAll("\\\\n", Prop.NL);
          if(langkeys) val = '[' + key + COLS + val + ']';
          if(TETXTS.get(key) == null) {
            TETXTS.put(key, val);
          } else if(chk) {
            errln("%." + SUFFIX + ": '%' assigned twice", lang, key);
          }
          if(chk) check.put(key, true);
        }
      }
    } catch(final IOException ex) {
      errln(ex);
    } finally {
      if(br != null) try { br.close(); } catch(final IOException ex) { }
    }
  }

  /**
   * Returns the specified string.
   * @param key key
   * @return string
   */
  static synchronized String lang(final String key) {
    if(key == null) {
      if(CHECK && !check.isEmpty()) {
        for(final String s : check.keySet())
          errln("%." + SUFFIX + ": '%' not used", language, s);
      }
      return null;
    }

    final String val = TETXTS.get(key);
    if(val == null) {
      if(!TETXTS.isEmpty())
        errln("%." + SUFFIX + ": '%' missing", language, key);
      return '[' + key + ']';
    }
    if(CHECK) check.remove(key);
    return val;
  }

  /**
   * Returns the specified string with some text extensions included.
   * @param key key
   * @param e text text extensions
   * @return string
   */
  static synchronized String lang(final String key, final Object... e) {
    return info(lang(key), e);
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
      errln(ex);
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
}
