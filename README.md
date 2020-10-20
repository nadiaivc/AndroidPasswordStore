	1. 	
	User interface (list of passwords)
	Functions for connecting with DB (create, update, insert, delete)
	4 fields: Resource (can't be empty), login, password(can't be empty), notes 
	
	
	2.	
	Show/Hide password in EditorActivity by pressing the button
	New activity - start activity with a field for a password
	
	
	3.	
	The password entered on the first activity is used to encrypt (AES) data in SQLite 
	(you enter password[1] and add a new field and then you exit -> you enter password[2] and you can't see 
	correct form of the field that you create with password[1])
	
	
	4.	
	I don't want to save the login password (previous step) in SharedPreferences. I'm not sure if it had been a good idea 
	// Of course, I tried to use KeyStore, but it doesn't work (yeah, I did smth wrong)
	I did this thing: I have string for check (any constant value) and encrypt it after the first login (by correct login password).
	After that I save this string to SharedPreferences
	Then, when somebody try to login I get an entered password and try to decrypt the string from SharedPreferences. 
	If the value matches the original string, then let the user in
	"MyCheckString" (encrypt with correct password) -> "Wow12345"
	"Wow12345" (decrypt with correct password) -> "MyCheckString"
	"Wow12345" (decrypt with incorrect password) -> "YouShouldGetOutOfHere"
	
	!!! If you know something more appropriate and logical for local login, please tell me!
	
	
	5.
	Func for changing the main password (OptionsItemSelected)
	-> all elements in the DB decrypt with the previous password -> all elements encrypt with the new password
	
	
	6.
	Func for exporting data from a DB to Download directory (XML format)
