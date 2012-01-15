package org.basex.test.query.simple;

import org.basex.test.query.QueryTest;

/**
 * XQuery type tests.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class TypeTest extends QueryTest {
  /** Constructor. */
  static {
    doc = "<dummy/>";

    queries = new Object[][] {
        { "Simple 1", bool(true), "1 castable as xs:integer" },
        { "Simple 2", bool(true), "1 castable as xs:integer?" },
        { "Simple 3", bool(true), "() castable as xs:integer?" },

        { "SimpleErr 1", "1 castable as xs:integer+" },
        { "SimpleErr 2", "1 castable as xs:integer()" },
        { "SimpleErr 3", "1 castable as xml:integer" },
        { "SimpleErr 4", "1 castable as integer" },
        { "SimpleErr 5", "1 castable as xs:NOTATION" },
        { "SimpleErr 6", "1 castable as xs:anyAtomicType" },

        { "Type 1", bool(true), "1 instance of item()" },
        { "Type 2", bool(true), "1 instance of xs:anyAtomicType" },
        { "Type 3", bool(true), "1 instance of xs:decimal" },
        { "Type 4", bool(true), "1 instance of xs:integer" },
        { "Type 5", bool(false), "1 instance of xs:string" },
        { "Type 6", bool(false), "1 instance of xs:untypedAtomic" },

        { "TypeErr 1", "1 instance of xs:abcde" },
        { "TypeErr 2", "1 instance of xs:string()" },
        { "TypeErr 3", "1 instance of item" },

        /*
         * Sequence type:
         * - for/let
         * - some/every
         * - instance of
         * - typeswitch
         * - treat as
         * - variable declaration
         * - function declaration (arguments, return value)
         *
         * Type.find:
         * - function declaration
         * - sequence type
         * - bind, query processor
         * - Functions.get
         */
    };
  }
}
