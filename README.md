# Gazebo Migration Tool

The Gazebo Migraiton Tool is a program used to modify the Gazebo databases easily programmatically vs doing it via SQL. Each migration is it's own file and has helper methods that can be used for things like creating tables, adding columns and more.

## Getting Started

To start all you have to do is create a new class named "Migration_xxxx" where xxxx is the next migration number sequentially. All classes in the migration package will be automatically loaded and attempted to be ran.
All migrations are handled in their own transaction and if they fail, they will fully roll back. 

Migration classes must also extend the Migration base class. There are three required methods to override:
 - getDescription, which should return the description of the migration being ran
 - getFailureCase, which should return what the program should do in the case the migration fails. The only options are "STAY_OFFLINE" and "RESUME_NON_CRITICAL". Stay offline halts the program and prevents further migrations from being ran. Resume non-critical will continue with further migrations but will not mark the current migration as completed.
 - run, this is the method that will actually run for the migration.

## Loader System

The Migration Tool also has a system in place to maintain system loaded values such as quests, achievements and seasons. Any class in the loaders package will be attempted to load based on it's name and backing wrapper class. This loader will read the .csv in the resources folder and insert/delete/update any records in the table based on the fields it's given to hash. 
