module namespace m = "http://www.w3.org/TestModules/dfd-module-001";

declare decimal-format df001 grouping-separator="'";

declare function m:do() as xs:string
{
   fn:format-number(123456.789, "#'###'###.###", 'df001')
};
