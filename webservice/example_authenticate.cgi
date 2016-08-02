#!/usr/bin/perl

use Crypt::OpenSSL::RSA;
use MIME::Base64::URLSafe;
use Text::QRCode;
use Digest::MD5 qw(md5_hex);

$key = "";
open(KEYFILE, "pubkey.txt");
foreach $k (<KEYFILE>) {
$key = $key . $k;
}
close(KEYFILE);
$rsa_pub = Crypt::OpenSSL::RSA->new_public_key($key);
$rsa_pub->use_pkcs1_padding();
@b32chars = ("a".."z","0","1","2","3","4","5");
@allchars = ("A".."Z", "a".."z", "0".."9");
$otpcode = "";
$otpcode = $otpcode . $b32chars[rand @b32chars] for 1..16;
$msg = "Sebbe testar...";
$validation = md5_hex($otpcode . $msg . $otpcode);
$fullmsg = "a::".$otpcode."::".$msg."::".$validation."::z";

if (length($fullmsg) < 244) {
$left = 244 - length($fullmsg);
$padding = "";
$padding = $padding . $allchars[rand @allchars] for 1..(rand($left));
$fullmsg = $padding . $fullmsg;
if (length($fullmsg) < 244) {
$left = 244 - length($fullmsg);
$padding = "";
$padding = $padding . $allchars[rand @allchars] for 1..(rand($left));
$fullmsg = $fullmsg . $padding;
}
}

$ciphertext = $rsa_pub->encrypt($fullmsg);
$ctext = urlsafe_b64encode($ciphertext);

$linkdata = "qrsa://c".$ctext;
$qrdata = "qrsa://s".$ctext;
$qrcode = Text::QRCode->new(level => 'M', casesensitive => 1);
$arref = $qrcode->plot($qrdata);
$qrtoprint = join "<\/tr><tr>", map { join '', map { $_ eq '*' ? "<th><\/th>" : "<td><\/td>" } @$_ } @$arref;

print "Content-Type: text/html\n\n<style> #qrcode table {border-spacing: 0;border: 0;margin: 0;} #qrcode td {background-color: #FFFFFF;margin: 0;padding: 0;width: 3px;height: 3px;} #qrcode th {background-color: #000000;margin: 0;padding: 0;width: 3px;height: 3px;}<\/style>";
print "<div id=\"qrcode\"><table><tr>".$qrtoprint."<\/tr><\/table><\/div><br><br>";
print "<a href=\"".$linkdata."\">Authenticate using the mobile.<\/a><br>";
