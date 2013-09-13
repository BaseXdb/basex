module namespace b = 'b';
import module namespace a = 'a' at 'a.xqm';
declare variable $b:bar := $a:foo - 2;
