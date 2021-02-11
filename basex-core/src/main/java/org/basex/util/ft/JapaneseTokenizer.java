package org.basex.util.ft;

import static org.basex.util.Token.*;
import static org.basex.util.FTToken.*;
import static org.basex.util.ft.FTFlag.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.io.*;
import org.basex.util.*;

/**
 * Japanese lexer using igo (http://igo.sourceforge.jp/).
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Toshio HIRAI
 */
final class JapaneseTokenizer extends Tokenizer {
  /** Flag available. */
  private static boolean available = true;

  /** Name of the Igo tagger class. */
  private static final String PATTERN = "net.reduls.igo.Tagger";
  /** Name of Japanese dictionary. */
  private static final String LANG = "ja";

  /** The kind of POS(Noun). */
  private static final String MEISHI = "\u540D\u8A5E";
  /** The kind of POS(Pre-noun Adjectival). */
  private static final String RENTAISHI = "\u9023\u4F53\u8A5E";
  /** The kind of POS(Adverb). */
  private static final String HUKUSHI = "\u526F\u8A5E";
  /** The kind of POS(Verb). */
  private static final String DOUSHI = "\u52D5\u8A5E";
  /** The kind of POS(Conjunction). */
  private static final String SETSUZOKUSHI = "\u63A5\u7D9A\u8A5E";
  /** The kind of POS(Prefix). */
  private static final String SETTOUSHI = "\u63A5\u982D\u8A5E";
  /** The kind of POS(Modal verbs). */
  private static final String JYODOUSHI = "\u52A9\u52D5\u8A5E";
  /** The kind of POS(Postpositional particle). */
  private static final String JYOSHI = "\u52A9\u8A5E";
  /** The kind of POS(Adjective). */
  private static final String KEIYOUSHI = "\u5F62\u5BB9\u8A5E";
  /** The kind of POS(Mark). */
  private static final String KIGOU = "\u8A18\u53F7";
  /** The kind of POS(Interjection). */
  private static final String KANDOUSHI = "\u611F\u52D5\u8A5E";
  /** The kind of POS(Filler). */
  private static final String FILLER = "\u30D5\u30A3\u30E9\u30FC";

  /** Constant of Feature(Mark). */
  private static final String KIGOU_FEATURE = "\u8A18\u53F7,*,*,*,*,*,*,*,*";
  /** Constant of Feature(Noun). */
  private static final String MEISHI_FEATURE = "\u540D\u8A5E,*,*,*,*,*,*,*,*";

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
  /** Token list. */
  private ArrayList<Morpheme> tokenList = new ArrayList<>();
  /** Current position of the Token list. */
  private int cpos;
  /** Current token. */
  private Morpheme currToken;

  /** Case option. */
  private final FTCase cs;
  /** Diacritics flag. */
  private final boolean dc;
  /** Wildcard flag. */
  private final boolean wc;
  /** Stemming flag. */
  private final boolean st;
  /** Token position. */
  private int pos = -1;
  /** Flag indicating a special character. */
  private boolean sc;

  static {
    IOFile dic = null;
    if(Reflect.available(PATTERN)) {
      dic = new IOFile(LANG);
      if(!dic.exists()) {
        dic = new IOFile(Prop.HOMEDIR, "etc/" + LANG);
        if(!dic.exists()) available = false;
      }
    } else {
      available = false;
    }

    if(available) {
      Class<?> clz = Reflect.find(PATTERN);
      if(clz == null) {
        Util.debug("Could not initialize Igo Japanese lexer.");
      } else {
        /* Igo constructor. */
        final Constructor<?> tgr = Reflect.find(clz, String.class);
        tagger = Reflect.get(tgr, dic.path());
        if(tagger == null) {
          available = false;
          Util.debug("Could not initialize Igo Japanese lexer.");
        } else {
          parse = Reflect.method(clz, "parse", CharSequence.class);
          if(parse == null) {
            Util.debug("Could not initialize Igo lexer method.");
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
    cs = fto != null && fto.cs != null ? fto.cs : FTCase.INSENSITIVE;
    wc = fto != null && fto.is(WC);
    dc = fto != null && fto.is(DC);
    st = fto != null && fto.is(ST);
  }

  @Override
  Tokenizer get(final FTOpt f) {
    return new JapaneseTokenizer(f);
  }

  @Override
  public JapaneseTokenizer init(final byte[] txt) {
    String source = string(txt);
    if(wc) { // convert wide-space to space
      source = source.replace('\u3000', '\u0020');
    }
    final ArrayList<?> morpheme = (ArrayList<?>) Reflect.invoke(parse, tagger, source);
    final ArrayList<Morpheme> list = new ArrayList<>();
    try {
      int prev = 0;
      final int ms = morpheme.size();
      for(int i = 0; i < ms; i++) {
        final Object m = morpheme.get(i);
        final String srfc = surface.get(m).toString();
        final String ftr = feature.get(m).toString();
        final int strt = start.getInt(m);
        if(i != 0) {
          final int l = strt - prev;
          if(l != 0) {
            list.add(new Morpheme(
                source.substring(strt - 1, strt + l - 1), KIGOU_FEATURE)
            );
          }
        }
        prev = srfc.length() + strt;

        // separates continuous mark (ASCII)
        boolean cont = true;
        final ArrayList<Morpheme> marks = new ArrayList<>();
        final int sl = srfc.length();
        for(int s = 0; s < sl; s++) {
          final String c = String.valueOf(srfc.charAt(s));
          final byte[] t = token(c);
          if(t.length == 1) {
            if(letter(t[0]) || digit(t[0])) cont = false;
            else marks.add(new Morpheme(c, KIGOU_FEATURE));
          } else {
            cont = false;
          }
        }

        if(cont) list.addAll(marks);
        else list.add(new Morpheme(srfc, ftr));
      }
    } catch(final Exception ex) {
      Util.errln(Util.className(this) + ": " + ex);
    }
    tokenList = list;
    tokens = list.iterator();

    return this;
  }

  /**
   * Returns whether the special character.
   * @param s string
   * @return result of check
   */
  private static boolean isFtChar(final String s) {
    return ".".equals(s) || "?".equals(s) || "*".equals(s) || "+".equals(s) ||
      "\\".equals(s) || "{".equals(s) || "}".equals(s);
  }

  /**
   * Returns whether the following token exists (using wildcards).
   * @return result of check
   */
  private boolean moreWC() {
    final StringBuilder word = new StringBuilder();
    final int size = tokenList.size();
    boolean period = false, bs = false, more = false;

    for(; cpos < size; cpos++) {
      String cSrfc = tokenList.get(cpos).getSurface();
      final boolean cMark = tokenList.get(cpos).isMark();
      String nSrfc = null;
      boolean nMark = false;
      if(cpos < size - 1) {
        nSrfc = tokenList.get(cpos + 1).getSurface();
        nMark = tokenList.get(cpos + 1).isMark();
      }

      if(nSrfc != null) {
        if("\\".equals(cSrfc)) bs = true;

        // delimiter
        if(cMark && !isFtChar(cSrfc) || "\\".equals(cSrfc) && nMark) {
          period = false;
          bs = false;
          if(word.length() != 0) {
            more = true;
            break;
          }
          if("\\".equals(cSrfc) && nMark) cpos++;
          continue;
        }

        word.append(cSrfc);

        if(bs || "\\".equals(nSrfc)) {
          more = true;
          continue;
        }

        if(".".equals(cSrfc) || ".".equals(nSrfc)) {
          period = true;
          continue;
        }
        if(period) {
          if("{".equals(cSrfc)) {
            cpos++;
            for(; cpos < size; cpos++) {
              cSrfc = tokenList.get(cpos).getSurface();
              word.append(cSrfc);
              if("}".equals(cSrfc)) {
                more = true;
                break;
              }
            }
            cpos++;
            break;
          }
          continue;
        }
      } else {
        // last token.
        if(cMark) {
          if("\\".equals(cSrfc)) continue;
          if(word.length() != 0) {
            word.append(cSrfc);
          }
          more = true;
          continue;
        }
      }

      if(period) {
        word.append(cSrfc);
      } else {
        if(bs)
          if(!isFtChar(cSrfc)) word.append(cSrfc);
        else
          word.setLength(0);
      }
      more = true;
      cpos++;
      break;
    }
    if(more) {
      currToken = word.length() == 0 ? tokenList.get(cpos - 1) :
        new Morpheme(word.toString(), MEISHI_FEATURE);
    }
    return more;
  }

  /**
   * Returns whether the following token exists.
   * @return result
   */
  private boolean more() {
    if(all) return tokens.hasNext();

    while(tokens.hasNext()) {
      currToken = tokens.next();
      if(!currToken.isMark() && !currToken.isAttachedWord()) return true;
    }
    return false;

  }
  @Override
  public boolean hasNext() {
    return wc ? moreWC() : more();
  }

  @Override
  public FTSpan next() {
    return new FTSpan(nextToken(), pos, sc);
  }

  /**
   * Returns the effective token.
   * @return token
   */
  private byte[] get() {
    pos++;
    String n = currToken.getSurface();
    final int hinshi = currToken.getHinshi();
    if(st &&
       (hinshi == Morpheme.HINSHI_DOUSHI ||
        hinshi == Morpheme.HINSHI_KEIYOUSHI)) {
        n = currToken.getBaseForm();
    }
    byte[] t = token(n);
    final boolean a = ascii(t);
    if(!a && !dc) t = noDiacritics(t);
    if(cs == FTCase.UPPER) t = WesternTokenizer.upper(t, a);
    else if(cs != FTCase.SENSITIVE) t = WesternTokenizer.lower(t, a);
    return toHankaku(t);
  }

  /**
   * Returns the token which contains special characters.
   * @return token
   */
  private byte[] getSC() {
    final Morpheme m = tokens.next();
    final String n = m.getSurface();
    if(m.isMark() || m.isAttachedWord()) {
      sc = true;
    } else {
      pos++;
      sc = false;
    }
    return token(n);
  }

  @Override
  public byte[] nextToken() {
    return original ? getSC() : get();
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
   * Converts to HANKAKU characters.
   * @param text Japanese text
   * @return result of conversion(->HANKAKU)
   */
  private static byte[] toHankaku(final byte[] text) {
    if(ascii(text)) return text;

    final int tl = text.length;
    final TokenBuilder tb = new TokenBuilder(tl);
    for(int t = 0; t < tl; t += cl(text, t)) {
      final int c = cp(text, t);
      if(c >= 0xFF10 && c <= 0xFF19 || c >= 0xFF21 && c <= 0xFF3A
          || c >= 0xFF41 && c <= 0xFF5A) {
        tb.add(c - 0xFEE0);
      } else if(c == 0x3000) { // IDEOGRAPHIC SPACE
        tb.add(0x0020);
      } else if(c == 0xFF01) { // !
        tb.add(0x0021);
      } else if(c == 0xFF02) { // " FULLWIDTH QUOTATION MARK
        tb.add(0x0022);
      } else if(c == 0x201C) { // " LEFT DOUBLE QUOTATION MARK
        tb.add(0x0022);
      } else if(c == 0x201D) { // " RIGHT DOUBLE QUOTATION MARK
        tb.add(0x0022);
      } else if(c == 0xFF03) { // #
        tb.add(0x0023);
      } else if(c == 0xFF04) { // $
        tb.add(0x0024);
      } else if(c == 0xFF05) { // %
        tb.add(0x0025);
      } else if(c == 0xFF06) { // &
        tb.add(0x0026);
      } else if(c == 0xFF07) { // ' FULLWIDTH APOSTROPHE
        tb.add(0x0027);
      } else if(c == 0x2018) { // ' LEFT SINGLE QUOTATION MARK
        tb.add(0x0027);
      } else if(c == 0x2019) { // ' RIGHT SINGLE QUOTATION MARK
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
  private static final class Morpheme {
    /** A part of speech in the context, NEISHI(Noun). */
    private static final int HINSHI_MEISHI = 1;
    /** A part of speech in the context, RENTAISHI(Pre-noun Adjectival). */
    private static final int HINSHI_RENTAISHI = 2;
    /** A part of speech in the context, HUKUSHI(Adverb). */
    private static final int HINSHI_HUKUSHI = 3;
    /** A part of speech in the context, DOUSHI(Verb). */
    private static final int HINSHI_DOUSHI = 4;
    /** A part of speech in the context, SETSUZOKUSHI(Conjunction). */
    private static final int HINSHI_SETSUZOKUSHI = 5;
    /** A part of speech in the context, JYODOUSHI(Modal verbs). */
    private static final int HINSHI_JYODOUSHI = 6;
    /** A part of speech in the context, JYOSHI(Postpositional particle). */
    private static final int HINSHI_JYOSHI = 7;
    /** A part of speech in the context, KEIYOUSHI(Adjective). */
    private static final int HINSHI_KEIYOUSHI = 8;
    /** A part of speech in the context, KIGOU(Mark). */
    private static final int HINSHI_KIGOU = 9;
    /** A part of speech in the context, KANDOUSHI(Interjection). */
    private static final int HINSHI_KANDOUSHI = 10;
    /** A part of speech in the context, FILLER(Filler). */
    private static final int HINSHI_FILLER = 11;
    /** A part of speech in the context, SETTOUSHI(Prefix). */
    private static final int HINSHI_SETTOUSHI = 12;
    /** A part of speech in the context, Others. */
    private static final int HINSHI_SONOTA = 0;

    /** Surface of morpheme. */
    private final String mSurface;
    /** Feature of morpheme. */
    private final String mFeature;

    /**
     * Constructor.
     * @param srfc surface
     * @param ftr feature
    */
    private Morpheme(final String srfc, final String ftr) {
      mSurface = srfc;
      mFeature = ftr;
    }

    /**
     * Returns surface.
     * @return Surface
     */
    public String getSurface() {
      return mSurface;
    }

    /**
     * Returns whether the avoid token.
     * @return result
     */
    public boolean isMark() {
      final int hinshi = getHinshi();
      return hinshi == HINSHI_KIGOU || hinshi == HINSHI_FILLER;
    }

    /**
     * Tests an attached word(FUZOKU-GO).
     * @return result
     */
    public boolean isAttachedWord() {
      final int hinshi = getHinshi();
      return hinshi == HINSHI_JYODOUSHI || hinshi == HINSHI_JYOSHI;
    }

    /**
     * Returns the part of speech.
     * @return part of speech
     */
    public int getHinshi() {
      final int hinshi;
      // morphological analyzer certainly returns
      // the single ascii char as a "noun".
      final byte[] s = token(mSurface);
      if(s.length == 1 && !letter(s[0]) && !digit(s[0])) {
        hinshi = HINSHI_KIGOU;
      } else {
        final String h = getPos();
        switch(h) {
          case MEISHI:       hinshi = HINSHI_MEISHI; break;
          case RENTAISHI:    hinshi = HINSHI_RENTAISHI; break;
          case HUKUSHI:      hinshi = HINSHI_HUKUSHI; break;
          case DOUSHI:       hinshi = HINSHI_DOUSHI; break;
          case SETSUZOKUSHI: hinshi = HINSHI_SETSUZOKUSHI; break;
          case SETTOUSHI:    hinshi = HINSHI_SETTOUSHI; break;
          case JYODOUSHI:    hinshi = HINSHI_JYODOUSHI; break;
          case JYOSHI:       hinshi = HINSHI_JYOSHI; break;
          case KEIYOUSHI:    hinshi = HINSHI_KEIYOUSHI; break;
          case KIGOU:        hinshi = HINSHI_KIGOU; break;
          case KANDOUSHI:    hinshi = HINSHI_KANDOUSHI; break;
          case FILLER:       hinshi = HINSHI_FILLER; break;
          default:           hinshi = HINSHI_SONOTA; break;
        }
      }
      return hinshi;
    }

    /**
     * Retrieves base form from feature.
     * @return base form
     */
    public String getBaseForm() {
      return Strings.split(mFeature, ',')[6];
    }

    /**
     * Retrieves parts of speech from feature.
     * @return parts of speech(coding in Japanese)
     */
    private String getPos() {
      return Strings.split(mFeature, ',')[0];
    }

    @Override
    public String toString() {
      return mSurface;
    }
  }
}
