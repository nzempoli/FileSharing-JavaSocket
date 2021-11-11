Initial setup:
IMPORTANT: Client and Server classes are capitalized, so ensure the commands take this into account.

1. Unpack pa1.jar onto your machine where you would like to run the program
	a. The folder into which you place the jar will be considered your root directory if absolute filepaths aren't given.
2. In the folder where pa1.jar is located run: 
	a. java -cp pa1.jar Server start 5555
	b. This will initialize the server at localhost:5555
3. In the terminal window/area where the client will be run set:
	a. set PA1_SERVER=localhost:5555
	b. This will set the client to look at local host port 5555

Commands:
1. Download/Upload - 
	Important: When running these ensure the full file path to the file host (location where the file is located) 
	including the file name is specified (first parameter). The second parameter should solely include the
	directory the file will be placed in. The program will place the uploaded/downloaded file with the same
	name and extension automatically. 
	a. java -cp pa1.jar Client upload </path/filename/on/client> <path_on_client>
	b. java -cp pa1.jar Client download </path/existing_filename/on/server> <path_on_client>
2. Directory manipulation
	Java -cp pa1.jar Client dir </path/existing_directory/on/server>
	java -cp pa1.jar Client mkdir </path/new_directory/on/server>
	java -cp pa1.jar Client rmdir </path/existing_directory/on/server>
3. Removing File: 
	java -cp pa1.jar Client rm </path/existing_filename/on/server>
4. Shutdown: 
	-Shutdown will tell the client it's shut down, while allowing all threads currently running on the server to finish.
	-Any attempts to connect to the server after this will fail.
	java -cp pa1.jar Client shutdown

Errors:
There are types of errors in the program, setup errors and connection errors. Setup Errors are errors such as: attempted
to use rm to delete a directory, or attempting to mess with an incorrect file. Note: Initial rejections of incorrectly formatted user input
is not classified as an error as the users just told to change their input.

Connection Errors occur when the systems are communicating, but something occurs that triggers an Exception. In these cases the users
just told what process the exception happened.
