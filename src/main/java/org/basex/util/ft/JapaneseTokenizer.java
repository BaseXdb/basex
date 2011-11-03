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
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

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
  /** A part of speech, KIGOU(Mark). */
  private static final int HINSHI_MARK = 1;
  /** A part of speech, Others(only a mark is distinguished). */
  private static final int HINSHI_OTHERS = 0;
  /** Igo constructor. */
  private static Constructor<?> tgr;
  /** Igo instance. */
  private static Object tagger;
  /** Parse method. */
  private static Method parse;
  /** Surface field. */
  private static Field surface;
  /** feature field. */
  private static Field feature;
  /** Start field. */
  private static Field start;

  /** Token iterator. */
  private Iterator<Morpheme> tokens;
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
        if(tagger == null) {
          available = false;
          Util.errln("Could not initialize Igo Japanese lexer.");
        } else {
          parse = Reflect.method(clz, "parse", CharSequence.class);
          if(parse == null) {
            Util.errln("Could not initialize Igo lexer method.");
          }
          clz = Reflect.find("net.reduls.igo.Morpheme");
          surface = Reflect.field(clz, "surface");
          feature = Reflect.field(clz, "feature");
          start = Reflect.field(clz, "start");
        }
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

    final ArrayList<Morpheme> list = new ArrayList<Morpheme>();
    try {
      int prev = 0;
      for(int i = 0; i < morpheme.size(); i++) {
        final Object m = morpheme.get(i);
        final String srfc = surface.get(m).toString();
        final String ftr = feature.get(m).toString();
        final int hinshi = getHinshi(srfc, ftr);
        final int s = start.getInt(m);
        if(i != 0) {
          final int l = s - prev;
          if(l != 0) {
            list.add(new Morpheme(
                source.substring(s - 1, s + l - 1), HINSHI_MARK)
            );
          }
        }
        prev = srfc.length() + s;
        list.add(new Morpheme(srfc, hinshi));
      }
    } catch(final Exception ex) {
      Util.errln(Util.name(this) + ": " + ex);
    }
    tokens = list.iterator();

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
    final Morpheme m = tokens.next();
    String n = m.mSurface;
    pos++;
    if(special) {
      return token(n);
    }
    if(m.mHinshi == HINSHI_MARK) {
      return EMPTY;
    }
    if(uc) n = n.toUpperCase();
    if(lc || !cs) n = n.toLowerCase();
    return toHankaku(token(n));
  }

  @Override
  protected byte prec() {
    return 20;
  }

  @Override
  Collection<Language> languages() {
    return collection(LANG);
  }

  /**
   * Return the mark or not.
   * @param srfc Morpheme surface
   * @param ftr Morpheme feature
   * @return mark or not
   */
  private static int getHinshi(final String srfc, final String ftr) {
    int hinshi = 0;
    //morphological analyzer certainly returns
    //the single ascii char as a "noun".
    final byte[] s = token(srfc);
    if(s.length == 1 && !letter(s[0]) && !digit(s[0])) {
      hinshi = HINSHI_MARK;
    } else {
      final String[] parts = ftr.split(",");
      if(parts[0].equals("\u8A18\u53F7")) {
        hinshi = HINSHI_MARK;
      } else {
        hinshi = HINSHI_OTHERS;
      }
    }
    return hinshi;
  }

  /**
   * Converts to HANKAKU characters.
   * @param s Japanese text
   * @return result of conversion(->HANKAKU)
   */
  private static byte[] toHankaku(final byte[] s) {
    if(ascii(s)) return s;
    final TokenBuilder tb = new TokenBuilder(s.length);
    for(int p = 0; p < s.length; p += cl(s, p)) {
      final int c = cp(s, p);
      if(c >= 0xFF10 && c <= 0xFF19 || c >= 0xFF21 && c <= 0xFF3A
          || c >= 0xFF41 && c <= 0xFF5A) {
        tb.add(c - 0xFEE0);
      } else if(c == 0x3000) { // IDEOGRAPHIC SPACE
        tb.add(0x0020);
      } else if(c == 0xFF01) { // !
        tb.add(0x0021);
      } else if(c == 0x201D) { // "
        tb.add(0x0022);
      } else if(c == 0xFF03) { // #
        tb.add(0x0023);
      } else if(c == 0xFF04) { // $
        tb.add(0x0024);
      } else if(c == 0xFF05) { // %
        tb.add(0x0025);
      } else if(c == 0xFF06) { // &
        tb.add(0x0026);
      } else if(c == 0x2019) { // '
        tb.add(0x0027);
      } else if(c == 0xFF08) { // (
        tb.add(0x0028);
      } else if(c == 0xFF09) { // )
        tb.add(0x0029);
      } else if(c == 0xFF0A) { // *
        tb.add(0x002A);
      } else if(c == 0xFF0B) { // +
        tb.add(0x002B);
      } else if(c == 0xFF0C) { // ,
        tb.add(0x002C);
      } else if(c == 0xFF0D) { // -
        tb.add(0x002D);
      } else if(c == 0xFF0E) { // .
        tb.add(0x002E);
      } else if(c == 0xFF0F) { // /
        tb.add(0x002F);
      } else if(c == 0xFF1A) { // :
        tb.add(0x003A);
      } else if(c == 0xFF1B) { // ;
        tb.add(0x003B);
      } else if(c == 0xFF1C) { // <
        tb.add(0x003C);
      } else if(c == 0xFF1D) { // =
        tb.add(0x003D);
      } else if(c == 0xFF1E) { // >
        tb.add(0x003E);
      } else if(c == 0xFF1F) { // ?
        tb.add(0x003F);
      } else if(c == 0xFF20) { // @
        tb.add(0x0040);
      } else if(c == 0xFF3B) { // [
        tb.add(0x005B);
      } else if(c == 0xFFE5) { // \
        tb.add(0x005C);
      } else if(c == 0xFF3D) { // ]
        tb.add(0x005D);
      } else if(c == 0xFF3E) { // ^
        tb.add(0x005E);
      } else if(c == 0xFF3F) { // _
        tb.add(0x005F);
      } else if(c == 0xFF40) { // `
        tb.add(0x0060);
      } else if(c == 0xFF5B) { // {
        tb.add(0x007B);
      } else if(c == 0xFF5C) { // |
        tb.add(0x007C);
      } else if(c == 0xFF5D) { // }
        tb.add(0x007D);
      } else if(c == 0xFF5E) { // ~
        tb.add(0x007E);
      } else {
        tb.add(c);
      }
    }
    return tb.finish();
  }

  /** Morpheme class. */
  private static class Morpheme {
    /** Surface of Morpheme. */
    final String mSurface;
    /** Feature of Morpheme. */
    final int mHinshi;

    /**
     * Constructor.
     * @param srfc surface
     * @param hinshi a part of speech
    */
    Morpheme(final String srfc, final int hinshi) {
      mSurface = srfc;
      mHinshi = hinshi;
    }
  }
}
