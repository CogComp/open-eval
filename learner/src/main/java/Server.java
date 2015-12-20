import edu.illinois.cs.cogcomp.annotation.Annotator;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Server extends RouterNanoHTTPD
{
    private static String INFO_ROUTE = "info";
    private static String INSTANCE_ROUTE = "instance";

    private static String BAD_METHOD_MESSAGE = "Error, method not allowed";

    private Annotator annotator;
    private InfoController infoController;
    private InstanceController instanceController;

    public Server(int port, Annotator annotator) throws IOException {
        super(port);
        this.annotator = annotator;
        addMappings();
    }

    @Override
    public void addMappings(){
        super.addMappings();
        addRoute("/instance",InstanceController.class,annotator);
        addRoute("/info",InfoController.class,annotator);
    }

//    @Override
//    public Response serve(IHTTPSession session){
//        Map<String, String> files = new HashMap<String, String>();
//        Method httpMethod = session.getMethod();
//
//        String uri = session.getUri().toLowerCase();
//        if (uri.charAt(uri.length()-1) == '/') {
//            uri = uri.substring(1,uri.length()-1);
//        }
//        else {
//            uri = uri.substring(1);
//        }
//
//        if (uri.equals(INFO_ROUTE)) {
//            if (httpMethod.equals(Method.GET)){
//                String responseBody = infoController.getInfo(annotator);
//                return new Response(responseBody);
//            }
//            else {
//                return generateResponse(BAD_METHOD_MESSAGE, Response.Status.METHOD_NOT_ALLOWED);
//            }
//        }
//        else if (uri.equals(INSTANCE_ROUTE)){
//            if (httpMethod.equals(Method.POST)){
//                String json;
//                try {
//                    session.parseBody(files);
//                } catch (IOException ex) {
//                    Response errorResponse = generateErrorResponse(ex, "Error reading request");
//                    return errorResponse;
//                } catch (ResponseException e)
//                {
//                    Response errorResponse = generateErrorResponse(e,"Error reading request");
//                    return errorResponse;
//                }
//                json = files.get("postData");
//                ResultStruct result = instanceController.getInstance(annotator,json);
//                if (result.error){
//                    return generateResponse(result.errorText, Response.Status.INTERNAL_ERROR);
//                }
//                else {
//                    return new Response(result.result);
//                }
//            }
//            else {
//                return generateResponse(BAD_METHOD_MESSAGE, Response.Status.METHOD_NOT_ALLOWED);
//            }
//        }
//        else {
//            return generateResponse("Not found", Response.Status.NOT_FOUND);
//        }
//    }
//
    public static Response generateMethodNotAllowedResponse(){
        Response notFound = NanoHTTPD.newFixedLengthResponse("Not found");
        notFound.setStatus(Response.Status.METHOD_NOT_ALLOWED);
        return notFound;
    }
//
//    private Response generateResponse(String body, Response.Status status){
//        Response response = new Response(body);
//        response.setStatus(status);
//        return response;
//    }
//
//    private Response generateErrorResponse(Exception e, String msg)
//    {
//        e.printStackTrace(System.err);
//        Response errorResponse = new Response(msg);
//        errorResponse.setStatus(Response.Status.INTERNAL_ERROR);
//        return errorResponse;
//    }
}
