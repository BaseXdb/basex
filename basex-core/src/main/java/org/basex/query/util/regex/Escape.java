package org.basex.query.util.regex;

import java.util.*;
import java.util.Map.Entry;

/**
 * Escape sequence.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class Escape extends RegExp {
  /**  Comparator for int ranges. */
  private static final Comparator<int[]> CMP = new Comparator<int[]>() {
    @Override
    public int compare(final int[] o1, final int[] o2) {
      return o1[0] - o2[0];
    }
  };

  /** Character classes. */
  private static final Map<String, CharRange[]> MAP = new HashMap<>();

  /** Initial name characters. */
  private static final String INITIAL;
  /** Everything except initial name characters. */
  private static final String NOT_INITIAL;
  /** Name characters. */
  private static final String CHAR;
  /** Everything except name characters. */
  private static final String NOT_CHAR;
  /** Word characters. */
  private static final String WORD;
  /** Everything except word characters. */
  private static final String NOT_WORD;
  /** Digits. */
  private static final String DIGIT = "\\p{Nd}";
  /** Everything except digits. */
  private static final String NOT_DIGIT = "\\P{Nd}";

  /** Image. */
  private final String img;

  /**
   * Constructor.
   * @param str image string
   */
  private Escape(final String str) {
    img = str;
  }

  /**
   * Creates a regular expression from the given escape sequence.
   * @param esc escape sequence
   * @return regular expression
   */
  public static RegExp get(final String esc) {
    if(esc.startsWith("\\p{Is") || esc.startsWith("\\P{Is")) {
      final CharRange[] rng = MAP.get(esc);
      return rng != null ? new CharClass(new CharGroup(rng), null) : null;
    }

    final String e;
    switch(esc.charAt(1)) {
      case 'i': e = '[' + INITIAL + ']';     break;
      case 'I': e = '[' + NOT_INITIAL + ']'; break;
      case 'c': e = '[' + CHAR + ']';        break;
      case 'C': e = '[' + NOT_CHAR + ']';    break;
      case 'd': e =       DIGIT;             break;
      case 'D': e =       NOT_DIGIT;         break;
      case 'w': e = '[' + WORD + ']';        break;
      case 'W': e = '[' + NOT_WORD + ']';    break;
      default: e = esc;
    }
    return new Escape(e);
  }

  /**
   * Gets the character escaped by the given single escape.
   * @param single single-char escape sequence
   * @return the escaped char
   */
  public static char getCp(final String single) {
    switch(single.charAt(1)) {
      case 'n': return '\n';
      case 'r': return '\r';
      case 't': return '\t';
      default:  return single.charAt(1);
    }
  }

  /**
   * Translates the given escape into character ranges if possible.
   * @param esc escape sequence
   * @return array of regular expressions suitable for char groups
   */
  public static RegExp[] inGroup(final String esc) {
    if(esc.startsWith("\\p{Is") || esc.startsWith("\\P{Is")) {
      final CharRange[] rng = MAP.get(esc);
      if(rng != null) return rng;
    }
    if(esc.length() > 2) return new RegExp[] { new Escape(esc) };
    final String e;
    switch(esc.charAt(1)) {
      case 'i': e = INITIAL;     break;
      case 'I': e = NOT_INITIAL; break;
      case 'c': e = CHAR;        break;
      case 'C': e = NOT_CHAR;    break;
      case 'd': e = DIGIT;       break;
      case 'D': e = NOT_DIGIT;   break;
      case 'w': e = WORD;        break;
      case 'W': e = NOT_WORD;    break;
      default: e = esc;
    }
    return new RegExp[] { new Escape(e) };
  }

  @Override
  StringBuilder toRegEx(final StringBuilder sb) {
    return sb.append(img);
  }

  /**
   * Reads ranges from a string.
   * @param string input string
   * @return ranges
   */
  private static int[][] read(final String string) {
    final ArrayList<int[]> ranges = new ArrayList<>();
    for(int i = 0; i < string.length();) {
      final int[] rng = new int[2];
      i += Character.charCount(rng[0] = string.codePointAt(i));
      i += Character.charCount(rng[1] = string.codePointAt(i));
      ranges.add(rng);
    }
    return ranges.toArray(new int[ranges.size()][]);
  }

  /**
   * Merges codepoint ranges.
   * @param rss ranges
   * @return merged ranges
   */
  private static int[][] merge(final int[][]... rss) {
    final ArrayList<int[]> ranges = new ArrayList<>();
    for(final int[][] rs : rss) Collections.addAll(ranges, rs);
    Collections.sort(ranges, CMP);
    for(int i = 0; i < ranges.size(); i++) {
      final int[] rng = ranges.get(i);
      while(i + 1 < ranges.size()) {
        final int[] rng2 = ranges.get(i + 1);
        if(rng2[0] - rng[1] > 1) break;
        rng[1] = rng2[1];
        ranges.remove(i + 1);
      }
    }
    return ranges.toArray(new int[ranges.size()][]);
  }

  /**
   * Inverts code point ranges.
   * @param rng ranges
   * @return inverted ranges
   */
  private static int[][] invert(final int[][] rng) {
    int start = Character.MIN_CODE_POINT;
    final ArrayList<int[]> ranges = new ArrayList<>();

    for(final int[] in : rng) {
      if(in[0] - 1 >= start) ranges.add(new int[] { start, in[0] - 1 });
      start = in[1] + 1;
    }

    if(start <= Character.MAX_CODE_POINT)
      ranges.add(new int[] { start, Character.MAX_CODE_POINT });

    return ranges.toArray(new int[ranges.size()][]);
  }

  /**
   * Serializes the given ranges to a string.
   * @param ranges ranges to serialize
   * @return resulting string
   */
  private static String serialize(final int[][] ranges) {
    final StringBuilder sb = new StringBuilder();
    for(final int[] rng : ranges) {
      sb.append(escape(rng[0]));
      if(rng[1] != rng[0]) sb.append('-').append(escape(rng[1]));
    }
    return sb.toString();
  }

  /**
   * Escapes code points for use in character ranges.
   * @param cp code point
   * @return char representation
   */
  public static char[] escape(final int cp) {
    switch(cp) {
      case '[':
      case ']':
      case '-':
      case '\\':
      case '^':
        return new char[] { '\\', (char) cp };
      case '\n':
        return new char[] { '\\', 'n' };
      case '\r':
        return new char[] { '\\', 'r' };
      case '\t':
        return new char[] { '\\', 't' };
      default:
        return Character.toChars(cp);
    }
  }

  /**
   * Adds a new named character range to the map.
   * @param m map
   * @param n name
   * @param r range
   */
  private static void add(final Map<String, int[][]> m, final String n, final int[] r) {
    final int[][] old = m.get(n), nw = { r };
    m.put(n, old == null ? nw : merge(old, nw));
  }

  static {
    // taken from http://www.w3.org/TR/2000/WD-xml-2e-20000814#NT-Letter
    final int[][] baseChar = read("\u0041\u005A\u0061\u007A\u00C0\u00D6\u00D8\u00F6" +
        "\u00F8\u00FF\u0100\u0131\u0134\u013E\u0141\u0148\u014A\u017E\u0180\u01C3\u01CD" +
        "\u01F0\u01F4\u01F5\u01FA\u0217\u0250\u02A8\u02BB\u02C1\u0386\u0386\u0388\u038A" +
        "\u038C\u038C\u038E\u03A1\u03A3\u03CE\u03D0\u03D6\u03DA\u03DA\u03DC\u03DC\u03DE" +
        "\u03DE\u03E0\u03E0\u03E2\u03F3\u0401\u040C\u040E\u044F\u0451\u045C\u045E\u0481" +
        "\u0490\u04C4\u04C7\u04C8\u04CB\u04CC\u04D0\u04EB\u04EE\u04F5\u04F8\u04F9\u0531" +
        "\u0556\u0559\u0559\u0561\u0586\u05D0\u05EA\u05F0\u05F2\u0621\u063A\u0641\u064A" +
        "\u0671\u06B7\u06BA\u06BE\u06C0\u06CE\u06D0\u06D3\u06D5\u06D5\u06E5\u06E6\u0905" +
        "\u0939\u093D\u093D\u0958\u0961\u0985\u098C\u098F\u0990\u0993\u09A8\u09AA\u09B0" +
        "\u09B2\u09B2\u09B6\u09B9\u09DC\u09DD\u09DF\u09E1\u09F0\u09F1\u0A05\u0A0A\u0A0F" +
        "\u0A10\u0A13\u0A28\u0A2A\u0A30\u0A32\u0A33\u0A35\u0A36\u0A38\u0A39\u0A59\u0A5C" +
        "\u0A5E\u0A5E\u0A72\u0A74\u0A85\u0A8B\u0A8D\u0A8D\u0A8F\u0A91\u0A93\u0AA8\u0AAA" +
        "\u0AB0\u0AB2\u0AB3\u0AB5\u0AB9\u0ABD\u0ABD\u0AE0\u0AE0\u0B05\u0B0C\u0B0F\u0B10" +
        "\u0B13\u0B28\u0B2A\u0B30\u0B32\u0B33\u0B36\u0B39\u0B3D\u0B3D\u0B5C\u0B5D\u0B5F" +
        "\u0B61\u0B85\u0B8A\u0B8E\u0B90\u0B92\u0B95\u0B99\u0B9A\u0B9C\u0B9C\u0B9E\u0B9F" +
        "\u0BA3\u0BA4\u0BA8\u0BAA\u0BAE\u0BB5\u0BB7\u0BB9\u0C05\u0C0C\u0C0E\u0C10\u0C12" +
        "\u0C28\u0C2A\u0C33\u0C35\u0C39\u0C60\u0C61\u0C85\u0C8C\u0C8E\u0C90\u0C92\u0CA8" +
        "\u0CAA\u0CB3\u0CB5\u0CB9\u0CDE\u0CDE\u0CE0\u0CE1\u0D05\u0D0C\u0D0E\u0D10\u0D12" +
        "\u0D28\u0D2A\u0D39\u0D60\u0D61\u0E01\u0E2E\u0E30\u0E30\u0E32\u0E33\u0E40\u0E45" +
        "\u0E81\u0E82\u0E84\u0E84\u0E87\u0E88\u0E8A\u0E8A\u0E8D\u0E8D\u0E94\u0E97\u0E99" +
        "\u0E9F\u0EA1\u0EA3\u0EA5\u0EA5\u0EA7\u0EA7\u0EAA\u0EAB\u0EAD\u0EAE\u0EB0\u0EB0" +
        "\u0EB2\u0EB3\u0EBD\u0EBD\u0EC0\u0EC4\u0F40\u0F47\u0F49\u0F69\u10A0\u10C5\u10D0" +
        "\u10F6\u1100\u1100\u1102\u1103\u1105\u1107\u1109\u1109\u110B\u110C\u110E\u1112" +
        "\u113C\u113C\u113E\u113E\u1140\u1140\u114C\u114C\u114E\u114E\u1150\u1150\u1154" +
        "\u1155\u1159\u1159\u115F\u1161\u1163\u1163\u1165\u1165\u1167\u1167\u1169\u1169" +
        "\u116D\u116E\u1172\u1173\u1175\u1175\u119E\u119E\u11A8\u11A8\u11AB\u11AB\u11AE" +
        "\u11AF\u11B7\u11B8\u11BA\u11BA\u11BC\u11C2\u11EB\u11EB\u11F0\u11F0\u11F9\u11F9" +
        "\u1E00\u1E9B\u1EA0\u1EF9\u1F00\u1F15\u1F18\u1F1D\u1F20\u1F45\u1F48\u1F4D\u1F50" +
        "\u1F57\u1F59\u1F59\u1F5B\u1F5B\u1F5D\u1F5D\u1F5F\u1F7D\u1F80\u1FB4\u1FB6\u1FBC" +
        "\u1FBE\u1FBE\u1FC2\u1FC4\u1FC6\u1FCC\u1FD0\u1FD3\u1FD6\u1FDB\u1FE0\u1FEC\u1FF2" +
        "\u1FF4\u1FF6\u1FFC\u2126\u2126\u212A\u212B\u212E\u212E\u2180\u2182\u3041\u3094" +
        "\u30A1\u30FA\u3105\u312C\uAC00\uD7A3");

    final int[][] ideographic = read("\u4E00\u9FA5\u3007\u3007\u3021\u3029");

    final int[][] combiningChar = read("\u0300\u0345\u0360\u0361\u0483\u0486\u0591" +
        "\u05A1\u05A3\u05B9\u05BB\u05BD\u05BF\u05BF\u05C1\u05C2\u05C4\u05C4\u064B\u0652" +
        "\u0670\u0670\u06D6\u06DC\u06DD\u06DF\u06E0\u06E4\u06E7\u06E8\u06EA\u06ED\u0901" +
        "\u0903\u093C\u093C\u093E\u094C\u094D\u094D\u0951\u0954\u0962\u0963\u0981\u0983" +
        "\u09BC\u09BC\u09BE\u09BE\u09BF\u09BF\u09C0\u09C4\u09C7\u09C8\u09CB\u09CD\u09D7" +
        "\u09D7\u09E2\u09E3\u0A02\u0A02\u0A3C\u0A3C\u0A3E\u0A3E\u0A3F\u0A3F\u0A40\u0A42" +
        "\u0A47\u0A48\u0A4B\u0A4D\u0A70\u0A71\u0A81\u0A83\u0ABC\u0ABC\u0ABE\u0AC5\u0AC7" +
        "\u0AC9\u0ACB\u0ACD\u0B01\u0B03\u0B3C\u0B3C\u0B3E\u0B43\u0B47\u0B48\u0B4B\u0B4D" +
        "\u0B56\u0B57\u0B82\u0B83\u0BBE\u0BC2\u0BC6\u0BC8\u0BCA\u0BCD\u0BD7\u0BD7\u0C01" +
        "\u0C03\u0C3E\u0C44\u0C46\u0C48\u0C4A\u0C4D\u0C55\u0C56\u0C82\u0C83\u0CBE\u0CC4" +
        "\u0CC6\u0CC8\u0CCA\u0CCD\u0CD5\u0CD6\u0D02\u0D03\u0D3E\u0D43\u0D46\u0D48\u0D4A" +
        "\u0D4D\u0D57\u0D57\u0E31\u0E31\u0E34\u0E3A\u0E47\u0E4E\u0EB1\u0EB1\u0EB4\u0EB9" +
        "\u0EBB\u0EBC\u0EC8\u0ECD\u0F18\u0F19\u0F35\u0F35\u0F37\u0F37\u0F39\u0F39\u0F3E" +
        "\u0F3E\u0F3F\u0F3F\u0F71\u0F84\u0F86\u0F8B\u0F90\u0F95\u0F97\u0F97\u0F99\u0FAD" +
        "\u0FB1\u0FB7\u0FB9\u0FB9\u20D0\u20DC\u20E1\u20E1\u302A\u302F\u3099\u3099\u309A" +
      '\u309A');

    final int[][] digit = read("\u0030\u0039\u0660\u0669\u06F0\u06F9\u0966\u096F\u09E6" +
        "\u09EF\u0A66\u0A6F\u0AE6\u0AEF\u0B66\u0B6F\u0BE7\u0BEF\u0C66\u0C6F\u0CE6\u0CEF" +
        "\u0D66\u0D6F\u0E50\u0E59\u0ED0\u0ED9\u0F20\u0F29");

    final int[][] extender = read("\u00B7\u00B7\u02D0\u02D0\u02D1\u02D1\u0387\u0387" +
        "\u0640\u0640\u0E46\u0E46\u0EC6\u0EC6\u3005\u3005\u3031\u3035\u309D\u309E\u30FC" +
      '\u30FE');

    final int[][] word = read("\u0024\u0024\u002b\u002b\u0030\u0039\u003c\u003e\u0041" +
        "\u005a\u005e\u005e\u0060\u007a\u007c\u007c\u007e\u007e\u00a2\u00aa\u00ac\u00ac" +
        "\u00ae\u00b6\u00b8\u00ba\u00bc\u00be\u00c0\u0377\u037a\u037d\u0384\u0386\u0388" +
        "\u038a\u038c\u038c\u038e\u03a1\u03a3\u0527\u0531\u0556\u0559\u0559\u0561\u0587" +
        "\u0591\u05bd\u05bf\u05bf\u05c1\u05c2\u05c4\u05c5\u05c7\u05c7\u05d0\u05ea\u05f0" +
        "\u05f2\u0606\u0608\u060b\u060b\u060e\u061a\u0620\u0669\u066e\u06d3\u06d5\u06dc" +
        "\u06de\u06ff\u0710\u074a\u074d\u07b1\u07c0\u07f6\u07fa\u07fa\u0800\u082d\u0840" +
        "\u085b\u0900\u0963\u0966\u096f\u0971\u0977\u0979\u097f\u0981\u0983\u0985\u098c" +
        "\u098f\u0990\u0993\u09a8\u09aa\u09b0\u09b2\u09b2\u09b6\u09b9\u09bc\u09c4\u09c7" +
        "\u09c8\u09cb\u09ce\u09d7\u09d7\u09dc\u09dd\u09df\u09e3\u09e6\u09fb\u0a01\u0a03" +
        "\u0a05\u0a0a\u0a0f\u0a10\u0a13\u0a28\u0a2a\u0a30\u0a32\u0a33\u0a35\u0a36\u0a38" +
        "\u0a39\u0a3c\u0a3c\u0a3e\u0a42\u0a47\u0a48\u0a4b\u0a4d\u0a51\u0a51\u0a59\u0a5c" +
        "\u0a5e\u0a5e\u0a66\u0a75\u0a81\u0a83\u0a85\u0a8d\u0a8f\u0a91\u0a93\u0aa8\u0aaa" +
        "\u0ab0\u0ab2\u0ab3\u0ab5\u0ab9\u0abc\u0ac5\u0ac7\u0ac9\u0acb\u0acd\u0ad0\u0ad0" +
        "\u0ae0\u0ae3\u0ae6\u0aef\u0af1\u0af1\u0b01\u0b03\u0b05\u0b0c\u0b0f\u0b10\u0b13" +
        "\u0b28\u0b2a\u0b30\u0b32\u0b33\u0b35\u0b39\u0b3c\u0b44\u0b47\u0b48\u0b4b\u0b4d" +
        "\u0b56\u0b57\u0b5c\u0b5d\u0b5f\u0b63\u0b66\u0b77\u0b82\u0b83\u0b85\u0b8a\u0b8e" +
        "\u0b90\u0b92\u0b95\u0b99\u0b9a\u0b9c\u0b9c\u0b9e\u0b9f\u0ba3\u0ba4\u0ba8\u0baa" +
        "\u0bae\u0bb9\u0bbe\u0bc2\u0bc6\u0bc8\u0bca\u0bcd\u0bd0\u0bd0\u0bd7\u0bd7\u0be6" +
        "\u0bfa\u0c01\u0c03\u0c05\u0c0c\u0c0e\u0c10\u0c12\u0c28\u0c2a\u0c33\u0c35\u0c39" +
        "\u0c3d\u0c44\u0c46\u0c48\u0c4a\u0c4d\u0c55\u0c56\u0c58\u0c59\u0c60\u0c63\u0c66" +
        "\u0c6f\u0c78\u0c7f\u0c82\u0c83\u0c85\u0c8c\u0c8e\u0c90\u0c92\u0ca8\u0caa\u0cb3" +
        "\u0cb5\u0cb9\u0cbc\u0cc4\u0cc6\u0cc8\u0cca\u0ccd\u0cd5\u0cd6\u0cde\u0cde\u0ce0" +
        "\u0ce3\u0ce6\u0cef\u0cf1\u0cf2\u0d02\u0d03\u0d05\u0d0c\u0d0e\u0d10\u0d12\u0d3a" +
        "\u0d3d\u0d44\u0d46\u0d48\u0d4a\u0d4e\u0d57\u0d57\u0d60\u0d63\u0d66\u0d75\u0d79" +
        "\u0d7f\u0d82\u0d83\u0d85\u0d96\u0d9a\u0db1\u0db3\u0dbb\u0dbd\u0dbd\u0dc0\u0dc6" +
        "\u0dca\u0dca\u0dcf\u0dd4\u0dd6\u0dd6\u0dd8\u0ddf\u0df2\u0df3\u0e01\u0e3a\u0e3f" +
        "\u0e4e\u0e50\u0e59\u0e81\u0e82\u0e84\u0e84\u0e87\u0e88\u0e8a\u0e8a\u0e8d\u0e8d" +
        "\u0e94\u0e97\u0e99\u0e9f\u0ea1\u0ea3\u0ea5\u0ea5\u0ea7\u0ea7\u0eaa\u0eab\u0ead" +
        "\u0eb9\u0ebb\u0ebd\u0ec0\u0ec4\u0ec6\u0ec6\u0ec8\u0ecd\u0ed0\u0ed9\u0edc\u0edd" +
        "\u0f00\u0f03\u0f13\u0f39\u0f3e\u0f47\u0f49\u0f6c\u0f71\u0f84\u0f86\u0f97\u0f99" +
        "\u0fbc\u0fbe\u0fcc\u0fce\u0fcf\u0fd5\u0fd8\u1000\u1049\u1050\u10c5\u10d0\u10fa" +
        "\u10fc\u10fc\u1100\u1248\u124a\u124d\u1250\u1256\u1258\u1258\u125a\u125d\u1260" +
        "\u1288\u128a\u128d\u1290\u12b0\u12b2\u12b5\u12b8\u12be\u12c0\u12c0\u12c2\u12c5" +
        "\u12c8\u12d6\u12d8\u1310\u1312\u1315\u1318\u135a\u135d\u1360\u1369\u137c\u1380" +
        "\u1399\u13a0\u13f4\u1401\u166c\u166f\u167f\u1681\u169a\u16a0\u16ea\u16ee\u16f0" +
        "\u1700\u170c\u170e\u1714\u1720\u1734\u1740\u1753\u1760\u176c\u176e\u1770\u1772" +
        "\u1773\u1780\u17b3\u17b6\u17d3\u17d7\u17d7\u17db\u17dd\u17e0\u17e9\u17f0\u17f9" +
        "\u180b\u180d\u1810\u1819\u1820\u1877\u1880\u18aa\u18b0\u18f5\u1900\u191c\u1920" +
        "\u192b\u1930\u193b\u1940\u1940\u1946\u196d\u1970\u1974\u1980\u19ab\u19b0\u19c9" +
        "\u19d0\u19da\u19de\u1a1b\u1a20\u1a5e\u1a60\u1a7c\u1a7f\u1a89\u1a90\u1a99\u1aa7" +
        "\u1aa7\u1b00\u1b4b\u1b50\u1b59\u1b61\u1b7c\u1b80\u1baa\u1bae\u1bb9\u1bc0\u1bf3" +
        "\u1c00\u1c37\u1c40\u1c49\u1c4d\u1c7d\u1cd0\u1cd2\u1cd4\u1cf2\u1d00\u1de6\u1dfc" +
        "\u1f15\u1f18\u1f1d\u1f20\u1f45\u1f48\u1f4d\u1f50\u1f57\u1f59\u1f59\u1f5b\u1f5b" +
        "\u1f5d\u1f5d\u1f5f\u1f7d\u1f80\u1fb4\u1fb6\u1fc4\u1fc6\u1fd3\u1fd6\u1fdb\u1fdd" +
        "\u1fef\u1ff2\u1ff4\u1ff6\u1ffe\u2044\u2044\u2052\u2052\u2070\u2071\u2074\u207c" +
        "\u207f\u208c\u2090\u209c\u20a0\u20b9\u20d0\u20f0\u2100\u2189\u2190\u2328\u232b" +
        "\u23f3\u2400\u2426\u2440\u244a\u2460\u26ff\u2701\u2767\u2776\u27c4\u27c7\u27ca" +
        "\u27cc\u27cc\u27ce\u27e5\u27f0\u2982\u2999\u29d7\u29dc\u29fb\u29fe\u2b4c\u2b50" +
        "\u2b59\u2c00\u2c2e\u2c30\u2c5e\u2c60\u2cf1\u2cfd\u2cfd\u2d00\u2d25\u2d30\u2d65" +
        "\u2d6f\u2d6f\u2d7f\u2d96\u2da0\u2da6\u2da8\u2dae\u2db0\u2db6\u2db8\u2dbe\u2dc0" +
        "\u2dc6\u2dc8\u2dce\u2dd0\u2dd6\u2dd8\u2dde\u2de0\u2dff\u2e2f\u2e2f\u2e80\u2e99" +
        "\u2e9b\u2ef3\u2f00\u2fd5\u2ff0\u2ffb\u3004\u3007\u3012\u3013\u3020\u302f\u3031" +
        "\u303c\u303e\u303f\u3041\u3096\u3099\u309f\u30a1\u30fa\u30fc\u30ff\u3105\u312d" +
        "\u3131\u318e\u3190\u31ba\u31c0\u31e3\u31f0\u321e\u3220\u32fe\u3300\u4db5\u4dc0" +
        "\u9fcb\ua000\ua48c\ua490\ua4c6\ua4d0\ua4fd\ua500\ua60c\ua610\ua62b\ua640\ua672" +
        "\ua67c\ua67d\ua67f\ua697\ua6a0\ua6f1\ua700\ua78e\ua790\ua791\ua7a0\ua7a9\ua7fa" +
        "\ua82b\ua830\ua839\ua840\ua873\ua880\ua8c4\ua8d0\ua8d9\ua8e0\ua8f7\ua8fb\ua8fb" +
        "\ua900\ua92d\ua930\ua953\ua960\ua97c\ua980\ua9c0\ua9cf\ua9d9\uaa00\uaa36\uaa40" +
        "\uaa4d\uaa50\uaa59\uaa60\uaa7b\uaa80\uaac2\uaadb\uaadd\uab01\uab06\uab09\uab0e" +
        "\uab11\uab16\uab20\uab26\uab28\uab2e\uabc0\uabea\uabec\uabed\uabf0\uabf9\uac00" +
        "\ud7a3\ud7b0\ud7c6\ud7cb\ud7fb\uf900\ufa2d\ufa30\ufa6d\ufa70\ufad9\ufb00\ufb06" +
        "\ufb13\ufb17\ufb1d\ufb36\ufb38\ufb3c\ufb3e\ufb3e\ufb40\ufb41\ufb43\ufb44\ufb46" +
        "\ufbc1\ufbd3\ufd3d\ufd50\ufd8f\ufd92\ufdc7\ufdd0\ufdfd\ufe00\ufe0f\ufe20\ufe26" +
        "\ufe62\ufe62\ufe64\ufe66\ufe69\ufe69\ufe70\ufe74\ufe76\ufefc\uff04\uff04\uff0b" +
        "\uff0b\uff10\uff19\uff1c\uff1e\uff21\uff3a\uff3e\uff3e\uff40\uff5a\uff5c\uff5c" +
        "\uff5e\uff5e\uff66\uffbe\uffc2\uffc7\uffca\uffcf\uffd2\uffd7\uffda\uffdc\uffe0" +
        "\uffe6\uffe8\uffee\ufffc\ufffd\ud800\udc00\ud800\udc0b\ud800\udc0d\ud800\udc26" +
        "\ud800\udc28\ud800\udc3a\ud800\udc3c\ud800\udc3d\ud800\udc3f\ud800\udc4d\ud800" +
        "\udc50\ud800\udc5d\ud800\udc80\ud800\udcfa\ud800\udd02\ud800\udd02\ud800\udd07" +
        "\ud800\udd33\ud800\udd37\ud800\udd8a\ud800\udd90\ud800\udd9b\ud800\uddd0\ud800" +
        "\uddfd\ud800\ude80\ud800\ude9c\ud800\udea0\ud800\uded0\ud800\udf00\ud800\udf1e" +
        "\ud800\udf20\ud800\udf23\ud800\udf30\ud800\udf4a\ud800\udf80\ud800\udf9d\ud800" +
        "\udfa0\ud800\udfc3\ud800\udfc8\ud800\udfcf\ud800\udfd1\ud800\udfd5\ud801\udc00" +
        "\ud801\udc9d\ud801\udca0\ud801\udca9\ud802\udc00\ud802\udc05\ud802\udc08\ud802" +
        "\udc08\ud802\udc0a\ud802\udc35\ud802\udc37\ud802\udc38\ud802\udc3c\ud802\udc3c" +
        "\ud802\udc3f\ud802\udc55\ud802\udc58\ud802\udc5f\ud802\udd00\ud802\udd1b\ud802" +
        "\udd20\ud802\udd39\ud802\ude00\ud802\ude03\ud802\ude05\ud802\ude06\ud802\ude0c" +
        "\ud802\ude13\ud802\ude15\ud802\ude17\ud802\ude19\ud802\ude33\ud802\ude38\ud802" +
        "\ude3a\ud802\ude3f\ud802\ude47\ud802\ude60\ud802\ude7e\ud802\udf00\ud802\udf35" +
        "\ud802\udf40\ud802\udf55\ud802\udf58\ud802\udf72\ud802\udf78\ud802\udf7f\ud803" +
        "\udc00\ud803\udc48\ud803\ude60\ud803\ude7e\ud804\udc00\ud804\udc46\ud804\udc52" +
        "\ud804\udc6f\ud804\udc80\ud804\udcba\ud808\udc00\ud808\udf6e\ud809\udc00\ud809" +
        "\udc62\ud80c\udc00\ud80d\udc2e\ud81a\udc00\ud81a\ude38\ud82c\udc00\ud82c\udc01" +
        "\ud834\udc00\ud834\udcf5\ud834\udd00\ud834\udd26\ud834\udd29\ud834\udd72\ud834" +
        "\udd7b\ud834\udddd\ud834\ude00\ud834\ude45\ud834\udf00\ud834\udf56\ud834\udf60" +
        "\ud834\udf71\ud835\udc00\ud835\udc54\ud835\udc56\ud835\udc9c\ud835\udc9e\ud835" +
        "\udc9f\ud835\udca2\ud835\udca2\ud835\udca5\ud835\udca6\ud835\udca9\ud835\udcac" +
        "\ud835\udcae\ud835\udcb9\ud835\udcbb\ud835\udcbb\ud835\udcbd\ud835\udcc3\ud835" +
        "\udcc5\ud835\udd05\ud835\udd07\ud835\udd0a\ud835\udd0d\ud835\udd14\ud835\udd16" +
        "\ud835\udd1c\ud835\udd1e\ud835\udd39\ud835\udd3b\ud835\udd3e\ud835\udd40\ud835" +
        "\udd44\ud835\udd46\ud835\udd46\ud835\udd4a\ud835\udd50\ud835\udd52\ud835\udea5" +
        "\ud835\udea8\ud835\udfcb\ud835\udfce\ud835\udfff\ud83c\udc00\ud83c\udc2b\ud83c" +
        "\udc30\ud83c\udc93\ud83c\udca0\ud83c\udcae\ud83c\udcb1\ud83c\udcbe\ud83c\udcc1" +
        "\ud83c\udccf\ud83c\udcd1\ud83c\udcdf\ud83c\udd00\ud83c\udd0a\ud83c\udd10\ud83c" +
        "\udd2e\ud83c\udd30\ud83c\udd69\ud83c\udd70\ud83c\udd9a\ud83c\udde6\ud83c\ude02" +
        "\ud83c\ude10\ud83c\ude3a\ud83c\ude40\ud83c\ude48\ud83c\ude50\ud83c\ude51\ud83c" +
        "\udf00\ud83c\udf20\ud83c\udf30\ud83c\udf35\ud83c\udf37\ud83c\udf7c\ud83c\udf80" +
        "\ud83c\udf93\ud83c\udfa0\ud83c\udfc4\ud83c\udfc6\ud83c\udfca\ud83c\udfe0\ud83c" +
        "\udff0\ud83d\udc00\ud83d\udc3e\ud83d\udc40\ud83d\udc40\ud83d\udc42\ud83d\udcf7" +
        "\ud83d\udcf9\ud83d\udcfc\ud83d\udd00\ud83d\udd3d\ud83d\udd50\ud83d\udd67\ud83d" +
        "\uddfb\ud83d\uddff\ud83d\ude01\ud83d\ude10\ud83d\ude12\ud83d\ude14\ud83d\ude16" +
        "\ud83d\ude16\ud83d\ude18\ud83d\ude18\ud83d\ude1a\ud83d\ude1a\ud83d\ude1c\ud83d" +
        "\ude1e\ud83d\ude20\ud83d\ude25\ud83d\ude28\ud83d\ude2b\ud83d\ude2d\ud83d\ude2d" +
        "\ud83d\ude30\ud83d\ude33\ud83d\ude35\ud83d\ude40\ud83d\ude45\ud83d\ude4f\ud83d" +
        "\ude80\ud83d\udec5\ud83d\udf00\ud83d\udf73\ud83f\udffe\ud869\uded6\ud869\udf00" +
        "\ud86d\udf34\ud86d\udf40\ud86e\udc1d\ud87e\udc00\ud87e\ude1d\ud87f\udffe\ud87f" +
        "\udfff\ud8bf\udffe\ud8bf\udfff\ud8ff\udffe\ud8ff\udfff\ud93f\udffe\ud93f\udfff" +
        "\ud97f\udffe\ud97f\udfff\ud9bf\udffe\ud9bf\udfff\ud9ff\udffe\ud9ff\udfff\uda3f" +
        "\udffe\uda3f\udfff\uda7f\udffe\uda7f\udfff\udabf\udffe\udabf\udfff\udaff\udffe" +
        "\udaff\udfff\udb3f\udffe\udb3f\udfff\udb40\udd00\udb40\uddef\udb7f\udffe\udb7f" +
        "\udfff\udbbf\udffe\udbbf\udfff\udbff\udffe\udbff\udfff");

    final int[][] letter = merge(baseChar, ideographic),
        initialNameChar = merge(letter, read("__::")),
        nameChar = merge(initialNameChar, digit, read("-."), combiningChar, extender);

    INITIAL     = serialize(initialNameChar);
    NOT_INITIAL = serialize(invert(initialNameChar));
    CHAR        = serialize(nameChar);
    NOT_CHAR    = serialize(invert(nameChar));
    WORD        = serialize(word);
    NOT_WORD    = serialize(invert(word));

    // easy to reproduce from http://www.w3.org/TR/xsd-unicode-blocknames/#blocks
    final HashMap<String, int[][]> m = new HashMap<>();
    add(m, "BasicLatin", new int[] { 0x0000, 0x007F });
    add(m, "Latin-1Supplement", new int[] { 0x0080, 0x00FF });
    add(m, "LatinExtended-A", new int[] { 0x0100, 0x017F });
    add(m, "LatinExtended-B", new int[] { 0x0180, 0x024F });
    add(m, "IPAExtensions", new int[] { 0x0250, 0x02AF });
    add(m, "SpacingModifierLetters", new int[] { 0x02B0, 0x02FF });
    add(m, "CombiningDiacriticalMarks", new int[] { 0x0300, 0x036F });
    add(m, "Greek", new int[] { 0x0370, 0x03FF });
    add(m, "GreekandCoptic", new int[] { 0x0370, 0x03FF });
    add(m, "Cyrillic", new int[] { 0x0400, 0x04FF });
    add(m, "CyrillicSupplementary", new int[] { 0x0500, 0x052F });
    add(m, "CyrillicSupplement", new int[] { 0x0500, 0x052F });
    add(m, "Armenian", new int[] { 0x0530, 0x058F });
    add(m, "Hebrew", new int[] { 0x0590, 0x05FF });
    add(m, "Arabic", new int[] { 0x0600, 0x06FF });
    add(m, "Syriac", new int[] { 0x0700, 0x074F });
    add(m, "ArabicSupplement", new int[] { 0x0750, 0x077F });
    add(m, "Thaana", new int[] { 0x0780, 0x07BF });
    add(m, "NKo", new int[] { 0x07C0, 0x07FF });
    add(m, "Samaritan", new int[] { 0x0800, 0x083F });
    add(m, "Mandaic", new int[] { 0x0840, 0x085F });
    add(m, "Devanagari", new int[] { 0x0900, 0x097F });
    add(m, "Bengali", new int[] { 0x0980, 0x09FF });
    add(m, "Gurmukhi", new int[] { 0x0A00, 0x0A7F });
    add(m, "Gujarati", new int[] { 0x0A80, 0x0AFF });
    add(m, "Oriya", new int[] { 0x0B00, 0x0B7F });
    add(m, "Tamil", new int[] { 0x0B80, 0x0BFF });
    add(m, "Telugu", new int[] { 0x0C00, 0x0C7F });
    add(m, "Kannada", new int[] { 0x0C80, 0x0CFF });
    add(m, "Malayalam", new int[] { 0x0D00, 0x0D7F });
    add(m, "Sinhala", new int[] { 0x0D80, 0x0DFF });
    add(m, "Thai", new int[] { 0x0E00, 0x0E7F });
    add(m, "Lao", new int[] { 0x0E80, 0x0EFF });
    add(m, "Tibetan", new int[] { 0x0F00, 0x0FBF });
    add(m, "Tibetan", new int[] { 0x0F00, 0x0FFF });
    add(m, "Myanmar", new int[] { 0x1000, 0x109F });
    add(m, "Georgian", new int[] { 0x10A0, 0x10FF });
    add(m, "HangulJamo", new int[] { 0x1100, 0x11FF });
    add(m, "Ethiopic", new int[] { 0x1200, 0x137F });
    add(m, "EthiopicSupplement", new int[] { 0x1380, 0x139F });
    add(m, "Cherokee", new int[] { 0x13A0, 0x13FF });
    add(m, "UnifiedCanadianAboriginalSyllabics", new int[] { 0x1400, 0x167F });
    add(m, "Ogham", new int[] { 0x1680, 0x169F });
    add(m, "Runic", new int[] { 0x16A0, 0x16FF });
    add(m, "Tagalog", new int[] { 0x1700, 0x171F });
    add(m, "Hanunoo", new int[] { 0x1720, 0x173F });
    add(m, "Buhid", new int[] { 0x1740, 0x175F });
    add(m, "Tagbanwa", new int[] { 0x1760, 0x177F });
    add(m, "Khmer", new int[] { 0x1780, 0x17FF });
    add(m, "Mongolian", new int[] { 0x1800, 0x18AF });
    add(m, "UnifiedCanadianAboriginalSyllabicsExtended", new int[] { 0x18B0, 0x18FF });
    add(m, "Limbu", new int[] { 0x1900, 0x194F });
    add(m, "TaiLe", new int[] { 0x1950, 0x197F });
    add(m, "NewTaiLue", new int[] { 0x1980, 0x19DF });
    add(m, "KhmerSymbols", new int[] { 0x19E0, 0x19FF });
    add(m, "Buginese", new int[] { 0x1A00, 0x1A1F });
    add(m, "TaiTham", new int[] { 0x1A20, 0x1AAF });
    add(m, "Balinese", new int[] { 0x1B00, 0x1B7F });
    add(m, "Sundanese", new int[] { 0x1B80, 0x1BBF });
    add(m, "Batak", new int[] { 0x1BC0, 0x1BFF });
    add(m, "Lepcha", new int[] { 0x1C00, 0x1C4F });
    add(m, "OlChiki", new int[] { 0x1C50, 0x1C7F });
    add(m, "VedicExtensions", new int[] { 0x1CD0, 0x1CFF });
    add(m, "PhoneticExtensions", new int[] { 0x1D00, 0x1D7F });
    add(m, "PhoneticExtensionsSupplement", new int[] { 0x1D80, 0x1DBF });
    add(m, "CombiningDiacriticalMarksSupplement", new int[] { 0x1DC0, 0x1DFF });
    add(m, "LatinExtendedAdditional", new int[] { 0x1E00, 0x1EFF });
    add(m, "GreekExtended", new int[] { 0x1F00, 0x1FFF });
    add(m, "GeneralPunctuation", new int[] { 0x2000, 0x206F });
    add(m, "SuperscriptsandSubscripts", new int[] { 0x2070, 0x209F });
    add(m, "CurrencySymbols", new int[] { 0x20A0, 0x20CF });
    add(m, "CombiningMarksforSymbols", new int[] { 0x20D0, 0x20FF });
    add(m, "CombiningDiacriticalMarksforSymbols", new int[] { 0x20D0, 0x20FF });
    add(m, "LetterlikeSymbols", new int[] { 0x2100, 0x214F });
    add(m, "NumberForms", new int[] { 0x2150, 0x218F });
    add(m, "Arrows", new int[] { 0x2190, 0x21FF });
    add(m, "MathematicalOperators", new int[] { 0x2200, 0x22FF });
    add(m, "MiscellaneousTechnical", new int[] { 0x2300, 0x23FF });
    add(m, "ControlPictures", new int[] { 0x2400, 0x243F });
    add(m, "OpticalCharacterRecognition", new int[] { 0x2440, 0x245F });
    add(m, "EnclosedAlphanumerics", new int[] { 0x2460, 0x24FF });
    add(m, "BoxDrawing", new int[] { 0x2500, 0x257F });
    add(m, "BlockElements", new int[] { 0x2580, 0x259F });
    add(m, "GeometricShapes", new int[] { 0x25A0, 0x25FF });
    add(m, "MiscellaneousSymbols", new int[] { 0x2600, 0x26FF });
    add(m, "Dingbats", new int[] { 0x2700, 0x27BF });
    add(m, "MiscellaneousMathematicalSymbols-A", new int[] { 0x27C0, 0x27EF });
    add(m, "SupplementalArrows-A", new int[] { 0x27F0, 0x27FF });
    add(m, "BraillePatterns", new int[] { 0x2800, 0x28FF });
    add(m, "SupplementalArrows-B", new int[] { 0x2900, 0x297F });
    add(m, "MiscellaneousMathematicalSymbols-B", new int[] { 0x2980, 0x29FF });
    add(m, "SupplementalMathematicalOperators", new int[] { 0x2A00, 0x2AFF });
    add(m, "MiscellaneousSymbolsandArrows", new int[] { 0x2B00, 0x2BFF });
    add(m, "Glagolitic", new int[] { 0x2C00, 0x2C5F });
    add(m, "LatinExtended-C", new int[] { 0x2C60, 0x2C7F });
    add(m, "Coptic", new int[] { 0x2C80, 0x2CFF });
    add(m, "GeorgianSupplement", new int[] { 0x2D00, 0x2D2F });
    add(m, "Tifinagh", new int[] { 0x2D30, 0x2D7F });
    add(m, "EthiopicExtended", new int[] { 0x2D80, 0x2DDF });
    add(m, "CyrillicExtended-A", new int[] { 0x2DE0, 0x2DFF });
    add(m, "SupplementalPunctuation", new int[] { 0x2E00, 0x2E7F });
    add(m, "CJKRadicalsSupplement", new int[] { 0x2E80, 0x2EFF });
    add(m, "KangxiRadicals", new int[] { 0x2F00, 0x2FDF });
    add(m, "IdeographicDescriptionCharacters", new int[] { 0x2FF0, 0x2FFF });
    add(m, "CJKSymbolsandPunctuation", new int[] { 0x3000, 0x303F });
    add(m, "Hiragana", new int[] { 0x3040, 0x309F });
    add(m, "Katakana", new int[] { 0x30A0, 0x30FF });
    add(m, "Bopomofo", new int[] { 0x3100, 0x312F });
    add(m, "HangulCompatibilityJamo", new int[] { 0x3130, 0x318F });
    add(m, "Kanbun", new int[] { 0x3190, 0x319F });
    add(m, "BopomofoExtended", new int[] { 0x31A0, 0x31BF });
    add(m, "CJKStrokes", new int[] { 0x31C0, 0x31EF });
    add(m, "KatakanaPhoneticExtensions", new int[] { 0x31F0, 0x31FF });
    add(m, "EnclosedCJKLettersandMonths", new int[] { 0x3200, 0x32FF });
    add(m, "CJKCompatibility", new int[] { 0x3300, 0x33FF });
    add(m, "CJKUnifiedIdeographsExtensionA", new int[] { 0x3400, 0x4DB5 });
    add(m, "CJKUnifiedIdeographsExtensionA", new int[] { 0x3400, 0x4DBF });
    add(m, "YijingHexagramSymbols", new int[] { 0x4DC0, 0x4DFF });
    add(m, "CJKUnifiedIdeographs", new int[] { 0x4E00, 0x9FFF });
    add(m, "YiSyllables", new int[] { 0xA000, 0xA48F });
    add(m, "YiRadicals", new int[] { 0xA490, 0xA4CF });
    add(m, "Lisu", new int[] { 0xA4D0, 0xA4FF });
    add(m, "Vai", new int[] { 0xA500, 0xA63F });
    add(m, "CyrillicExtended-B", new int[] { 0xA640, 0xA69F });
    add(m, "Bamum", new int[] { 0xA6A0, 0xA6FF });
    add(m, "ModifierToneLetters", new int[] { 0xA700, 0xA71F });
    add(m, "LatinExtended-D", new int[] { 0xA720, 0xA7FF });
    add(m, "SylotiNagri", new int[] { 0xA800, 0xA82F });
    add(m, "CommonIndicNumberForms", new int[] { 0xA830, 0xA83F });
    add(m, "Phags-pa", new int[] { 0xA840, 0xA87F });
    add(m, "Saurashtra", new int[] { 0xA880, 0xA8DF });
    add(m, "DevanagariExtended", new int[] { 0xA8E0, 0xA8FF });
    add(m, "KayahLi", new int[] { 0xA900, 0xA92F });
    add(m, "Rejang", new int[] { 0xA930, 0xA95F });
    add(m, "HangulJamoExtended-A", new int[] { 0xA960, 0xA97F });
    add(m, "Javanese", new int[] { 0xA980, 0xA9DF });
    add(m, "Cham", new int[] { 0xAA00, 0xAA5F });
    add(m, "MyanmarExtended-A", new int[] { 0xAA60, 0xAA7F });
    add(m, "TaiViet", new int[] { 0xAA80, 0xAADF });
    add(m, "EthiopicExtended-A", new int[] { 0xAB00, 0xAB2F });
    add(m, "MeeteiMayek", new int[] { 0xABC0, 0xABFF });
    add(m, "HangulSyllables", new int[] { 0xAC00, 0xD7A3 });
    add(m, "HangulSyllables", new int[] { 0xAC00, 0xD7AF });
    add(m, "HangulJamoExtended-B", new int[] { 0xD7B0, 0xD7FF });
    add(m, "HighSurrogates", new int[] { 0xD800, 0xDB7F });
    add(m, "HighPrivateUseSurrogates", new int[] { 0xDB80, 0xDBFF });
    add(m, "LowSurrogates", new int[] { 0xDC00, 0xDFFF });
    add(m, "PrivateUse", new int[] { 0xE000, 0xF8FF });
    add(m, "PrivateUseArea", new int[] { 0xE000, 0xF8FF });
    add(m, "CJKCompatibilityIdeographs", new int[] { 0xF900, 0xFAFF });
    add(m, "AlphabeticPresentationForms", new int[] { 0xFB00, 0xFB4F });
    add(m, "ArabicPresentationForms-A", new int[] { 0xFB50, 0xFDFF });
    add(m, "VariationSelectors", new int[] { 0xFE00, 0xFE0F });
    add(m, "VerticalForms", new int[] { 0xFE10, 0xFE1F });
    add(m, "CombiningHalfMarks", new int[] { 0xFE20, 0xFE2F });
    add(m, "CJKCompatibilityForms", new int[] { 0xFE30, 0xFE4F });
    add(m, "SmallFormVariants", new int[] { 0xFE50, 0xFE6F });
    add(m, "ArabicPresentationForms-B", new int[] { 0xFE70, 0xFEFE });
    add(m, "ArabicPresentationForms-B", new int[] { 0xFE70, 0xFEFF });
    add(m, "Specials", new int[] { 0xFEFF, 0xFEFF });
    add(m, "HalfwidthandFullwidthForms", new int[] { 0xFF00, 0xFFEF });
    add(m, "Specials", new int[] { 0xFFF0, 0xFFFD });
    add(m, "Specials", new int[] { 0xFFF0, 0xFFFF });
    add(m, "LinearBSyllabary", new int[] { 0x10000, 0x1007F });
    add(m, "LinearBIdeograms", new int[] { 0x10080, 0x100FF });
    add(m, "AegeanNumbers", new int[] { 0x10100, 0x1013F });
    add(m, "AncientGreekNumbers", new int[] { 0x10140, 0x1018F });
    add(m, "AncientSymbols", new int[] { 0x10190, 0x101CF });
    add(m, "PhaistosDisc", new int[] { 0x101D0, 0x101FF });
    add(m, "Lycian", new int[] { 0x10280, 0x1029F });
    add(m, "Carian", new int[] { 0x102A0, 0x102DF });
    add(m, "OldItalic", new int[] { 0x10300, 0x1032F });
    add(m, "Gothic", new int[] { 0x10330, 0x1034F });
    add(m, "Ugaritic", new int[] { 0x10380, 0x1039F });
    add(m, "OldPersian", new int[] { 0x103A0, 0x103DF });
    add(m, "Deseret", new int[] { 0x10400, 0x1044F });
    add(m, "Shavian", new int[] { 0x10450, 0x1047F });
    add(m, "Osmanya", new int[] { 0x10480, 0x104AF });
    add(m, "CypriotSyllabary", new int[] { 0x10800, 0x1083F });
    add(m, "ImperialAramaic", new int[] { 0x10840, 0x1085F });
    add(m, "Phoenician", new int[] { 0x10900, 0x1091F });
    add(m, "Lydian", new int[] { 0x10920, 0x1093F });
    add(m, "Kharoshthi", new int[] { 0x10A00, 0x10A5F });
    add(m, "OldSouthArabian", new int[] { 0x10A60, 0x10A7F });
    add(m, "Avestan", new int[] { 0x10B00, 0x10B3F });
    add(m, "InscriptionalParthian", new int[] { 0x10B40, 0x10B5F });
    add(m, "InscriptionalPahlavi", new int[] { 0x10B60, 0x10B7F });
    add(m, "OldTurkic", new int[] { 0x10C00, 0x10C4F });
    add(m, "RumiNumeralSymbols", new int[] { 0x10E60, 0x10E7F });
    add(m, "Brahmi", new int[] { 0x11000, 0x1107F });
    add(m, "Kaithi", new int[] { 0x11080, 0x110CF });
    add(m, "Cuneiform", new int[] { 0x12000, 0x123FF });
    add(m, "CuneiformNumbersandPunctuation", new int[] { 0x12400, 0x1247F });
    add(m, "EgyptianHieroglyphs", new int[] { 0x13000, 0x1342F });
    add(m, "BamumSupplement", new int[] { 0x16800, 0x16A3F });
    add(m, "KanaSupplement", new int[] { 0x1B000, 0x1B0FF });
    add(m, "ByzantineMusicalSymbols", new int[] { 0x1D000, 0x1D0FF });
    add(m, "MusicalSymbols", new int[] { 0x1D100, 0x1D1FF });
    add(m, "AncientGreekMusicalNotation", new int[] { 0x1D200, 0x1D24F });
    add(m, "TaiXuanJingSymbols", new int[] { 0x1D300, 0x1D35F });
    add(m, "CountingRodNumerals", new int[] { 0x1D360, 0x1D37F });
    add(m, "MathematicalAlphanumericSymbols", new int[] { 0x1D400, 0x1D7FF });
    add(m, "MahjongTiles", new int[] { 0x1F000, 0x1F02F });
    add(m, "DominoTiles", new int[] { 0x1F030, 0x1F09F });
    add(m, "PlayingCards", new int[] { 0x1F0A0, 0x1F0FF });
    add(m, "EnclosedAlphanumericSupplement", new int[] { 0x1F100, 0x1F1FF });
    add(m, "EnclosedIdeographicSupplement", new int[] { 0x1F200, 0x1F2FF });
    add(m, "MiscellaneousSymbolsAndPictographs", new int[] { 0x1F300, 0x1F5FF });
    add(m, "Emoticons", new int[] { 0x1F600, 0x1F64F });
    add(m, "TransportAndMapSymbols", new int[] { 0x1F680, 0x1F6FF });
    add(m, "AlchemicalSymbols", new int[] { 0x1F700, 0x1F77F });
    add(m, "CJKUnifiedIdeographsExtensionB", new int[] { 0x20000, 0x2A6D6 });
    add(m, "CJKUnifiedIdeographsExtensionB", new int[] { 0x20000, 0x2A6DF });
    add(m, "CJKUnifiedIdeographsExtensionC", new int[] { 0x2A700, 0x2B73F });
    add(m, "CJKUnifiedIdeographsExtensionD", new int[] { 0x2B740, 0x2B81F });
    add(m, "CJKCompatibilityIdeographsSupplement", new int[] { 0x2F800, 0x2FA1F });
    add(m, "Tags", new int[] { 0xE0000, 0xE007F });
    add(m, "VariationSelectorsSupplement", new int[] { 0xE0100, 0xE01EF });
    add(m, "PrivateUse", new int[] { 0xF0000, 0xFFFFD });
    add(m, "SupplementaryPrivateUseArea-A", new int[] { 0xF0000, 0xFFFFF });
    add(m, "PrivateUse", new int[] { 0x100000, 0x10FFFD });
    add(m, "SupplementaryPrivateUseArea-B", new int[] { 0x100000, 0x10FFFF });

    // add entries for all known character classes
    for(final Entry<String, int[][]> e : m.entrySet()) {
      final int[][] v = e.getValue();
      final CharRange[] rs = new CharRange[v.length];
      for(int i = 0; i < v.length; i++) rs[i] = new CharRange(v[i][0], v[i][1]);
      MAP.put("\\p{Is" + e.getKey() + '}', rs);

      final int[][] not = invert(v);
      final CharRange[] nrs = new CharRange[not.length];
      for(int i = 0; i < not.length; i++) nrs[i] = new CharRange(not[i][0], not[i][1]);
      MAP.put("\\P{Is" + e.getKey() + '}', nrs);
    }
  }
}
