package org.basex.test.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.util.*;
import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests the XQuery hashing functions prefixed with "hash".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNHashTest extends AdvancedQueryTest {
  /** Test method for the util:md5() function. */
  @Test
  public void utilMd5() {
    check(_HASH_MD5);
    query(_HASH_MD5.args(""), "D41D8CD98F00B204E9800998ECF8427E");
    query(_HASH_MD5.args("BaseX"), "0D65185C9E296311C0A2200179E479A2");
  }

  /** Test method for the util:sha1() function. */
  @Test
  public void utilSha1() {
    check(_HASH_SHA1);
    query(_HASH_SHA1.args(""), "DA39A3EE5E6B4B0D3255BFEF95601890AFD80709");
    query(_HASH_SHA1.args("BaseX"), "3AD5958F0F27D5AFFDCA2957560F121D0597A4ED");
  }

  /** Test method for the util:sha256() function. */
  @Test
  public void utilSha256() {
    check(_HASH_SHA256);
    query(_HASH_SHA256.args(""),
        "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855");
    query(_HASH_SHA256.args("BaseX"),
        "15D570763DEB75D728BB69643392873B835CCCC94A2F1E881909DA47662821A3");
  }

  /** Test method for the util:hash() function. */
  @Test
  public void utilHash() {
    check(_HASH_HASH);
    query(_HASH_HASH.args("", "MD5"), "D41D8CD98F00B204E9800998ECF8427E");
    query(_HASH_HASH.args("", "md5"), "D41D8CD98F00B204E9800998ECF8427E");
    query(_HASH_HASH.args("", "SHA"), "DA39A3EE5E6B4B0D3255BFEF95601890AFD80709");
    query(_HASH_HASH.args("", "SHA1"), "DA39A3EE5E6B4B0D3255BFEF95601890AFD80709");
    query(_HASH_HASH.args("", "SHA-1"), "DA39A3EE5E6B4B0D3255BFEF95601890AFD80709");
    query(_HASH_HASH.args("", "SHA-256"),
        "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855");
    error(_HASH_HASH.args("", ""), Err.HASH_ALG);
  }

  /** Test method for the util:hash-binary() function. */
  @Test
  public void utilHashBinary() {
    check(_HASH_HASH_BINARY);
    query(_HASH_HASH_BINARY.args("xs:hexBinary('')", "md5"),
        "D41D8CD98F00B204E9800998ECF8427E");
    query(_HASH_HASH_BINARY.args("xs:base64Binary('')", "MD5"),
        "D41D8CD98F00B204E9800998ECF8427E");
    error(_HASH_HASH_BINARY.args("xs:hexBinary('')", ""), Err.HASH_ALG);
  }
}
