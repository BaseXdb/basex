xquery version "3.0";

(:***********************************************************:)
(: Test: module-pub-priv.xq                                  :)
(: Written By: Carmelo Montanez                              :)
(: Date: 2009-10-28                                          :)
(: Purpose: Library module with public and private functions :)
(:***********************************************************:)

module namespace mod="http://www.w3.org/TestModules/module-pub-priv";

declare %fn:public variable $mod:one := 1;

declare %fn:private variable $mod:two := 2;

declare %fn:private function mod:f() { 23 };

declare %fn:public function mod:g($a as xs:integer) {
   mod:f() + $a + $mod:two - 2*$mod:one
};

declare function mod:h($a as xs:integer) {
   mod:g($a)
};

declare variable $mod:ninety := mod:g(42) + mod:f() + $mod:two div $mod:one;