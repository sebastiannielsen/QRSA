#!/usr/bin/perl

use MIME::Base64::URLSafe;
use MIME::Base64;
use CGI ':standard';
use Text::QRCode;


$inkey = param('inkey');
$inkey =~ s/[^abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\-_]*//sgi;
if ($inkey) {

if ($inkey eq "INCOMPATIBLE_DEVICE") {
print "Content-Type: text/html\n\nCouldn't Enroll! Your device is incompatible.";
}
else
{
$key = "-----BEGIN PUBLIC KEY-----\n".encode_base64(urlsafe_b64decode($inkey))."-----END PUBLIC KEY-----";
open (KEYFILE, ">pubkey.txt");
print KEYFILE $key;
close (KEYFILE);
print "Content-Type: text/html\n\nSuccessfully Enrolled!";
}
}
else
{

$qrcode = Text::QRCode->new(level => 'M', casesensitive => 1);
$arref = $qrcode->plot("qrsa://us".urlsafe_b64encode("www.example.org/enroll.cgi?inkey="));
$qrtoprint = join "<\/tr><tr>", map { join '', map { $_ eq '*' ? "<th><\/th>" : "<td><\/td>" } @$_ } @$arref;

print "Content-Type: text/html\n\n<style> #qrcode table {border-spacing: 0;border: 0;margin: 0;} #qrcode td {background-color: #FFFFFF;margin: 0;padding: 0;width: 3px;height: 3px;} #qrcode th{background-color: #000000;margin: 0;padding: 0;width: 3px;height: 3px;}<\/style>";
print "<div id=\"qrcode\"><table><tr>".$qrtoprint."<\/tr><\/table><\/div><br><br>";
print "<a href=\"qrsa:\/\/e\">enroll<\/a><br><form method=\"post\"><input type=\"text\" name=\"inkey\"><input type=\"submit\"><\/form>";
}
