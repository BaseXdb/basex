(:~
 : Hello World.
 :
 : @author BaseX GmbH, 2014
 :)
module namespace _ = 'hello';

(:~
 : Says hello.
 :
 : @return response string as json
 :)
declare
  %rest:path("/hello")
  %rest:query-param("name", "{$name}", "anonymous friend")
  %output:method("json")
function _:hello($name) {
  <json type="object">
    <greeting>Welcome to RestXQ, {$name}!</greeting>
  </json>
};
