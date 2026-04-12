# FishFinder NorCal — Setup Guide

## What You Need
1. **Java JDK 17+** (we use 22)
2. **MySQL 8.0** running on any port
3. **Apache Tomcat 9.x** (download from https://tomcat.apache.org/download-90.cgi)
4. **MySQL Connector/J 9.x** JAR (download from https://dev.mysql.com/downloads/connector/j/)

## 1. Set Up the Database

Open MySQL Workbench or the mysql CLI and run the entire `webapps/FishFinderNorCal/sql/schema.sql` file. This creates the `fishfindernorcaldb` database, all 10 tables, and seed data.

```
mysql -u root -p < webapps/FishFinderNorCal/sql/schema.sql
```

If your MySQL is on a non-default port (e.g. 3307), add `--port=3307`.

## 2. Configure Database Environment Variables

The app reads its MySQL connection settings from environment variables at Tomcat startup:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3307"
$env:DB_NAME="fishfindernorcaldb"
$env:DB_USER="root"
$env:DB_PASSWORD="YOUR_PASSWORD"
```

If `DB_PASSWORD` is not set, the app will fail fast with a clear configuration error instead of a generic 500.

## 3. Set Up Tomcat

1. Extract Tomcat 9.x somewhere (e.g. `C:\tomcat9`)
2. Copy the MySQL Connector JAR into `TOMCAT/webapps/FishFinderNorCal/WEB-INF/lib/`
3. If port 8080 is taken, edit `TOMCAT/conf/server.xml` and change the Connector port
4. The bundled Tomcat config in this repo already uses `8081` for HTTP and `8006` for shutdown

## 4. Compile the Servlets

The classpath needs `servlet-api.jar`, `jsp-api.jar` (from Tomcat's `lib/`), and the MySQL connector JAR.

On **Windows (Git Bash)**, semicolons in classpath break, so use an argfile:

```bash
TOMCAT_HOME="/path/to/tomcat9"
PROJECT="/path/to/S1-Team12/webapps/FishFinderNorCal"
WEBAPP="$TOMCAT_HOME/webapps/FishFinderNorCal"
MYSQL_JAR="/path/to/mysql-connector-j-9.x.jar"

mkdir -p "$WEBAPP/WEB-INF/classes" "$WEBAPP/WEB-INF/lib"
cp "$MYSQL_JAR" "$WEBAPP/WEB-INF/lib/"

cat > /tmp/javac_args.txt << EOF
-cp
EOF
echo "$(cygpath -w "$TOMCAT_HOME/lib/servlet-api.jar");$(cygpath -w "$TOMCAT_HOME/lib/jsp-api.jar");$(cygpath -w "$MYSQL_JAR")" >> /tmp/javac_args.txt
echo "-d" >> /tmp/javac_args.txt
cygpath -w "$WEBAPP/WEB-INF/classes" >> /tmp/javac_args.txt
for f in $PROJECT/src/*.java; do
  cygpath -w "$f" >> /tmp/javac_args.txt
done

javac @/tmp/javac_args.txt
```

On **Mac/Linux** it's simpler:

```bash
javac -cp "$TOMCAT_HOME/lib/servlet-api.jar:$TOMCAT_HOME/lib/jsp-api.jar:$MYSQL_JAR" \
  -d "$WEBAPP/WEB-INF/classes" $PROJECT/src/*.java
```

## 5. Deploy

```bash
cp $PROJECT/home.html "$WEBAPP/"
cp -r $PROJECT/css "$WEBAPP/"
cp -r $PROJECT/js "$WEBAPP/"
cp $PROJECT/WEB-INF/web.xml "$WEBAPP/WEB-INF/"
cp $PROJECT/WEB-INF/jsp/*.jsp "$WEBAPP/WEB-INF/jsp/"
```

## 6. Start Tomcat

```bash
export CATALINA_HOME="/path/to/tomcat9"
export JAVA_HOME="/path/to/jdk"
export DB_HOST="localhost"
export DB_PORT="3307"
export DB_NAME="fishfindernorcaldb"
export DB_USER="root"
export DB_PASSWORD="YOUR_PASSWORD"
bash "$CATALINA_HOME/bin/startup.sh"
```

Open **http://localhost:8081/FishFinderNorCal/** for this bundled Tomcat setup (or whatever port you configured).

For this repo-local bundled Tomcat on Windows PowerShell, the equivalent startup flow is:

```powershell
$env:DB_HOST="localhost"
$env:DB_PORT="3307"
$env:DB_NAME="fishfindernorcaldb"
$env:DB_USER="root"
$env:DB_PASSWORD="YOUR_PASSWORD"
$env:CATALINA_HOME=(Resolve-Path .).Path
$env:CATALINA_BASE=$env:CATALINA_HOME
.\bin\startup.bat
```

## 7. Run Tests

```bash
bash tests/run_tests.sh http://localhost:8081/FishFinderNorCal
```

Requires `curl` and `mysql` CLI. Runs 32 integration tests.
