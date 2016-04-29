# OpenEval 

[![Build Status](https://semaphoreci.com/api/v1/projects/4f27c2b5-9ce3-4fca-95a1-064b60600f44/589884/badge.svg)](https://semaphoreci.com/danyaljj/open-eval)
[![Build Status](https://travis-ci.org/IllinoisCogComp/open-eval.svg?branch=master)](https://travis-ci.org/IllinoisCogComp/open-eval)

- Project's [slack channel](https://cogcomp.slack.com/messages/open-eval/).

## About 
Data scientists working on machine learning problems have historically had several issues
relating to evaluating their systems: spending time individually developing
evaluation frameworks for tasks, comparing results over time, and keeping evaluations
consistent among teams. OpenEval is a system designed to address these problems.

In developing this system we set out to build a centralized, easy-to-use platform for groups to
evaluate their models. All the user needs to do to evaluate their solver is host it on a thin server,
which we provide. Then, on the web interface, they need to select their desired task and dataset
to test their solver. After their solver finishes processing the dataset, the user can view
the results.

## Modules  
The project contains two main modules. 

 - The OpenEval core, which contains the main functionalities and the web app. To read more on details of core, visit [here](core/). 
 - A Learner, which acts as a toy system to be evaluated against core, visit [here](learner/). 

## Quick Guide on Running the Apps
Your will need Java 8 in order to run App. openjdk on Ubuntu seems to have issues.

You will also need sbt

First, run `sbt` from the parent directory. 

- `projects` will show the names of the existing module names. 
    - `project core` will take you inside the core package.  
    -  `project learner` will take you inside the examples package.
- Inside each project you can `compile` it, or `run` it. 

If you `run` the core you can browse to `localhost:9000`. To run on specific port simply add the port number after `run`. 
You also should not have to start it multiple times. You can just save code, and refresh the page.
