# About

The learner endpoint is built to be used alongside the [Open-Eval](https://github.com/IllinoisCogComp/open-eval) system. The Open 
Eval system will test your learner and record previous runs in a central place. In order to do this it will send instances your learner label and send back. To ease this process we have built the learner endpoint, which will take care of most of these communications for you. 

# How to Use

## Adding the Learner Endpoint to your project

Here is how you can add the learner endpoint through sbt:
 - Add the following resolver: `"CogcompSoftware" at "http://cogcomp.cs.illinois.edu/m2repo/"`
 - Add the following dependency: `"edu.illinois.cs.cogcomp" % "openeval-learner" % "version"`

Here is how you can add the learner endpoint through maven:

```xml
    <repositories>
		<repository>
			<id>CogcompSoftware</id>
			<name>CogcompSoftware</name>
			<url>http://cogcomp.cs.illinois.edu/m2repo/</url>
		</repository>
	</repositories>

	<dependencies>
		<dependency>
			<groupId>edu.illinois.cs.cogcomp</groupId>
			<artifactId>openeval-learner</artifactId>
			<version>"version"</version>
		</dependency>
	</dependencies>
```


## The `Server` class

The `Server` class will be taking care of the communications between your learner and the Open-Eval system. When creating the Server you need to specify the port that it will listen on, and the Annotator that represents you learner.

## The `Annotator` class

The `Annotator` class must be extended in order for the `Server` to communicate with your learner. In super constructor you must specify the name of the view that your learner will be adding, and also any names of views that your learner requires. For example, if you are doing POS tagging, you will be adding the POS view. You may depend on receiving a token view. You would specify this, and any other view your require in the second parameter or the super constructor. Not all views will be provided, and if not available, the Open-Eval system will notify you.

The `Annotator` will receive instances by the `addView` method. You will receive a `TextAnnotation` object, which you must add the type of view you specified in the constructor.

## Example

Below is an example POS annotator that just assigns randoms labels to each token. It also starts the server.

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
    
        public static void main(String args[]) throws IOException {
    
            // Do any training
            Annotator annotator = new ToyPosAnnotator();
    
            // We will have our server listen on port 5757 and pass it our trained annotator
            Server server = new Server(5757, annotator);
    
            // We have no more work to do, so we will use the executeInstance method to start and keep our Server alive
            fi.iki.elonen.util.ServerRunner.executeInstance(server);
        }
    }
```

## Under the Hood

Here is a general overview of what the learner endpoint is doing to communicate with the core. *It is not necessary to know this to test your learner.*

After starting the Server and running the configuration the Open-Eval system submit a GET request to your server at `/info` which provides the following information as JSON:

 - The required views of your learner
 - The view your learner will be adding

After this the Open-Eval system will start posting instances as JSON to `/instance`.  The learner endpoint will deserialize them and run them through the provided Annotator. Once the view is added the endpoint will serialize the whole `TextAnnotation` and send it back to the Open-Eval system. This process repeats until all the instances have been tested.
