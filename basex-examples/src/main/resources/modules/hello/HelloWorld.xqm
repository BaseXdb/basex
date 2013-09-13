(:~
 : This is an example for a module that can be added to the BaseX repository.
 : @author BaseX Team 2005-12, BSD License
 :)
module namespace m = 'http://basex.org/modules/Hello';

(:~
 : Say hello to someone.
 : @param $world the one to be greeted
 : @return welcome string
 :)
declare function m:hello($world) {
  'Hello ' || $world
};
