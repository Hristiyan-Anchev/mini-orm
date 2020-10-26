# mini-orm

This is a custom ORM that supports insertion, update and retrieving of database entries using JDBC API.
Under no circumstances should you use this in a real project as its prone to injection attacks.
Feel free to implement the rest of the CRUD functionality.

This is only a proof of concept.

#How to:
Once you download the code create "db-config.env" file in your main directory.
Type your database username on the first line of the file,
and your database password on the second line.

When you run the project it will populate the database with random values
and then make a call to all the functions that the ORM supports so far.
