# About

The learner endpoint is built to be used alongside the [Open Eval](https://github.com/IllinoisCogComp/open-eval) system. This system is meant to test your learner, and will send instances to you for you to label and send back. To ease this process we have built the learner endpoint, which will take care of most of these communications for you. 

# How to Use

## Adding the Learner Endpoint to your project
*Maven Resolver*
*Maven Dependency*

## The `Server` class

The `Server` class will be taking care of the communications between your learner and the Open Eval system. When creating the Server you need to specify the port that it will listen on, and the Annotator that represents you learner.

## The `Annotator` class

The `Annotator` class must be extended in order for the `Server` to communicate with your learner. In super constructor you must specify the name of the view that your learner will be adding, and also any names of views that your learner requires.

The `Annotator` will receive instances by the `addView` method. You will receive a `TextAnnotation` object, which you must add the type of view you specified in the constructor.
