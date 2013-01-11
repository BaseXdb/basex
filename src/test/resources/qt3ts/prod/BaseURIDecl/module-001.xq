module namespace m = "http://www.w3.org/TestModules/module-001";

declare base-uri "http://www.example.org/base2";

declare function m:static-base-uri() as xs:anyURI?
{
   fn:static-base-uri()
};
