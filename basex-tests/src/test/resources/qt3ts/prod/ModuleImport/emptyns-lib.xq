(:*******************************************************:)
(: Test: emptyns-lib.xqy                                 :)
(: Written By: Mary Holstege                             :)
(: Date: 2005/12/05 14:47:00                             :)
(: Purpose: Library module with empty namespace          :)
(:*******************************************************:)

module namespace test1="";

declare function test1:ok ()
{
   "ok"
};
