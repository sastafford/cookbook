# ml-gradle: Build and Deploy

The MarkLogic Gradle Plugin (ml-gradle) helps developers build, automate, and deliver better software faster.  Starting with the second layer in Figure 2, the Management API is a REST-based API that allows you to administer MarkLogic Server and access MarkLogic Server instrumentation with no provisioning or set-up. You can use the API to perform administrative tasks such as initializing or extending a cluster; creating databases, forests, and App Servers; and managing tiered storage partitions. The API also provides the ability to capture detailed information on MarkLogic Server objects and processes.  Java developers can interact with the Management API using a Java library called ml-app-deployer.  This layer can enable a configuration coordinator to read/write files from a filesystem and push those configuration resources into MarkLogic and setup an environment.  Gradle is an off the shelf developer power tool that enables developers to run tasks (programs) and execute an action.  Plugins expand the types of tasks that to be executed.  The gradle plugin that allows you to run tasks against the Management REST API is the ml-gradle plugin.  
 
##MarkLogic Version
MarkLogic 8.0-4+

## References
 * [ml-gradle](https://github.com/marklogic-community/ml-gradle)
 * [Gradle](https://gradle.org/)
 * (OPTIONAL) [ml-app-deployer](https://github.com/marklogic-community/ml-app-deployer)

## Create a new project
1)	Read the [getting started guide](https://github.com/marklogic-community/ml-gradle/wiki/Getting-started)
2)	View [sample project](https://github.com/marklogic-community/ml-gradle/tree/master/examples/sample-project)


## Deploy Application on Brand New MarkLogic Install
For a given new installation of MarkLogic, to initialize the MarkLogic installation, create an admin user, install an application, and load data (custom task).  

    gradle mlInit mlInstallAdmin mlDeploy importData
