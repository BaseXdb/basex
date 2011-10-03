package org.basex.util.ft;

import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.basex.core.Prop;
import org.basex.util.Reflect;
import org.basex.util.Util;
import org.basex.util.list.StringList;

/**
 * Japanese lexer using igo (http://igo.sourceforge.jp/).
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Toshio HIRAI
 */
class JapaneseTokenizer extends Tokenizer {
  /** Flag available. */
  private static boolean available = true;

  /** Name of the Igo tagger class. */
  private static final String PATTERN = "net.reduls.igo.Tagger";
  /** Name of Japanese dictionary. */
  private static final String LANG = "ja";

  /** Igo constructor. */
  private static Constructor<?> tgr;
  /** Igo instance. */
  private static Object tagger;
  /** Parse method. */
  private static Method parse;
  /** Surface field. */
  private static Field surface;
  /** Start field. */
  private static Field start;

  /** Token iterator. */
  private Iterator<String> tokens;
  /** Sensitivity flag. */
  private final boolean cs;
  /** Uppercase flag. */
  private final boolean uc;
  /** Lowercase flag. */
  private final boolean lc;
  /** Token position. */
  private int pos = -1;

  static {
    File dic = null;
    if(!Reflect.available(PATTERN)) {
      available = false;
    } else {
      dic = new File(LANG);
      if(!dic.exists()) {
        dic = new File(Prop.HOME, "etc/" + LANG);
        if(!dic.exists()) {
          available = false;
        }
      }
    }

    if(available) {
      Class<?> clz = Reflect.find(PATTERN);
      if(clz == null) {
        Util.errln("Could not initialize Igo Japanese lexer.");
      } else {
        tgr = Reflect.find(clz, String.class);
        tagger = Reflect.get(tgr, dic.toString());
        parse = Reflect.method(clz, "parse", CharSequence.class);
        if(parse == null) {
          Util.errln("Could not initialize Igo lexer method.");
        }
        clz = Reflect.find("net.reduls.igo.Morpheme");
        surface = Reflect.field(clz, "surface");
        start = Reflect.field(clz, "start");
      }
    }
  }

  /**
   * Checks if the library is available.
   * @return result of check
   */
  static boolean available() {
    return available;
  }

  /**
   * Constructor.
   * @param fto (optional) full-text options
   */
  JapaneseTokenizer(final FTOpt fto) {
    lc = fto != null && fto.is(LC);
    uc = fto != null && fto.is(UC);
    cs = fto != null && fto.is(CS);
  }

  @Override
  Tokenizer get(final FTOpt f) {
    return new JapaneseTokenizer(f);
  }

  @Override
  public JapaneseTokenizer init(final byte[] txt) {
    final String source = string(txt);
    final ArrayList<?> morpheme =
      (ArrayList<?>) Reflect.invoke(parse, tagger, source);

    final StringList sl = new StringList();
    try {
      int prev = 0;
      for(int i = 0; i < morpheme.size(); i++) {
        final Object m = morpheme.get(i);
        final String srfc = surface.get(m).toString();
        final int s = start.getInt(m);
        if(i != 0) {
          final int l = s - prev;
          if(l != 0) {
            sl.add(source.substring(s - 1, s + l - 1));
          }
        }
        prev = srfc.length() + s;
        sl.add(srfc);
      }
    } catch(final Exception ex) {
      Util.errln(Util.name(this) + ": " + ex);
    }
    tokens = sl.iterator();

    return this;
  }

  @Override
  public boolean hasNext() {
    return tokens.hasNext();
  }

  @Override
  public FTSpan next() {
    return new FTSpan(nextToken(), pos, false);
  }

  @Override
  public byte[] nextToken() {
    String n = tokens.next();
    pos++;
    if(uc) n = n.toUpperCase();
    if(lc || !cs) n.toLowerCase();
    return token(n);
  }

  @Override
  protected byte prec() {
    return 20;
  }

  @Override
  Collection<Language> languages() {
    return collection(LANG);
  }
}
