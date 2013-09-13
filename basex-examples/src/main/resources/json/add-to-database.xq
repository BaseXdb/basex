(:~
 : This script adds all JSON files to the
 : specified database.
:)

declare variable $database := 'db';

(: add new files :)
for $name in file:list('.', false(), '*.json')
let $file := file:read-text($name)
let $json := json:parse($file)
return db:add($database, document { $json }, $name)
