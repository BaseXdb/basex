(: Name: XQST0088_2 :)
(: Description: check that invalid module declarations are reported correctly :)
(: Author: Tim Mills :)
(: Date: 2008-05-16 :)
module namespace cheese = '';

declare function cheese:cheese()
{
  1 + 2
};
