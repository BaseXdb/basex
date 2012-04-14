(:*******************************************************:)
(: Test: module-uris10-lib.xq                            :)
(: Written By: Dennis Knochenwefel                       :)
(: Date: 2011/12/05                                      :)
(: Purpose: uri with type param                          :)
(:*******************************************************:)

module namespace test="ftp://www.w3.org/TestModules/test;type=A";

declare function test:ok ()
{
   "ok"
};

