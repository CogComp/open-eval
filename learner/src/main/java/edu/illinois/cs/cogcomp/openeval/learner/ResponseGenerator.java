package edu.illinois.cs.cogcomp.openeval.learner;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by rnkelch on 12/19/2015.
 */
public class ResponseGenerator
{
    private static String BAD_METHOD_MESSAGE = "Error, method not allowed";

    public static NanoHTTPD.Response generateMethodNotAllowedResponse(){
        NanoHTTPD.Response notFound = NanoHTTPD.newFixedLengthResponse(BAD_METHOD_MESSAGE);
        notFound.setStatus(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED);
        return notFound;
    }

    public static NanoHTTPD.Response generateErrorResponse(Exception e, String msg)
    {
        e.printStackTrace(System.err);
        NanoHTTPD.Response errorResponse = generateResponse(msg, NanoHTTPD.Response.Status.INTERNAL_ERROR);
        return errorResponse;
    }

    public static NanoHTTPD.Response generateResponse(String body, NanoHTTPD.Response.Status status){
        NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(status,"text/html",body);
        return response;
    }
}
