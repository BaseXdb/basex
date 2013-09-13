(:*******************************************************:)
(: Test: K2-DirectConElemAttr-75                         :)
(: Written by: Frans Englich                             :)
(: Date: 2007-11-22T11:31:21+01:00                       :)
(: Purpose: Mix several ways for creating text for various kinds of nodes. This test is useful if an implementation is performing normalization of such constructors. :)
(:*******************************************************:)
<e attribute="{
"abc",
"def",
"ghi",
1,
2,
3,
xs:untypedAtomic("abc"),
text {"a text node"},
text {"a text node"},
xs:untypedAtomic("def"),
xs:untypedAtomic("ghi"),
xs:hexBinary("FF"),
xs:untypedAtomic("abc"),
xs:string("def"),
xs:untypedAtomic("ghi")
}
textNode
{"xs:string"}
textNode
{"xs:string"}
textNode
{"xs:string"}
text {"a text node"},
{"xs:string"}
{"xs:string"}textnode">
{ 

attribute name
{
text {"a text node"},
text {"a text node"},
"abc",
"def",
text {"a text node"},
"ghi",
1,
2,
text {"a text node"},
3,
xs:untypedAtomic("abc"),
xs:untypedAtomic("def"),
text {"a text node"},
xs:untypedAtomic("ghi"),
xs:hexBinary("FF"),
xs:untypedAtomic("abc"),
xs:string("def"),
xs:untypedAtomic("ghi"),
"xs:string",
xs:untypedAtomic("ghi"),
"xs:string",
"xs:string",
"xs:string",
xs:untypedAtomic("ghi")
}
}
{
text {"a text node"},
text {"a text node"},
"abc",
"def",
text {"a text node"},
"ghi",
1,
2,
text {"a text node"},
3,
xs:untypedAtomic("abc"),
text {"a text node"},
xs:untypedAtomic("def"),
xs:untypedAtomic("ghi"),
xs:hexBinary("FF"),
xs:untypedAtomic("abc"),
xs:string("def"),
xs:untypedAtomic("ghi")
}
textNode
{"xs:string"}
textNode
{"xs:string"}
textNode
{"xs:string"}
{"xs:string"}
{"xs:string"}
text {"a text node"},
text {"a text node"},
text {"a text node"},
text {"a text node"},

{
comment
{
"abc",
"def",
"ghi",
1,
2,
3,
xs:untypedAtomic("abc"),
xs:untypedAtomic("def"),
xs:untypedAtomic("ghi"),
xs:hexBinary("FF"),
xs:untypedAtomic("abc"),
xs:string("def"),
xs:untypedAtomic("ghi"),
"xs:string",
"xs:string",
xs:untypedAtomic("ghi"),
"xs:string",
text {"a text node"},
text {"a text node"},
"xs:string",
xs:untypedAtomic("ghi"),
"xs:string"
},
processing-instruction target
{
"abc",
"def",
"ghi",
1,
2,
3,
xs:untypedAtomic("abc"),
xs:untypedAtomic("def"),
xs:untypedAtomic("ghi"),
text {"a text node"},
text {"a text node"},
xs:hexBinary("FF"),
xs:untypedAtomic("abc"),
xs:string("def"),
xs:untypedAtomic("ghi"),
"xs:string",
"xs:string",
xs:untypedAtomic("ghi"),
"xs:string",
"xs:string",
xs:untypedAtomic("ghi"),
text {"a text node"},
"xs:string"
},
text
{
text {"a text node"},
text {"a text node"},
text {"a text node"},
text {"a text node"},
"abc",
"def",
"ghi",
1,
2,
text {"a text node"},
3,
xs:untypedAtomic("abc"),
text {"a text node"},
xs:untypedAtomic("def"),
xs:untypedAtomic("ghi"),
xs:hexBinary("FF"),
xs:untypedAtomic("abc"),
xs:string("def"),
xs:untypedAtomic("ghi"),
"xs:string",
"xs:string",
xs:untypedAtomic("ghi"),
"xs:string",
"xs:string",
xs:untypedAtomic("ghi"),
"xs:string",
text {"a text node"}
}

}
</e>