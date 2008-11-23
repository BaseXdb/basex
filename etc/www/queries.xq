for $item score $s in doc('xmark')//item~  [payment ftcontains 'Checks' with stemming case sensitive]~order by $s descending~return <item ranking='{ $s}'>{ $item/payment/text() }</item>
<calc>{~  for $c in (1, 3, 5, 7, 9)~  return <square num='{ $c }'>{ $c * $c}</square>~}</calc>
for $country in doc('factbook')//country~let $pop := number($country/@population),~    $name := normalize-space($country/name[1])~order by $pop descending~return <country pop="{ $pop }">{ $name }</country>
let $names := ("Jack London", "Jack", "Jim Beam", "Jack Daniels")~for $name at $pos score $score in $names[. ftcontains 'Jack']~order by $score descending~return <name pos="{ $pos }">{ $name }</name>
<noaddress>{~  doc('xmark')//person[not(address)]/name~}</noaddress>
for $name in ("Hanz", "Heinz", "Hans", "Huns", "Hund")~where $name ftcontains 'Hans' with fuzzy~return $name
for $city in doc('factbook')//city/name/text()~where starts-with($city, 'Q')~order by $city~return data($city)
'This a simple text. It contains two sentences.' ftcontains~  ('simple texts' with stemming ftand~    'two sentences' without stemming)~    different sentence distance at most 2 words
<selection>{~  for $auction in doc('xmark')//open_auction~  where $auction/reserve > $auction/initial * 5~  return <auction>{ $auction/@id }</auction>~}</selection>