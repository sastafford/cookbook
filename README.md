# Instructions

The instructions assumes MarkLogic has been installed.  The following steps will initialize MarkLogic with the configuration found in gradle.properties
    
    gradlew mlInit
    gradlew mlInstallAdmin

Install the MarkLogic database and application server

     ./gradlew mlDeploy
     

Run the unit tests

    ./gradlew test