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
The information about the DB (url, username, password) need to be included inside `conf/application.conf`. 

#### Tables

**users**: 
  - Contains all information about a specific user. 
  - Passwords are stored as a SHA-256 hash.
  - isSuper is a boolean indicating whether the user is allowed to see and run all configurations i.e., the admin user. 
  - teamName is a foreign key referencing "name" of the "teams" table. 

CREATE TABLE users (
  username varchar(50) NOT NULL DEFAULT '',
  password char(64) DEFAULT NULL,
  teamName varchar(50) DEFAULT NULL,
  isSuper tinyint(1) DEFAULT NULL,
  PRIMARY KEY (username),
  UNIQUE KEY username (username),
  KEY teamName (teamName)
);

**teams**:  
  - Contains names and passwords of a team. 

CREATE TABLE teams (
  name varchar(50) NOT NULL DEFAULT '',
  password char(64) DEFAULT NULL,
  PRIMARY KEY (name),
  UNIQUE KEY name (name)
);

**datasets**: 
  - Specifies which datasets are available for which tasks. The acutal data (text annotations) are in the textannotations table.
  - task is a foreign key referencing "name" of the "tasks" table. 

CREATE TABLE datasets (
  name varchar(50) NOT NULL DEFAULT '',
  task varchar(50) NOT NULL,
  KEY fk_task (task)
);

**textAnnotations**: 
  - Contains all the text annotations in the database along with the corresponding dataset they belong to.
  - dataset_name is a foreign key referencing "name" of the "datasets" table.  

CREATE TABLE textannotations (
  textAnnotation mediumtext,
  dataset_name varchar(50) DEFAULT NULL,
  KEY dataset_name (dataset_name)
);

**configurations**: 
  - Contains all information regarding a configuration.
  - The evaluator field was used before, but now it is obsolete and the evaluator is specified in the "taskMappings" table. 
  - "teamName" is an old field, but now is treated as the name of the whole configuration. 
  - team_name is a foreign key referencing "name" of the "teams" table. 

CREATE TABLE configurations (
  id int(11) NOT NULL AUTO_INCREMENT,
  datasetName varchar(20) DEFAULT NULL,
  teamName varchar(50) DEFAULT NULL,
  description varchar(150) DEFAULT NULL,
  evaluator varchar(50) DEFAULT NULL,
  taskType varchar(50) DEFAULT NULL,
  taskVariant varchar(100) DEFAULT NULL,
  lastUpdated timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  team_name varchar(50) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY teamName (teamName),
  KEY fk_team_name (team_name)
);   

**tasks**: 
  - Lists all the tasks in the database as well as provides the mapping from (task, task-variant) -> evaluatorView

CREATE TABLE tasks (
  name varchar(50) NOT NULL,
  evaluatorView varchar(50) DEFAULT NULL,
  task_variant varchar(100) DEFAULT NULL
);

**taskvariants**: 
  - Specifies for which task, all the available task-variants. 
  - task_name is a foreign key referencing "name" of the "tasks" table. 

CREATE TABLE taskvariants (
  name varchar(100) DEFAULT NULL,
  task_name varchar(50) DEFAULT NULL,
  KEY task_name (task_name)
);

**taskMappings**:
  - Lists the mappings of (task, task-variant) -> evaluator. 
  - task_name is a foreign key referencing "name" of the "tasks" table.

CREATE TABLE taskMappings (
  name varchar(50) DEFAULT NULL,
  task_name varchar(50) DEFAULT NULL,
  task_variant varchar(100) DEFAULT NULL,
  KEY task_name (task_name)
);

**records**: 
  - Contains information about a record (a specific run of a configuration).
  - configuration_id is a foreign key referencing "id" of the "configurations" table. 

CREATE TABLE records (
  configuration_id int(11) NOT NULL,
  record_id int(11) NOT NULL AUTO_INCREMENT,
  url varchar(250) DEFAULT NULL,
  date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  comment varchar(150) DEFAULT NULL,
  repo varchar(250) DEFAULT NULL,
  author varchar(20) DEFAULT NULL,
  recall double DEFAULT NULL,
  f1 double DEFAULT NULL,
  gold_count int(11) DEFAULT NULL,
  correct_count int(11) DEFAULT NULL,
  predicted_count int(11) DEFAULT NULL,
  missed_count int(11) DEFAULT NULL,
  extra_count int(11) DEFAULT NULL,
  precision_score double DEFAULT NULL,
  isRunning tinyint(1) DEFAULT NULL,
  avgSolveTime double DEFAULT NULL,
  PRIMARY KEY (record_id),
  KEY configuration_id (configuration_id)
);


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
 - First, add the name of dataset and its corresponding task into the database using the [method] (https://github.com/dshine2/open-eval-1/blob/master/core/app/controllers/readers/Reader.java) `Reader.insertIntoDatasets(String corpusName, String task)`. If the dataset is associated with multiple tasks, then call the above function for each of those tasks.
 - Any dataset can be added as long as it can be transformed into a series of Text Annotations, so use a reader to output a `List<TextAnnotation>`.
 - Then, insert the Text Annotations into the database using [method](https://github.com/IllinoisCogComp/illinois-cogcomp-nlp/blob/master/core-utilities/src/main/java/edu/illinois/cs/cogcomp/core/utilities/SerializationHelper.java)`Reader.storeTextAnnotations(String corpusName, List<TextAnnotation> textAnnotations)` with the same corpusName you used in step 1. 


