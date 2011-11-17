import module namespace example = "http://basex.org/modules/mapExample";

let $map := map { 'key' := 'value', 'bla' := 'blu' }
return example:test($map, 'bla')
