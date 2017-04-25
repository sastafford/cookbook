# Instructions

The instructions assumes MarkLogic has been installed.  The following steps will initialize MarkLogic with the configuration found in gradle.properties
    
    gradlew mlInit
    gradlew mlInstallAdmin

Install the MarkLogic database and application server

     ./gradlew mlDeploy
     

Run the unit tests

    ./gradlew test
    
Load a sample RDBMS

    ./gradlew loadH2Data
    
Create RDBMS to MarkLogic batch program

    ./gradlew installDist
    
Execute program

    build\install\cookbook\bin\cookbook.bat com.marklogic.cookbook.SpringBatchConfig importCustomersJob
    
