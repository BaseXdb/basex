package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Hash Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class HashModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void hash() {
    final Function func = _HASH_HASH;
    // queries
    query("string(" + func.args("", "MD5") + ")", "1B2M2Y8AsgTpgAmY7PhCfg==");
    query("string(" + func.args("", "md5") + ")", "1B2M2Y8AsgTpgAmY7PhCfg==");
    query("string(" + func.args("", "SHA") + ")", "2jmj7l5rSw0yVb/vlWAYkK/YBwk=");
    query("string(" + func.args("", "SHA1") + ")", "2jmj7l5rSw0yVb/vlWAYkK/YBwk=");
    query("string(" + func.args("", "SHA-1") + ")", "2jmj7l5rSw0yVb/vlWAYkK/YBwk=");
    query("string(" + func.args("", "SHA-256") + ")",
        "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
    query("string(" + func.args(" xs:base64Binary('')", "md5") + ")", "1B2M2Y8AsgTpgAmY7PhCfg==");
    query("string(" + func.args(" xs:hexBinary('')", "md5") + ")", "1B2M2Y8AsgTpgAmY7PhCfg==");

    final String file = sandbox() + "hash";
    try {
      query(_FILE_WRITE.args(file, "BaseX"));
      query("string(" + func.args(_FILE_READ_BINARY.args(file), "md5") + ")",
          "DWUYXJ4pYxHAoiABeeR5og==");
    } finally {
      query(_FILE_DELETE.args(file));
    }

    error(func.args("", ""), HASH_ALGORITHM_X);
  }

  /** Test method. */
  @Test public void md5() {
    final Function func = _HASH_MD5;
    // queries
    query("string(" + func.args("") + ")", "1B2M2Y8AsgTpgAmY7PhCfg==");
    query("string(" + func.args("BaseX") + ")", "DWUYXJ4pYxHAoiABeeR5og==");
    query("string(" + func.args(" <x/>") + ")", "1B2M2Y8AsgTpgAmY7PhCfg==");
  }

  /** Test method. */
  @Test public void sha1() {
    final Function func = _HASH_SHA1;
    // queries
    query("string(" + func.args("") + ")", "2jmj7l5rSw0yVb/vlWAYkK/YBwk=");
    query("string(" + func.args("BaseX") + ")", "OtWVjw8n1a/9yilXVg8SHQWXpO0=");
    query("string(" + func.args(" <x/>") + ")", "2jmj7l5rSw0yVb/vlWAYkK/YBwk=");
  }

  /** Test method. */
  @Test public void sha256() {
    final Function func = _HASH_SHA256;
    // queries
    query("string(" + func.args("") + ")", "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
    query("string(" + func.args("BaseX") + ")", "FdVwdj3rddcou2lkM5KHO4NczMlKLx6IGQnaR2YoIaM=");
  }
}
