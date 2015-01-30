import com.google.common.base.Strings;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ETagServlet extends HttpServlet {
    private static final String SMART_COOKIE = "smartUID";
    private static final Logger logger = Logger.getLogger(ETagServlet.class.getCanonicalName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.log(Level.FINE, "Remote Address: " + request.getRemoteAddr());
        Cookie[] cookies = request.getCookies();
        String smartUID = null;
        if (cookies != null){
            //Find the UUID in the cookie
            for(Cookie cookie : cookies){
                if(SMART_COOKIE.equals(cookie.getName())){
                    smartUID = cookie.getValue();
                    logger.log(Level.FINE, "Cookie: " + smartUID);
                }
            }
        }
        //Get the previous eTag
        String eTag  =  request.getHeader("If-None-Match");
        logger.log(Level.FINE, "Previous eTag: " + eTag);
        // If no smartUID was found in cookie.
        if (!Strings.isNullOrEmpty(smartUID)){
            eTag =  request.getHeader("If-None-Match");
            if (smartUID.equals(eTag )){
                //eTag matched, send 304 Not Modified
                logger.log(Level.FINE, "304 Not Modified");
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            }else{
                response.setDateHeader("Last-Modified", Calendar.getInstance().getTime().getTime());
            }
        }else{
            //No cookie, check if we have a eTag.
            if (Strings.isNullOrEmpty(eTag)){
                //If we don't, generate a new UUID.
                eTag  = UUID.randomUUID().toString();
                logger.log(Level.FINE, "Generating new UUID");
            }
            //Set the cookie in the response.
            smartUID = eTag;
            response.addCookie(new Cookie(SMART_COOKIE, smartUID));
            logger.log(Level.FINE, "Creating new cookie.");
        }
        //Update the eTag.
        logger.log(Level.FINE, "Updating the eTag");
        response.setHeader("ETag", smartUID);
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "ETag");
    }
}