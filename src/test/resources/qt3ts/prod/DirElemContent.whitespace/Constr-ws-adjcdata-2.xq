(: Name: Constr-ws-adjcdata-2 :)
(: Written by: Andreas Behm :)
(: Description: preserve line feed adjacent to cdata section :)

declare boundary-space strip;
<elem>
<![CDATA[]]>
</elem>