# About

The `core` module of Open-Eval, when run, hosts an instance of the webapp.  It is responsible for responding to incoming http requests, communicating  with the database, and interfacing with the `learner` module.

# Database

To deploy this application, you'll need to have an SQL database set up.  We've provided an SQL dump file with the empty tables you'll need to get started.

# Overview of Components

 - `Application.java`: Responds to http GET requests.
 - `Core.java`: Main class consisting of static methods connecting all back-end modules.
 - `FrontEndDbInterface.java`: Performs database communication.
 - `Redactor.java`: Removes views from  `TextAnnotation` objects so they can be sent to the solver.
 - `controllers.evaluators`: The actual evaluation is done using illinois-cogcomp's main NLP library, which can be found here: [illinois-core-utilities](https://github.com/cogcomp-dev/illinois-cogcomp-nlp/blob/master/core-utilities/README.md).
 - `controllers.readers`: Generates `TextAnnotation` objects for particular datasets.
 - `LearnerInterface.java`: Interfaces with the solver.