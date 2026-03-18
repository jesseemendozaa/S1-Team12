# FishFinder NorCal — Environment Setup

## Prerequisites
- **Java JDK 22** — `C:\Program Files\Java\jdk-22`
- **MySQL 8.0** — `C:\Program Files\MySQL\MySQL Server 8.0` (runs on **port 3307**)
- **Apache Tomcat 9.0.115** — `C:\Users\minston\Downloads\apache-tomcat-9.0.115-windows-x64\apache-tomcat-9.0.115`
- **MySQL Connector/J 9.6.0** — `C:\Users\minston\Downloads\mysql-connector-j-9.6.0\mysql-connector-j-9.6.0\mysql-connector-j-9.6.0.jar`

## Project Structure
```
S1-Team12/
├── setup.md
└── webapps/
    └── FishFinderNorCal/
        ├── home.html
        ├── css/style.css
        ├── js/app.js
        ├── sql/schema.sql
        ├── src/                    # Java servlet source files
        │   ├── DBUtil.java
        │   ├── PasswordUtil.java
        │   ├── LoginServlet.java
        │   ├── RegisterServlet.java
        │   ├── LogoutServlet.java
        │   ├── AccountServlet.java
        │   ├── DashboardServlet.java
        │   ├── LocationListServlet.java
        │   ├── LocationDetailServlet.java
        │   ├── LocationFormServlet.java
        │   ├── CatchReportListServlet.java
        │   ├── CatchReportFormServlet.java
        │   ├── CatchReportEditServlet.java
        │   ├── CommentServlet.java
        │   ├── FavoriteServlet.java
        │   └── FavoriteListServlet.java
        └── WEB-INF/
            ├── web.xml
            └── jsp/                # JSP templates
                ├── header.jsp
                ├── footer.jsp
                ├── login.jsp
                ├── register.jsp
                ├── dashboard.jsp
                ├── account.jsp
                ├── locations.jsp
                ├── locationDetail.jsp
                ├── locationForm.jsp
                ├── reports.jsp
                ├── reportForm.jsp
                ├── reportDetail.jsp
                ├── favorites.jsp
                └── error.jsp
```

## Database
- **Database name:** `fishfindernorcaldb`
- **Port:** 3307
- **User:** root
- **MySQL config file:** `C:\Users\minston\.my.cnf` (stores credentials)
- **Connection string:** `jdbc:mysql://localhost:3307/fishfindernorcaldb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- **10 tables:** users, moderators, locationtypes, locations, species, catchreports, comments, bans, favorites, locationspecies

## MySQL CLI Access
```bash
mysql --defaults-file="C:/Users/minston/.my.cnf" --get-server-public-key fishfindernorcaldb
```

## Compile Servlets
Bash semicolons break the classpath on Windows Git Bash. Use an argfile:

```bash
TOMCAT_HOME="/c/Users/minston/Downloads/apache-tomcat-9.0.115-windows-x64/apache-tomcat-9.0.115"
PROJECT="/c/Users/minston/projects/cs157a/final_project/S1-Team12/webapps/FishFinderNorCal"
WEBAPP="$TOMCAT_HOME/webapps/FishFinderNorCal"
MYSQL_JAR="/c/Users/minston/Downloads/mysql-connector-j-9.6.0/mysql-connector-j-9.6.0/mysql-connector-j-9.6.0.jar"

# Build argfile (avoids semicolon issues in Git Bash)
cat > /tmp/javac_args.txt << 'EOF'
-cp
EOF
echo "C:\\Users\\minston\\Downloads\\apache-tomcat-9.0.115-windows-x64\\apache-tomcat-9.0.115\\lib\\servlet-api.jar;C:\\Users\\minston\\Downloads\\apache-tomcat-9.0.115-windows-x64\\apache-tomcat-9.0.115\\lib\\jsp-api.jar;C:\\Users\\minston\\Downloads\\mysql-connector-j-9.6.0\\mysql-connector-j-9.6.0\\mysql-connector-j-9.6.0.jar" >> /tmp/javac_args.txt
echo "-d" >> /tmp/javac_args.txt
cygpath -w "$WEBAPP/WEB-INF/classes" >> /tmp/javac_args.txt
for f in $PROJECT/src/*.java; do
  cygpath -w "$f" >> /tmp/javac_args.txt
done

javac @/tmp/javac_args.txt
```

## Deploy to Tomcat
```bash
TOMCAT_HOME="/c/Users/minston/Downloads/apache-tomcat-9.0.115-windows-x64/apache-tomcat-9.0.115"
PROJECT="/c/Users/minston/projects/cs157a/final_project/S1-Team12/webapps/FishFinderNorCal"
WEBAPP="$TOMCAT_HOME/webapps/FishFinderNorCal"

# Copy web resources
cp "$PROJECT/home.html" "$WEBAPP/"
cp -r "$PROJECT/css" "$WEBAPP/"
cp -r "$PROJECT/js" "$WEBAPP/"
cp "$PROJECT/WEB-INF/web.xml" "$WEBAPP/WEB-INF/"
cp "$PROJECT/WEB-INF/jsp/"*.jsp "$WEBAPP/WEB-INF/jsp/"

# Remove duplicate old servlet classes (Login, Logout, Register conflict with LoginServlet etc.)
rm -f "$WEBAPP/WEB-INF/classes/Login.class" "$WEBAPP/WEB-INF/classes/Logout.class" "$WEBAPP/WEB-INF/classes/Register.class"
```

## Start / Stop Tomcat
```bash
export CATALINA_HOME="/c/Users/minston/Downloads/apache-tomcat-9.0.115-windows-x64/apache-tomcat-9.0.115"
export JAVA_HOME="/c/Program Files/Java/jdk-22"

# Start (port 9090 — Jenkins uses 8080)
bash "$CATALINA_HOME/bin/startup.sh"

# Stop
bash "$CATALINA_HOME/bin/shutdown.sh"
```

## App URL
**http://localhost:9090/FishFinderNorCal/**

## Known Gotchas
- **Port 8080 is taken by Jenkins** — Tomcat runs on **9090** (`conf/server.xml` was changed)
- **Git Bash eats semicolons** in classpath args — use the argfile approach for `javac`
- **Duplicate servlet files** — `Login.java`, `Logout.java`, `Register.java` are old duplicates of `LoginServlet.java`, `LogoutServlet.java`, `RegisterServlet.java`. They compile but must be **deleted from WEB-INF/classes** or Tomcat fails with URL mapping conflicts
- **No JSTL** — all JSPs use scriptlets, not `<c:forEach>` / `<c:if>` tags
- **MySQL auth** — `caching_sha2_password` requires `--get-server-public-key` flag or `allowPublicKeyRetrieval=true` in JDBC URL
