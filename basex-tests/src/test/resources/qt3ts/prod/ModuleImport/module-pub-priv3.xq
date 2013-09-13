xquery version "3.0";

(:***********************************************************:)
(: Test: module-pub-priv3.xq                                 :)
(: Written By: Josh Spiegel                                  :)
(: Date: 2012-01-04                                          :)
(: Purpose: calls a public function in another module        :)
(:***********************************************************:)

module namespace mod3="http://www.w3.org/TestModules/module-pub-priv3";

import module namespace mod="http://www.w3.org/TestModules/module-pub-priv";

declare function mod3:f($a as xs:integer) {
   mod:g($a)
};
