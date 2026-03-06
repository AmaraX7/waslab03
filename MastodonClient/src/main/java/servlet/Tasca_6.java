package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.client5.http.fluent.Request;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

@WebServlet(value = "/")
public class Tasca_6 extends HttpServlet {

    private static final String BASE_URL = "https://mastodont.cat";
    private static final String FIB_ASW_ID = "109862447110628983";

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String TOKEN = ResourceBundle.getBundle("token").getString("token");

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy 'a les' HH:mm:ss", Locale.forLanguageTag("ca"));
        String now = sdf.format(new Date());

        out.println("<!DOCTYPE html>");
        out.println("<html lang='ca'>");
        out.println("<head>");
        out.println("  <meta charset='UTF-8'>");
        out.println("  <title>Darrers tuts dels comptes seguits</title>");
        out.println("  <link href='waslab03-t6.css' rel='stylesheet' type='text/css'/>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class='header'>");
        out.println("  <h1>Els cinc tuts més recents del comptes seguits per l'usuari 'fib_asw'</h1>");
        out.println("  <p>" + now + "</p>");
        out.println("</div>");

        try {
            // obtengo los accounts que sigue fib_asw
            String followingUrl = BASE_URL + "/api/v1/accounts/" + FIB_ASW_ID + "/following?limit=80";
            String followingOutput = Request.get(followingUrl)
                    .addHeader("Authorization", "Bearer " + TOKEN)
                    .execute()
                    .returnContent()
                    .asString();

            JSONArray following = new JSONArray(followingOutput);

            // para cada account
            for (int i = 0; i < following.length(); i++) {
                JSONObject account = following.getJSONObject(i);

                String accountId     = account.getString("id");
                String displayName   = account.getString("display_name");
                String acct          = account.getString("acct");
                String avatar        = account.getString("avatar");
                int followersCount   = account.getInt("followers_count");

                // Si acct contiene @ es de otro servidor, si no es de mastodont.cat
                String fullUsername = "@" + acct;

                out.println("<div class='account'>");
                out.println("  <h2>");
                out.println("    <img src='" + avatar + "' alt='Avatar' class='account-avatar'>");
                out.println("    " + displayName + " (" + fullUsername + ")");
                out.println("  </h2>");
                out.println("  <p>Nombre de seguidors: " + followersCount + "</p>");
                out.println("  <div class='tuts'>");

                // obtener SOLO últimos 5 tuts
                String statusesUrl = BASE_URL + "/api/v1/accounts/" + accountId + "/statuses?limit=5";
                String statusesOutput = Request.get(statusesUrl)
                        .addHeader("Authorization", "Bearer " + TOKEN)
                        .execute()
                        .returnContent()
                        .asString();

                JSONArray statuses = new JSONArray(statusesOutput);

                for (int j = 0; j < statuses.length(); j++) {
                    JSONObject status = statuses.getJSONObject(j);
                    boolean isReblog = !status.isNull("reblog");

                    if (isReblog) {
                        JSONObject originalStatus  = status.getJSONObject("reblog");
                        JSONObject originalAccount = originalStatus.getJSONObject("account");
                        String originalDisplayName = originalAccount.getString("display_name");
                        String originalAcct        = originalAccount.getString("acct");
                        String originalFullAcct    = "@" + originalAcct;
                        String reblogContent       = originalStatus.getString("content");
                        String reblogTime          = formatTimestamp(status.getString("created_at"));

                        out.println("    <div class='tut reblog'>");
                        out.println("      <p class='timestamp'>🔁 Retut - " + reblogTime +
                                " <span class='original-author'>(Original: " + originalDisplayName +
                                " (" + originalFullAcct + "))</span></p>");
                        out.println("      <div class='content'>" + reblogContent + "</div>");
                        out.println("    </div>");

                    } else {
                        String content   = status.getString("content");
                        String timestamp = formatTimestamp(status.getString("created_at"));

                        out.println("    <div class='tut'>");
                        out.println("      <p class='timestamp'>" + timestamp + "</p>");
                        out.println("      <div class='content'>" + content + "</div>");
                        out.println("    </div>");
                    }
                }

                out.println("  </div>"); // pa loss tuts
                out.println("</div>");   // pa las account
            }

        } catch (Exception e) {
            out.println("<p>Error: " + e.getMessage() + "</p>");
            e.printStackTrace();
        }

        out.println("</body>");
        out.println("</html>");
    }

    private String formatTimestamp(String isoTimestamp) {
        try {
            SimpleDateFormat input  = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = input.parse(isoTimestamp);
            return output.format(date);
        } catch (Exception e) {
            return isoTimestamp;
        }
    }
}