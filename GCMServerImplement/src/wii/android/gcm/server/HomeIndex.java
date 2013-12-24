package wii.android.gcm.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Servlet that adds display number of devices and button to send a message.
 * <p>
 * This servlet is used just by the browser (i.e., not device) and contains the
 * main page of the demo app.
 */
public class HomeIndex extends BaseHttpServer {

    static final String ATTRIBUTE_STATUS = "status";

    /**
    * Displays the existing messages and offer the option to send a new one.
    */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.print("<html><body>");
        out.print("<head>");
        out.print("  <title>GCM Demo</title>");
        out.print("  <link rel='icon' href='favicon.png'/>");
        out.print("</head>");

        String status = (String) req.getAttribute(ATTRIBUTE_STATUS);
        if (status != null) {
            out.print(status);
        }
        List<String> devices = Datastore.getDevices();

        out.print("<h2>" + devices.size() + " device(s) registered!</h2>");
        out.print("<form name='form' method='POST' action='sendAll'>");
        out.print("<textarea name=\"Message\" rows=\"3\" cols=\"30\" autofocus=\"autofocus\"></textarea><br>");
        out.print("<input type='submit' value='Send Message' />");
        out.print("</form>");

        out.print("</body></html>");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        doGet(req, resp);
    }

}
