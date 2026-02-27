package standalone;

import org.apache.hc.client5.http.fluent.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ResourceBundle;

public class Tasca_5 {

    public static void main(String[] args) {

        String fibAswId = "109862447110628983";
        String TOKEN = ResourceBundle.getBundle("token").getString("token");
        String statusesURI = "https://mastodont.cat/api/v1/accounts/" + fibAswId + "/statuses?limit=1";

        try {
            String statusesOutput = Request.get(statusesURI)
                    .addHeader("Authorization", "Bearer " + TOKEN)
                    .execute()
                    .returnContent()
                    .asString();

            JSONArray statuses = new JSONArray(statusesOutput);
            JSONObject lastStatus = statuses.getJSONObject(0);

            String statusId = lastStatus.getString("id");
            String contingut  = lastStatus.getString("content");

            System.out.format("\n ultim tut:\n id: %s\n contingut: %s\n", statusId, contingut);


            String boostURI = "https://mastodont.cat/api/v1/statuses/" + statusId + "/reblog";

            String boostOutput = Request.post(boostURI)
                    .addHeader("Authorization", "Bearer " + TOKEN)
                    .execute()
                    .returnContent()
                    .asString();

            JSONObject boostResult = new JSONObject(boostOutput);
            String boostUrl = boostResult.getString("url");

            System.out.format("\n boost fet!");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}