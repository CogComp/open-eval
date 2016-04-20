# OpenEval Core 

The `core` module of OpenEval, when run, hosts an instance of the webapp. It is responsible for 
responding to incoming http requests, communicating with the database, and interfacing with the 
`learner` module.

## Basic definitions and components 

Before we begin a discussion of the actual system, we must define a few terms.

  - A **task** is a specific AI problem such as Part of Speech Tagging or Named-Entity Recognition.
  - A **task-variant** is a modification to the task. For example, part-of-speech tagging can be done
on tokenized sentences or on raw text.
  - A **solver** is a piece of software developed to solve a target task. For example, for the task of
Part of Speech Tagging, the solver would receive sentences as input, and assign a part of speech
to each word.
  - A **dataset** is set of (input, output) pairs. In the part-of-speech tagging example, the input would
be a sentence, and the output would be an ordered list of part-of-speech tags.
  - A **configuration** is what the user runs. It encapsulates a task, a task-variant, a dataset.
  - A **run** is specific instance of running the configuration. Users can run their configuration any
number of items to see how their solver improves over time.

Our project backend is implemented as a set of modules that pass around and process the instances
of the dataset. These modules include: 

  - **Database Interface:** Allows the backend to store and retrieve configurations, evaluation records and datasets in the MySQL database.
  - **Redactors:** Processes the dataset and removes the values to be predicted.
  - **Learner Endpoint:** A thin server provided to the client. This is run independently to allow our system to send test instances to the solver and retrieve solved instances back.
  - **Learner Interface:** Sends test instances to the Learner Endpoint and retrieves predictions.
  - **Evaluators:** Creates an evaluation (results of accuracy and efficiency) given the solver’s predictions and the original dataset.
  - **Core:** Connects all other modules by passing the data between them
  
## Technologies used 
  - The actual datasets are stored and passed around as lists of `TextAnnotation` objects. A `TextAnnotation`
is a central data structure created by the IllinoisCogComp team that serves as a container for 
solved or predicted values of an NLP problem.
  - Play is the web application framework we used, which allowed for fast, and easy development.
  - The primary back-end language used is Java.
  - The web Interface is written in Javascript, JQuery, HTML, and the Twirl Template Engine.
  - Database management service used is MySQL.

![System components' diagram](diagram.png)

## Database

To deploy this application, you'll need to have an SQL database set up.  We've provided an SQL script (TODO: this) that will set up the empty tables you'll need to get started.  In the `core.properties` file, you'll need to fill out the database URL, username, and password.

## Overview of Components

 - [`Application.java`](https://github.com/IllinoisCogComp/open-eval/blob/master/core/app/controllers/Application.java): Responds to http GET requests.
 - [`Core.java`](https://github.com/IllinoisCogComp/open-eval/blob/master/core/app/controllers/Core.java): Main class consisting of static methods connecting all back-end modules.
 - [`FrontEndDbInterface.java`](https://github.com/IllinoisCogComp/open-eval/blob/master/core/app/controllers/FrontEndDBInterface.java): Performs database communication.
 - [`Redactor.java`](https://github.com/IllinoisCogComp/open-eval/blob/master/core/app/controllers/Redactor.java): Removes views from  `TextAnnotation` objects so they can be sent to the solver.
 - [`controllers.evaluators`](https://github.com/IllinoisCogComp/open-eval/tree/master/core/app/controllers/evaluators): The actual evaluation is done using illinois-cogcomp's main NLP library, which can be found here: [illinois-core-utilities](https://github.com/cogcomp-dev/illinois-cogcomp-nlp/blob/master/core-utilities/README.md).
 - [`controllers.readers`](https://github.com/IllinoisCogComp/open-eval/tree/master/core/app/controllers/readers): Generates `TextAnnotation` objects for particular datasets.
 - [`LearnerInterface.java`](https://github.com/IllinoisCogComp/open-eval/blob/master/core/app/models/LearnerInterface.java): Interfaces with the solver.

## Multi-threaded Architecture

 The application is integrated with [Akka](http://akka.io/), a toolkit for building concurrent applications.  Akka adopts the [Actor Model](https://en.wikipedia.org/wiki/Actor_model) as its theoretical foundation.  Below is a brief description of how Akka is used in this application.

 - `MasterActor`: The `MasterActor` is the link between http requests and the actual computation.  Its responsibilities are to respond to requests by spawning other actors to carry out intensive computations (communicating with the database, redacting views from `TextAnnotation`s, interfacing with the solver, and evaluating and storing the results) as well as to keep track of the progress of its child actors.
 - `JobProcessingActor`: The `JobProcessingActor` sends and receives `TextAnnotation`s to and from the solver.

## How to add your own evaluator

 - Extend illinois-cogcomp's `Evaluator` abstract class.
 - In the [method](https://github.com/IllinoisCogComp/open-eval/blob/master/core/app/controllers/Core.java#L183) `Core.getEvaluator(Configuration runConfig)`, add your evaluator name to the switch statement.
 - Add *the same* evaluator name to the database

## How to add your own datasets

[TODO]