package org.basex.query.func.crypto;

import static org.basex.util.Token.*;

import java.util.*;

/**
 * Minimal reader and writer for DER-encoded ASN.1 elements.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class Der {
  // Java 25 (once the baseline exceeds 21): obsolete if PEMDecoder/PEMEncoder cover PKCS #1, SEC1

  /** Encoded version 0 (INTEGER), the first element of PKCS #8 structures. */
  static final byte[] VERSION_0 = { 0x02, 0x01, 0x00 };
  /** Tag: BIT STRING. */
  static final int BIT_STRING = 0x03;
  /** Tag: OCTET STRING. */
  static final int OCTET_STRING = 0x04;
  /** Tag: SEQUENCE. */
  static final int SEQUENCE = 0x30;
  /** Tag: context-specific element [0] (SEC1: parameters). */
  static final int CONTEXT_0 = 0xA0;
  /** Tag: context-specific element [1] (SEC1: public key). */
  static final int CONTEXT_1 = 0xA1;

  /** Tag. */
  final int tag;
  /** Encoded data. */
  private final byte[] data;
  /** Start of the element. */
  private final int start;
  /** Start of the content. */
  private final int content;
  /** End of the element. */
  private final int end;

  /**
   * Constructor.
   * @param data encoded data
   * @param start start of the element
   * @param limit end of the enclosing element
   * @throws IllegalArgumentException invalid encoding
   */
  private Der(final byte[] data, final int start, final int limit) {
    this.data = data;
    this.start = start;
    if(start + 2 > limit) throw new IllegalArgumentException("Truncated DER element");
    tag = data[start] & 0xFF;
    int p = start + 1, l = data[p++] & 0xFF;
    if(l > 0x80 && l < 0x85) {
      final int n = l & 0x7F;
      if(n > limit - p) throw new IllegalArgumentException("Truncated DER length");
      l = 0;
      for(int i = 0; i < n; i++) l = l << 8 | data[p++] & 0xFF;
    } else if(l >= 0x80) {
      throw new IllegalArgumentException("Unsupported DER length");
    }
    // overflow-safe: compare the length with the remaining bytes
    if(l < 0 || l > limit - p) throw new IllegalArgumentException("Truncated DER element");
    content = p;
    end = p + l;
  }

  /**
   * Parses a DER element.
   * @param data encoded data
   * @return element
   * @throws IllegalArgumentException invalid encoding
   */
  static Der parse(final byte[] data) {
    return new Der(data, 0, data.length);
  }

  /**
   * Returns the child elements of a constructed element.
   * @return child elements
   * @throws IllegalArgumentException invalid encoding
   */
  List<Der> children() {
    final List<Der> list = new ArrayList<>();
    for(int p = content; p < end;) {
      final Der child = new Der(data, p, end);
      list.add(child);
      p = child.end;
    }
    return list;
  }

  /**
   * Returns the first child element with the specified tag.
   * @param tg tag
   * @return child element (can be {@code null})
   * @throws IllegalArgumentException invalid encoding
   */
  Der child(final int tg) {
    for(final Der child : children()) {
      if(child.tag == tg) return child;
    }
    return null;
  }

  /**
   * Returns the content bytes.
   * @return content
   */
  byte[] content() {
    return Arrays.copyOfRange(data, content, end);
  }

  /**
   * Returns the encoded element.
   * @return encoded bytes
   */
  byte[] encoded() {
    return Arrays.copyOfRange(data, start, end);
  }

  /**
   * Encodes an element.
   * @param tag tag
   * @param content content
   * @return encoded bytes
   */
  static byte[] encode(final int tag, final byte[] content) {
    int l = content.length;
    final int n = l < 0x80 ? 0 : (39 - Integer.numberOfLeadingZeros(l)) / 8;
    final byte[] head = new byte[n + 2];
    head[0] = (byte) tag;
    head[1] = (byte) (n == 0 ? l : 0x80 | n);
    for(int i = n + 1; i > 1; i--, l >>>= 8) head[i] = (byte) l;
    return concat(head, content);
  }

  /**
   * Encodes a BIT STRING without unused bits.
   * @param bytes bytes
   * @return encoded bytes
   */
  static byte[] bitString(final byte[] bytes) {
    return encode(BIT_STRING, concat(new byte[] { 0 }, bytes));
  }
}
