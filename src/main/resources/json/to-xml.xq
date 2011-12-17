(:~
 : This script converts all JSON files in this
 : directory to their XML representation.
:)

<files>{
  for $name in file:list('.', false(), '*.json')
  let $file := file:read-text($name)
  let $json := json:parse($file)
	return $json
}</files>
