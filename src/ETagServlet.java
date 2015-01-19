import com.google.common.base.Strings;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

public class ETagServlet extends HttpServlet {
    private static final String SMART_COOKIE = "smartUID";
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        String smartUID = null;
        if (cookies != null){
            //Find the UUID in the cookie
            for(Cookie cookie : cookies){
                if(SMART_COOKIE.equals(cookie.getName())){
                    smartUID = cookie.getValue();
                }
            }
        }
        //Get the previous eTag
        String eTag  =  request.getHeader("If-None-Match");
        // If no smartUID was found in cookie.
        if (!Strings.isNullOrEmpty(smartUID)){
            eTag =  request.getHeader("If-None-Match");
            if (smartUID.equals(eTag )){
                //eTag matched, send 304 Not Modified
                response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
            }else{
                response.setDateHeader("Last-Modified", Calendar.getInstance().getTime().getTime());
            }
        }else{
            //No cookie, check if we have a eTag.
            if (Strings.isNullOrEmpty(eTag )){
                //If we don't, generate a new UUID.
                eTag  = UUID.randomUUID().toString();
            }
            //Set the cookie in the response.
            smartUID = eTag;
            response.addCookie(new Cookie(SMART_COOKIE, smartUID));
        }
        //Update the eTag.
        response.setHeader("ETag", smartUID);
    }
}