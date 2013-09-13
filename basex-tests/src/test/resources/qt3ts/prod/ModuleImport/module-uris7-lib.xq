(:*******************************************************:)
(: Test: module-uris7-lib.xq                             :)
(: Written By: Dennis Knochenwefel                       :)
(: Date: 2011/12/05                                      :)
(: Purpose: uri containing unicode characters            :)
(:*******************************************************:)

module namespace test="http://www.w3.org/TestModules/&#xd0a4;/test";

declare function test:ok ()
{
   "ok"
};
