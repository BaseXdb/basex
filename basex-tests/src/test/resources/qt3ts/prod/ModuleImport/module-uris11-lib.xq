(:*******************************************************:)
(: Test: module-uris11-lib.xq                             :)
(: Written By: Dennis Knochenwefel                       :)
(: Date: 2011/12/05                                      :)
(: Purpose: uri containing query                         :)
(:*******************************************************:)

module namespace test="http://www.w3.org/TestModules/test?hello=world";

declare function test:ok ()
{
   "ok"
};

