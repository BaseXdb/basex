(:*******************************************************:)
(: Test: serialization1-lib.xq                           :)
(: Written By: Dennis Knochenwefel                       :)
(: Date: 2011/12/05                                      :)
(: Purpose: library module containing serialization opts :)
(:*******************************************************:)

module namespace test="http://www.w3.org/TestModules/test";

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";
declare option output:indent "yes";

declare function test:ok ()
{
   "ok"
};
