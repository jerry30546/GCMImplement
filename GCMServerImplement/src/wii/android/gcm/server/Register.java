package wii.android.gcm.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Register extends BaseHttpServer {

    private static final String PARAMETER_REG_ID = "regId";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String regId = getParameter(req, PARAMETER_REG_ID);
        Datastore.register(regId);
        req.setAttribute(HomeIndex.ATTRIBUTE_STATUS, "New Registration");
        getServletContext().getRequestDispatcher("/index").forward(req, resp);
    }

}
