Website-Scraper
===============
The project consists of the following files - 

1. ParserClass.java file which contains the source code for scraping along with the two jar files that were included in the build path for execution.

2. The resulting JSON file, 'PaintingRecords.json', which contains an array of JSON objects, each having key-value pairs of the various fields in the painting. There is data about 360 paintings in the file.


Instructions to run the code - 
1. Compile (preferably in an IDE) the java file along with the jars as -
      	javac "json-simple-1.1.1.jar;json-simple-1.1.1.jar" ParserClass.java
2. Run the java executable that has been created after successful compilation as -\
	      java ParserClass

The succesful execution of the code would result in a JSON file named "PaintingRecords.json" which would contain an array of all the JSON objects, each having the information for a painting.

