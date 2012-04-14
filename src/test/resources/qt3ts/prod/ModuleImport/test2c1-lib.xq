(:*******************************************************:)
(: Test: test2c1-lib.xq                                  :)
(: Written By: Mary Holstege                             :)
(: Date: 2005/12/05 14:47:00                             :)
(: Purpose: Circular import test                         :)
(:*******************************************************:)

module namespace test2="http://www.w3.org/TestModules/test2";

(: start-insert :)
import module namespace test1="http://www.w3.org/TestModules/test1";
(: end-insert :)

declare function test2:ok ()
{
   "ok"
};
