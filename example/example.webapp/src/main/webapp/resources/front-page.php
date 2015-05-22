<?php
global $wp_scripts;
class WP_NoScripts extends WP_Scripts {
	public function do_item( $handle, $group = false ) {
		return false;
	}
}

$wp_scripts = new WP_NoScripts();
function returnFalse(){
	return false;
}
add_filter('show_admin_bar', 'returnFalse');
apply_filters( 'style_loader_tag', '', $handle );
?>
<!DOCTYPE html>
<html>
<head>
	<title>До работа с колело</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="viewport" content="width=device-width">
	<link href="http://bike2work.be/wp-content/themes/twentyfifteen/front-page.css" rel="stylesheet" />
	<script type='text/javascript' src='http://bike2work.be/wp-includes/js/jquery/jquery.js'></script>
	<script type="text/javascript">
	jQuery(document).ready(function() {
		jQuery('.card').bind('mouseenter', function(e) {
			jQuery(this).addClass('selected');
		});
		jQuery('.card').bind('touchstart', function(e) {
			jQuery(this).addClass('touched');
		});
		jQuery('.card').bind('mouseleave', function(e) {
			jQuery(this).removeClass('selected');
			jQuery(this).removeClass('touched');
			jQuery(this).removeClass('alreadyClicked');
		});
		jQuery('.card').bind('click', function(e) {
			if (jQuery(this).hasClass('touched')) {
				if (!jQuery(this).hasClass('alreadyClicked')) {
					e.preventDefault();
					jQuery(this).addClass('alreadyClicked');
					return;
				}
			}
			window.open(jQuery(this).attr("data-url"), "_self")
		});
	});
	</script>
</head>

<body>

<div class="pageContainer">
<div class="scrollableContainer">
<div class="containerTable">
<div class="containerCell">
<div class="dummy"></div>
<div class="cardHolder">
<div class="card" data-url="/campaign"><div class="card-content"><div class="card-text">ЗА КАМПАНИЯТА</div></div><img src="http://bike2work.be/wp-content/uploads/2015/04/1.gif"/></div>
<div class="card" data-url="/campaign/participate"><div class="card-content"><div class="card-text">УЧАСТВАЙ<br/><div style="font-weight: normal; font-size: 11pt;">Вход / Регистрация</div></div></div><img src="http://bike2work.be/wp-content/uploads/2015/04/2.gif"/></div>
<div class="card" data-url="/campaign/bybiker"><div class="card-content"><div class="card-text">КЛАСИРАНЕ</div></div><img src="http://bike2work.be/wp-content/uploads/2015/04/3.gif"/></div>
<div class="clear"></div>
<div class="card" data-url="/campaign/byteam"><div class="card-content"><div class="card-text">ОРГАНИЗАТОРИ</div></div><img src="http://bike2work.be/wp-content/uploads/2015/04/4.gif"/></div>
<div class="card" data-url="/history"><div class="card-content"><div class="card-text">ИСТОРИЯ</div></div><img src="http://bike2work.be/wp-content/uploads/2015/04/5.gif"/></div>
</div>
<div class="dummy"></div>
</div>
</div>
</div>
</div>

</body>
</html>
