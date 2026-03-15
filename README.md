# Fish Finder Norcal

## Functional Requirements
- Register an Account
- Login
- Logout


# How to Run Website

1. Place the 'Website' folder inside your tomcat/webapps/ folder on your local machine

2. Use cd to navigate to Website folder
cd Website

3. Use cd to navigate to src folder 
cd src

4. Edit the Login.java and Register.java file with your password for SQL. Edit the text 'YOUR_PASSWORD_HERE'

5. Compile using following command: (You may need to edit the file path after -cp to compile correctly)
javac -cp “/Applications/tomcat/lib/servlet-api.jar” -d WEB-INF/classes src/*.java

6. Open SQL workbench

7. Enter the following:

CREATE DATABASE login_info_db;
USE login_info_db;

CREATE TABLE users (
	username VARCHAR(50) PRIMARY KEY,
    	password VARCHAR(50)
    	);

8. Then Click the lightning bolt and then refresh your schemas. You should see a new schema now

9. Start Tomcat (Below is an example, this may vary based off your filepath of where tomcat is)

cd /Applications/tomcat/bin/
sh startup.sh

10. Now go to your internet browser and enter the following into url:
http://localhost:8080/Website/home.html 
