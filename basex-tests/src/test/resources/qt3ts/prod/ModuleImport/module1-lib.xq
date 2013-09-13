(:*******************************************************:)
(: Test: module1-lib.xq                                  :)
(: Written By: Carmelo Montanez                         :)
(: Date: 2006/07/13                                     :)
(: Purpose: Library module with function                :)
(:*******************************************************:)

module namespace mod1="http://www.w3.org/TestModules/module1";

import module namespace mod2="http://www.w3.org/TestModules/module2";

declare function mod1:x() {"x", mod2:y(), "x"};