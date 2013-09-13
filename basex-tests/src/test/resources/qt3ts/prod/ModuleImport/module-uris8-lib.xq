(:*******************************************************:)
(: Test: module-uris8-lib.xq                             :)
(: Written By: Dennis Knochenwefel                       :)
(: Date: 2011/12/05                                      :)
(: Purpose: uri containint unusual characters            :)
(:*******************************************************:)

module namespace test="http://www.w3.org/TestModules/&#x3c;&#x3d;&#x3e;&#x40;/test";

declare function test:ok ()
{
   "ok"
};

