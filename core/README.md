# About

The `core` module of Open-Eval, when run, hosts an instance of the webapp.  It is responsible for responding to incoming http requests, communicating  with the database, and interfacing with the `learner` module.

# Database

To deploy this application, you'll need to have an SQL database set up.  We've provided an SQL dump file (TODO: this) with the empty tables you'll need to get started.  In the core.properties file, you'll need to fill out the database URL, username, and password.

# Overview of Components

 - [`Application.java`](https://github.com/IllinoisCogComp/open-eval/blob/master/core/app/controllers/Application.java): Responds to http GET requests.
 - [`Core.java`](https://github.com/IllinoisCogComp/open-eval/blob/master/core/app/controllers/Core.java): Main class consisting of static methods connecting all back-end modules.
 - [`FrontEndDbInterface.java`](https://github.com/IllinoisCogComp/open-eval/blob/master/core/app/controllers/FrontEndDBInterface.java): Performs database communication.
 - [`Redactor.java`](https://github.com/IllinoisCogComp/open-eval/blob/master/core/app/controllers/Redactor.java): Removes views from  `TextAnnotation` objects so they can be sent to the solver.
 - [`controllers.evaluators`](https://github.com/IllinoisCogComp/open-eval/tree/master/core/app/controllers/evaluators): The actual evaluation is done using illinois-cogcomp's main NLP library, which can be found here: [illinois-core-utilities](https://github.com/cogcomp-dev/illinois-cogcomp-nlp/blob/master/core-utilities/README.md).
 - [`controllers.readers`](https://github.com/IllinoisCogComp/open-eval/tree/master/core/app/controllers/readers): Generates `TextAnnotation` objects for particular datasets.
 - [`LearnerInterface.java`](https://github.com/IllinoisCogComp/open-eval/blob/master/core/app/models/LearnerInterface.java): Interfaces with the solver.

# Multi-threaded Architecture

 The application is integrated with [Akka](http://akka.io/), a toolkit for building concurrent applications.  Akka adopts the [Actor Model](https://en.wikipedia.org/wiki/Actor_model) as its theoretical foundation.  Below is a brief description of how Akka is used in this application.

 - `MasterActor`: The `MasterActor` is the link between http requests and the actual computation.  Its responsibilities are to respond to requests by spawning other actors to carry out intensive computations (communicating with the database, redacting views from `TextAnnotation`s, interfacing with the solver, and evaluating and storing the results) as well as to keep track of the progress of its child actors.
 - `JobProcessingActor`: The `JobProcessingActor` sends and receives `TextAnnotation`s to and from the solver.

# How to add your own evaluator

 - Extend illinois-cogcomp's `Evaluator` abstract class.
 - In the [method](https://github.com/IllinoisCogComp/open-eval/blob/master/core/app/controllers/Core.java#L183) `Core.getEvaluator(Configuration runConfig)`, add your evaluator name to the switch statement.
 - Add *the same* evaluator name to the database

 # How to add your own datasets

**Placeholder**
 - put em in the database or somethin idk figure it out