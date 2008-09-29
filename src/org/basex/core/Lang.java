package org.basex.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import org.basex.BaseX;
import org.basex.util.Token;

/**
 * This class loads language specific data when the {@link #lang}
 * method is called for the first time.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Andreas Weiler
 */
public final class Lang {
  /** Throws an error if this class is loaded. This flag is used to check
   * if non-verbose processes access the language files. */
  private static final boolean DISALLOW = false;
  /** Checks which strings of the language file are not used. */
  private static final boolean CHECK = true;
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
  public static void read(final String lang, final boolean chk) {
    try {
      if(DISALLOW) throw new Error("Language file was accessed.");
      if(chk) check = new HashMap<String, Boolean>();
      
      final String path = "lang/" + lang + ".lang";
      final URL url = BaseX.class.getResource(path);
      if(url != null) {
        final InputStream is = (InputStream) url.getContent();
        final InputStreamReader isr = new InputStreamReader(is, Token.UTF8);
        final BufferedReader br = new BufferedReader(isr);
        String line;
        while((line = br.readLine()) != null) {
          final int i = line.indexOf("=");
          if(i == -1 || line.startsWith("#")) continue;
          final String key = line.substring(0, i);
          String val = line.substring(i + 1);
          if(val.contains("\\n")) val = val.replaceAll("\\\\n", "\n");
          if(texts.get(key) != null) {
            if(chk) BaseX.errln("%.lang: '%' assigned twice", lang, key);
          } else {
            texts.put(key, val);
          }
          if(chk) check.put(key, true);
        }
        br.close();
      } else {
        BaseX.errln("%.lang not found.", lang);
      }
    } catch(final IOException ex) {
      BaseX.errln(ex.toString());
    }
  }

  /**
   * Returns the specified string.
   * @param key key
   * @return string
   */
  public static String lang(final String key) {
    if(key == null) {
      if(CHECK && check.size() != 0) {
        final Iterator<String> it = check.keySet().iterator();
        while(it.hasNext()) {
          BaseX.errln("%.lang: '%' not used", Prop.language, it.next());
        }
      }
      return null;
    }

    final String val = texts.get(key);
    if(val == null) {
      if(texts.size() != 0) {
        BaseX.errln("%.lang: '%' missing", Prop.language, key);
        //if(Prop.language.equals("English")) return "?????";
        //read("English", false);
        //val = texts.get(key);
      }
      return "?????";
    }
    if(CHECK) check.remove(key);
    return val;
  }
}
