(:*******************************************************:)
(: Test: modules-recursive1.xq                           :)
(: Written By: Nicolae Brinza                            :)
(: Purpose: Some Module Definitions                      :)
(:*******************************************************:)

module namespace defs1 = "http://www.w3.org/TestModules/defs1";

import module namespace defs2 = "http://www.w3.org/TestModules/defs2"; 


declare variable $defs1:var as xs:integer := $defs2:var; 
