(:*******************************************************:)
(: Test: modules-recursive2.xq                           :)
(: Written By: Nicolae Brinza                            :)
(: Purpose: Some Module Definitions                      :)
(:*******************************************************:)

module namespace defs2 = "http://www.w3.org/TestModules/defs2";

import module namespace defs1 = "http://www.w3.org/TestModules/defs1";


declare variable $defs2:var as xs:integer := $defs1:var;

