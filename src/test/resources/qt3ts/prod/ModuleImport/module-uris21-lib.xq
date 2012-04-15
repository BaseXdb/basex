(:*******************************************************:)
(: Test: module-uris21-lib.xq                            :)
(: Written By: Dennis Knochenwefel                       :)
(: Date: 2011/12/05                                      :)
(: Purpose: uri ldap                                     :)
(:*******************************************************:)

module namespace test="ldap://[2001:db8::7]/c=GB?objectClass?one";

declare function test:ok ()
{
   "ok"
};

