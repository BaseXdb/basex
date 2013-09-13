xquery version "3.0";

(:***********************************************************:)
(: Test: module-pub-priv2.xq                                 :)
(: Written By: Josh Spiegel                                  :)
(: Date: 2012-01-04                                          :)
(: Purpose: calls a private function in another module       :)
(:***********************************************************:)

module namespace mod2="http://www.w3.org/TestModules/module-pub-priv2";

import module namespace mod="http://www.w3.org/TestModules/module-pub-priv";

declare function mod2:fails() {
   mod:f()
};
