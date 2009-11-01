package org.basex.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.basex.BaseX;
import org.basex.io.IOFile;
import org.basex.util.StringList;
import org.basex.util.Token;

/**
 * This class loads language specific texts when the {@link #lang}
 * method is called for the first time.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public final class Lang {
  /** Throws an error if this class is loaded. This flag is used to check
   * if non-verbose processes access the language files. */
  private static final boolean DISALLOW = false;
  /** Checks which strings of the language file are not used. */
  private static final boolean CHECK = true;
  /** Language suffix. */
  private static final String SUFFIX = "lang";

  /** Cached source files. */
  private static HashMap<String, String> texts = new HashMap<String, String>();
  /** Checks which strings have been applied. */
  private static HashMap<String, Boolean> check;

  /** Private constructor. */
  private Lang() { }

  /** Reads the language file. */
  static { read(Prop.language, CHECK); }

  /**
   * Reads the specified language file.
   * @param lang language
   * @param chk check flag
   */
  private static synchronized void read(final String lang, final boolean chk) {
    try {
      if(DISALLOW) throw new Error("Language file was accessed.");
      if(chk) check = new HashMap<String, Boolean>();

      final String path = SUFFIX + "/" + lang + "." + SUFFIX;
      final URL url = BaseX.class.getResource(path);
      if(url == null) {
        Main.errln("%." + SUFFIX + " not found.", lang);
      } else {
        final BufferedReader br = new BufferedReader(new InputStreamReader(
            (InputStream) url.getContent(), Token.UTF8));
        String line;
        while((line = br.readLine()) != null) {
          final int i = line.indexOf('=');
          if(i == -1 || line.startsWith("#")) continue;
          final String key = line.substring(0, i);
          String val = line.substring(i + 1);
          if(val.contains("\\n")) val = val.replaceAll("\\\\n", Prop.NL);
          if(Prop.langkeys) val = "[" + key + ": " + val + "]";
          if(texts.get(key) == null) {
            texts.put(key, val);
          } else if(chk) {
            Main.errln("%." + SUFFIX + ": '%' assigned twice", lang, key);
          }
          if(chk) check.put(key, true);
        }
        br.close();
      }
    } catch(final IOException ex) {
      Main.errln(ex);
    }
  }

  /**
   * Returns the specified string.
   * @param key key
   * @return string
   */
  public static synchronized String lang(final String key) {
    if(key == null) {
      if(CHECK && check.size() != 0) {
        final Iterator<String> it = check.keySet().iterator();
        while(it.hasNext()) Main.errln("%." + SUFFIX + ": '%' not used",
            Prop.language, it.next());
      }
      return null;
    }

    final String val = texts.get(key);
    if(val == null) {
      if(texts.size() != 0) Main.errln("%." + SUFFIX + ": '%' missing",
          Prop.language, key);
      return "?????";
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
  public static synchronized String lang(final String key, final Object... e) {
    return Main.info(lang(key), e);
  }

  /**
   * Parse all available language files and return the names and credits.
   * @return language arrays
   */
  public static synchronized String[][] parse() {
    final StringList langs = new StringList();
    final StringList creds = new StringList();

    try {
      // supported protocols: jar and file
      final URL url = BaseX.class.getResource(SUFFIX);
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
          jar.getInputStream(entry).read(cont);
          langs.add(name.replaceAll(".*/|." + SUFFIX, ""));
          creds.add(credits(cont));
        }
      } else {
        for(final File f : new File(url.getFile()).listFiles()) {
          langs.add(f.getName().replaceAll("." + SUFFIX, ""));
          creds.add(credits(new IOFile(f).content()));
        }
      }
    } catch(final IOException ex) {
      Main.errln(ex);
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
}
