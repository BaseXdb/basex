(:*******************************************************:)
(: Test: module-uris2-lib.xq                             :)
(: Written By: Dennis Knochenwefel                       :)
(: Date: 2011/12/05                                      :)
(: Purpose: module uri containing whitespaces            :)
(:*******************************************************:)

module namespace test="&#x20;&#x9;&#xA;&#xD;http://www.w3.org/Test&#x20;&#x9;&#xA;&#xD;Modules/test&#x20;&#x9;&#xA;&#xD;";

declare function test:ok ()
{
   "ok"
};
