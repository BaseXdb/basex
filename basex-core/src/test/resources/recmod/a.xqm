module namespace a = 'a';
import module namespace b = 'b' at 'b.xqm';
declare variable $a:foo := 23;
declare function a:bar() { $b:bar * 2 };
