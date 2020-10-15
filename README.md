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
	
	
