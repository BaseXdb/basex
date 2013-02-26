(:*******************************************************:)
(: Test: K2-DirectConOther-51                            :)
(: Written by: Frans Englich                             :)
(: Date: 2007-11-22T11:31:21+01:00                       :)
(: Purpose: Ensure that EOL-normalization also takes place in CDATA sections(#2). :)
(:*******************************************************:)
string(<e><![CDATA[]]></e>) eq "&#xA;"