(:*******************************************************:)
(: Test: module-uris13-lib.xq                            :)
(: Written By: Dennis Knochenwefel                       :)
(: Date: 2011/12/05                                      :)
(: Purpose: uri containing param, query and fragment     :)
(:*******************************************************:)

module namespace test="ftp://www.w3.org/TestModules/test;type=A?hello=world&amp;q#world";

declare function test:ok ()
{
   "ok"
};

