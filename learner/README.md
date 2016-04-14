# About

The learner endpoint is built to be used alongside the [Open-Eval](https://github.com/IllinoisCogComp/open-eval) system. The Open-Eval system will test your learner and record previous runs in a central place. In order to do this it will send instances to your learner to label and send back. To ease this process we have built the learner endpoint, which will take care of most of these communications for you.

# Background

## About `TextAnnotation`'s and `View`'s

A `TextAnnotation` can be thought of as a container that stores different layers of annotations over some text. It contains two main parts. It the text of the document. It also contains `View`s which represent the annotations of the text.

The library stores all information about a specific annotation over text in an object called `View`.  A `View` is a graph, where the nodes are `Constituents` and the edges are `Relations`. In its most general sense, a `View` is graph whose nodes are labeled spans of text. The edges between the nodes represent the relationships between them. A `TextAnnotation` can be thought of as a container of views, indexed by their names.

For more information and examples check the [illinois-core-utilities documentation](https://github.com/cogcomp-dev/illinois-cogcomp-nlp/blob/master/core-utilities/README.md).

## The `Annotator` class

The `Annotator` class must be extended in order for the `Server` to communicate with your learner. In super constructor you must specify the name of the `View` that your learner will be adding, and also any names of `View`'s that your learner requires. For example, if you are doing POS tagging, you will be adding the POS `View`. You may depend on receiving a token `View`. You would specify this, and any other `View` your require in the second parameter or the super constructor. Not all views will be provided, and if not available, the Open-Eval system will notify you.

The `Annotator` will receive instances by the `addView` method. You will receive a `TextAnnotation` object, which you must add the type of view you specified in the constructor.

## Example

Below is an example POS annotator that just assigns randoms labels to each token.

```java
import edu.illinois.cs.cogcomp.annotation.Annotator;
import edu.illinois.cs.cogcomp.annotation.AnnotatorException;
import edu.illinois.cs.cogcomp.core.datastructures.ViewNames;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.Constituent;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.TextAnnotation;
import edu.illinois.cs.cogcomp.core.datastructures.textannotation.View;
import edu.illinois.cs.cogcomp.nlp.utilities.POSUtils;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class ToyPosAnnotator extends Annotator
{
    public ToyPosAnnotator()
    {
        // The problem we are trying to solve is parts of speech (POS)
        // The view we require is TOKENS
        super(ViewNames.POS, new String[] {ViewNames.TOKENS});
    }

    @Override
    public void addView(TextAnnotation textAnnotation) throws AnnotatorException
    {
        String[] tokens = textAnnotation.getTokens();
        List<String> tags = POSUtils.allPOS;

        // Create a new view with our view name (the other fields are unimportant for this example)
        View posView = new View(ViewNames.POS,"POS-annotator",textAnnotation,1.0);
        textAnnotation.addView(ViewNames.POS,posView);

        Random random = new Random();

        for(int i=0;i<tokens.length;i++){
            // For this example we will just randomly assigning tags.
            int randomTagIndex = random.nextInt(tags.size());
            // Add the tag to the view for the specified token
            posView.addConstituent(new Constituent(tags.get(randomTagIndex),ViewNames.POS,textAnnotation,i,i+1));
        }
    }
}
```

# How to Use

## Adding the Learner Endpoint to your project

Here is how you can add the learner endpoint through sbt:
 - Add the following resolver: `"CogcompSoftware" at "http://cogcomp.cs.illinois.edu/m2repo/"`
 - Add the following dependency: `"edu.illinois.cs.cogcomp" % "openeval-client_2.11" % "0.1.1"`

Here is how you can add the learner endpoint through Maven:

Add the following repository to your pom.xml

```xml
<repository>
	<id>CogcompSoftware</id>
	<name>CogcompSoftware</name>
	<url>http://cogcomp.cs.illinois.edu/m2repo/</url>
</repository>
```
Add the following dependency to your pom.xml
```xml
<dependency>
	<groupId>edu.illinois.cs.cogcomp</groupId>
	<artifactId>openeval-client_2.11</artifactId>
	<version>0.1.1</version>
</dependency>
```

## The `Server` class

The `Server` class will be taking care of the communications between your learner and the Open-Eval system. When creating the Server you need to specify the port that it will listen on, and the `Annotator` that represents you learner.

There are two ways to run the server:
 - Use the `Server.start` instance method: This is a non-blocking call. It will start the `Server` and continue to execute code. However, once the program is finished the `Server` will die. Use `Server.stop` to stop the `Server`
 - __(recomended)__ Use the `fi.iki.elonen.util.ServerRunner.executeInstance` static method: This is a blocking call. It will prevent the program from exiting and therefore keep the `Server` alive. Press `control+D` in the terminal to stop the `Server`.

You can test to see if your `Server` is running by browsing to `localhost:<port>/info`. If you get JSON describing your required views the `Server` is working properly. The URL the Open-Eval system needs will either be `<computer_name>:<port>` or `<computer_ip>:port` if your machine and the Open-Eval system are on the same network. If you are outside the network of the Open-Eval system you will either need to use VPN, a domain name, or set up port forwarding.

### Example

```java
public static void main(String args[]) throws IOException {
    // Create the annotator
    Annotator annotator = new ToyPosAnnotator();
    
    // We will have our server listen on port 5757 and pass it our toy annotator
    Server server = new Server(5757, annotator);

    // We have no more work to do, so we will use the executeInstance method to start and keep our Server alive
    fi.iki.elonen.util.ServerRunner.executeInstance(server);
}
```

## Running with the open eval system

__You will need to be on the same network as the system, or VPNed in__. When you run the server it will print out a url. Copy this url and browse to the open eval site. Choose a *Configuration* or create one if you haven't yet. Click the button at the top, "New run of configuration". Paste the url in the solver address area.

## Under the Hood

Here is a general overview of what the learner endpoint is doing to communicate with the core. *It is not necessary to know this to test your learner.*

After starting the Server and running the configuration the Open-Eval system submit a GET request to your server at `/info` which provides the following information as JSON:

 - The required views of your learner
 - The view your learner will be adding

After this the Open-Eval system will start posting instances as JSON to `/instance`.  The learner endpoint will deserialize them and run them through the provided Annotator. Once the view is added the endpoint will serialize the whole `TextAnnotation` and send it back to the Open-Eval system. This process repeats until all the instances have been tested.
