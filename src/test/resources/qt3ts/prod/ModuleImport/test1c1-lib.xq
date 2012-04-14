(:*******************************************************:)
(: Test: test1c1-lib.xq                                  :)
(: Written By: Mary Holstege                             :)
(: Date: 2005/12/05 14:47:00                             :)
(: Purpose: Circular import test                         :)
(:*******************************************************:)

module namespace test1="http://www.w3.org/TestModules/test1";

(: start-insert :)
import module namespace test2="http://www.w3.org/TestModules/test2";
(: end-insert :)

declare function test1:ok ()
{
   "ok"
};
