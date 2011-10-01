package org.basex.util.ft;

import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

import org.basex.core.Prop;
import org.basex.util.Reflect;
import org.basex.util.Util;
import org.basex.util.list.StringList;

/**
 * Japanese Lexer using igo (http://igo.sourceforge.jp/).
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Toshio HIRAI
 */
public class IgoLexer implements Iterator<FTSpan> {
  /** Name of the Igo tagger class. */
  private static final String PATTERN = "net.reduls.igo.Tagger";
  /** Name of Japanese dictionary. */
  private static final String DIC = "ipadic";

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
  private final Iterator<String> tokens;
  /** Sensitivity flag. */
  private final boolean cs;
  /** Uppercase flag. */
  private final boolean uc;
  /** Lowercase flag. */
  private final boolean lc;
  /** Token position. */
  private int pos = -1;

  /** Flag available. */
  static boolean isAvailable = true;

  static {
    File dic = null;
    if(!Reflect.available(PATTERN)) {
      isAvailable = false;
    } else {
      dic = new File(DIC);
      if(!dic.exists()) {
        dic = new File(Prop.HOME, "etc/" + DIC);
        if(!dic.exists()) {
          isAvailable = false;
        }
      }
    }

    if(isAvailable) {
      final Class<?> clz = Reflect.find(PATTERN);
      if(clz == null) {
        Util.errln("Could not initialize Igo Japanese lexer.");
      } else {
        tgr = Reflect.find(clz, String.class);
        tagger = Reflect.get(tgr, dic.toString());
        parse = Reflect.method(clz, "parse", CharSequence.class);
        if(parse == null) {
          Util.errln("Could not initialize Igo lexer method.");
        }
        final Class<?> clazz = Reflect.find("net.reduls.igo.Morpheme");
        surface = Reflect.field(clazz, "surface");
        start = Reflect.field(clazz, "start");
      }
    }
  }

  /**
   * Constructor.
   * @param txt input text
   * @param fto full-text options
   */
  IgoLexer(final byte[] txt, final FTOpt fto) {
    lc = fto != null && fto.is(LC);
    uc = fto != null && fto.is(UC);
    cs = fto != null && fto.is(CS);

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
      Util.errln("Could not retrieve field from morpheme instance.");
    }
    tokens = sl.iterator();
  }

  @Override
  public boolean hasNext() {
    return tokens.hasNext();
  }

  @Override
  public FTSpan next() {
    return new FTSpan(nextToken(), pos, false);
  }

  /**
   * Returns the next token. May be called as an alternative to {@link #next}
   * to avoid the creation of new {@link FTSpan} instances.
   * @return token
   */
  public byte[] nextToken() {
    String n = tokens.next();
    pos++;
    if(uc) n = n.toUpperCase();
    if(lc || !cs) n.toLowerCase();
    return token(n);
  }

  @Override
  public final void remove() {
    Util.notimplemented();
  }
}
