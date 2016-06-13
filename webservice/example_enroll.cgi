#!/usr/bin/perl

use MIME::Base64::URLSafe;
use MIME::Base64;
use CGI ':standard';

$inkey = param('inkey');
$inkey =~ s/[^abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789\-_]*//sgi;
if ($inkey) {
$key = "-----BEGIN PUBLIC KEY-----\n".encode_base64(urlsafe_b64decode($inkey))."-----END PUBLIC KEY-----";
open (KEYFILE, ">pubkey.txt");
print KEYFILE $key;
close (KEYFILE);
print "Content-Type: text/html\n\nSuccessfully Enrolled!";
}
else
{
print "Content-Type: text/html\n\n<a href=\"qrsa:\/\/e\">enroll<\/a><br><form method=\"post\"><textarea name=\"inkey\"><\/textarea><input type=\"submit\"><\/form>";
}
