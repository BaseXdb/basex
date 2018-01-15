package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Hash Module.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class HashModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void md5() {
    query("string(" + _HASH_MD5.args("") + ")", "1B2M2Y8AsgTpgAmY7PhCfg==");
    query("string(" + _HASH_MD5.args("BaseX") + ")", "DWUYXJ4pYxHAoiABeeR5og==");
    query("string(" + _HASH_MD5.args(" <x/>") + ")", "1B2M2Y8AsgTpgAmY7PhCfg==");
  }

  /** Test method. */
  @Test
  public void sha1() {
    query("string(" + _HASH_SHA1.args("") + ")", "2jmj7l5rSw0yVb/vlWAYkK/YBwk=");
    query("string(" + _HASH_SHA1.args("BaseX") + ")", "OtWVjw8n1a/9yilXVg8SHQWXpO0=");
    query("string(" + _HASH_SHA1.args(" <x/>") + ")", "2jmj7l5rSw0yVb/vlWAYkK/YBwk=");
  }

  /** Test method. */
  @Test
  public void sha256() {
    query("string(" + _HASH_SHA256.args("") + ")", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
    query("string(" + _HASH_SHA256.args("BaseX") + ")",
        "FdVwdj3rddcou2lkM5KHO4NczMlKLx6IGQnaR2YoIaM=");
  }

  /** Test method. */
  @Test
  public void hash() {
    query("string(" + _HASH_HASH.args("", "MD5") + ")", "1B2M2Y8AsgTpgAmY7PhCfg==");
    query("string(" + _HASH_HASH.args("", "md5") + ")", "1B2M2Y8AsgTpgAmY7PhCfg==");
    query("string(" + _HASH_HASH.args("", "SHA") + ")", "2jmj7l5rSw0yVb/vlWAYkK/YBwk=");
    query("string(" + _HASH_HASH.args("", "SHA1") + ")", "2jmj7l5rSw0yVb/vlWAYkK/YBwk=");
    query("string(" + _HASH_HASH.args("", "SHA-1") + ")", "2jmj7l5rSw0yVb/vlWAYkK/YBwk=");
    query("string(" + _HASH_HASH.args("", "SHA-256") + ")",
        "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
    query("string(" + _HASH_HASH.args(" xs:base64Binary('')", "md5") + ")",
        "1B2M2Y8AsgTpgAmY7PhCfg==");
    query("string(" + _HASH_HASH.args(" xs:hexBinary('')", "md5") + ")",
        "1B2M2Y8AsgTpgAmY7PhCfg==");

    final String file = sandbox() + "hash";
    try {
      query(_FILE_WRITE.args(file, "BaseX"));
      query("string(" + _HASH_HASH.args(_FILE_READ_BINARY.args(file), "md5") + ")",
          "DWUYXJ4pYxHAoiABeeR5og==");
    } finally {
      query(_FILE_DELETE.args(file));
    }

    error(_HASH_HASH.args("", ""), HASH_ALGORITHM_X);
  }
}
