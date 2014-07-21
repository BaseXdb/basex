package org.basex.query.simple;

import org.basex.query.*;

/**
 * XQuery type tests.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class TypeTest extends QueryTest {
  /** Constructor. */
  static {
    queries = new Object[][] {
        { "Simple 1", booleans(true), "1 castable as xs:integer" },
        { "Simple 2", booleans(true), "1 castable as xs:integer?" },
        { "Simple 3", booleans(true), "() castable as xs:integer?" },

        { "SimpleErr 1", "1 castable as xs:integer+" },
        { "SimpleErr 2", "1 castable as xs:integer()" },
        { "SimpleErr 3", "1 castable as xml:integer" },
        { "SimpleErr 4", "1 castable as integer" },
        { "SimpleErr 5", "1 castable as xs:NOTATION" },
        { "SimpleErr 6", "1 castable as xs:anyAtomicType" },

        { "Type 1", booleans(true), "1 instance of item()" },
        { "Type 2", booleans(true), "1 instance of xs:anyAtomicType" },
        { "Type 3", booleans(true), "1 instance of xs:decimal" },
        { "Type 4", booleans(true), "1 instance of xs:integer" },
        { "Type 5", booleans(false), "1 instance of xs:string" },
        { "Type 6", booleans(false), "1 instance of xs:untypedAtomic" },

        { "TypeErr 1", "1 instance of xs:abcde" },
        { "TypeErr 2", "1 instance of xs:string()" },
        { "TypeErr 3", "1 instance of item" },
    };
  }
}
