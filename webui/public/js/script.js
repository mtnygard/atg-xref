$(document).ready(function() {
  $('#moduleplaceholder').load('/v1/modules');
  $('#componentplaceholder').load('/v1/components');
  $('#classplaceholder').load('/v1/classes');
  $('#jspplaceholder').load('/v1/jsps');
});
