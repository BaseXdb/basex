(:*******************************************************:)
(: Test: modulesdiffns-lib.xq                            :)
(: Written By: Carmelo Montanez                          :)
(: Purpose: Some invalid Module Definitions              :)
(:*******************************************************:)

module namespace defs ="http://www.w3.org/TestModules/diffns";

declare namespace foo = "http://example.org";

declare variable $foo:var1 := 1;