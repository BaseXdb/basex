(:*******************************************************:)
(: Test: modulesDefs-lib.xq                              :)
(: Written By: Carmelo Montanez                          :)
(: Purpose: Some Module Definitions                      :)
(:*******************************************************:)

module namespace defs ="http://www.w3.org/TestModules/defs";

(: insert-start :)
import module namespace test1="http://www.w3.org/TestModules/test1";
(: insert-end :)

declare namespace foo = "http://example.org";

declare variable $defs:var1 := 1;
declare variable $defs:var2 := $test1:flag + 2;

declare function defs:ok ()
{
   "ok"
};
