(: binding sets of the static type soundness test: sparse and rich values :)
let $f := fn($x as xs:integer) as xs:string { string($x) }
let $g := fn($x as xs:integer) as xs:boolean { $x > 1 }
let $h := fn($x as xs:integer, $y as xs:integer) as xs:integer { $x + $y }
return (
  {
    'i': 0, 'io': (), 'is': (), 'ip': 1, 'dc': 0.5, 'd': 0e0, 's': '', 'so': (), 'ss': (),
    'u': xs:untypedAtomic('1'), 'x': (), 'e': <a/>, 'es': (), 'doc': document { <a/> },
    'a': [], 'as': (), 'm': {}, 'r': { 'a': 1, 'b': '' }, 'f': $f, 'g': $g, 'h': $h,
    'b': false(), 'dt': xs:date('2000-01-01'), 'dto': (),
    'dtm': xs:dateTime('2000-01-01T00:00:00'), 'dur': xs:dayTimeDuration('PT1H'),
    'ym': xs:yearMonthDuration('P1M')
  },
  {
    'i': 2, 'io': 3, 'is': (1, 2, 3), 'ip': (1, 2, 3), 'dc': 2.5, 'd': 1.5e0, 's': 'a b',
    'so': 'x', 'ss': ('a', 'b'), 'u': xs:untypedAtomic('2'), 'x': <x>5</x>,
    'e': <a x='1'><a><b/></a>text<!--c--><?pi x?><b/></a>,
    'es': (<a/>, <a x='1'><a/></a>), 'doc': document { <!--c-->, <a x='1'><a/><b/></a> },
    'a': [ 1, 2, 3 ], 'as': ([ 'a' ], [ 'b', 'c' ]), 'm': { 'a': 1, 'b': 2 },
    'r': { 'a': 1, 'b': 'x' }, 'f': $f, 'g': $g, 'h': $h, 'b': true(),
    'dt': xs:date('2000-02-03+01:00'), 'dto': xs:date('2001-01-01'),
    'dtm': xs:dateTime('2000-01-01T10:20:30.5Z'), 'dur': xs:dayTimeDuration('P1DT2H'),
    'ym': xs:yearMonthDuration('P1Y2M')
  }
)
