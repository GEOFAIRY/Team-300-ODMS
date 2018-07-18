package seng302.Generic;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import java.io.StringReader;

public class APIResponse{
    private JsonParser jp;
    private String body;
    private int status;
    public APIResponse(Response response){
        jp = new JsonParser();
        this.body = response.readEntity(String.class);
        this.status = response.getStatus();
    }

    public APIResponse(int status) {
        this.body = "";
        this.status = status;
    }

    public boolean isValidJson(){
        try{
            new JSONObject(body);
        } catch (JSONException oe){
            try {
                new JSONArray(body);
            } catch (JSONException ae){
                return false;
            }
        }
        return true;
    }

    public JsonObject getAsJsonObject(){
        return jp.parse(body).getAsJsonObject();
    }

    public JsonArray getAsJsonArray(){
        return jp.parse(body).getAsJsonArray();
    }

    public String getAsString(){
        return body;
    }

    public int getStatusCode() {
        return status;
    }
}