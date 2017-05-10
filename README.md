# What is the Cookbook?
The cookbook project are a set of recipes to help prescribe best practices for using MarkLogic.

# What do I need to run the recipes?

## Required Software

 * MarkLogic 9+
 * Java Development Kit 1.8+
 * (Optional) Gradle 3+

## Set up  

If you have a brand new install of MarkLogic and have not initialized MarkLogic and added an admin user, then execute the following steps otherwise skip to step #3.  

1) Review the host, username, and password properties found in gradle.properties 

2) Run the following gradle commands
    
    gradlew mlInit
    gradlew mlInstallAdmin

3) Install the MarkLogic database and application server

     ./gradlew mlDeploy  

4) Run the unit tests

    ./gradlew test
 
5) Load a sample RDBMS

    ./gradlew loadH2Data
 
6) Create RDBMS to MarkLogic batch program

    ./gradlew installDist
 
7) Execute program

    build\install\cookbook\bin\cookbook.bat com.marklogic.cookbook.SpringBatchConfig importCustomersJob
 
