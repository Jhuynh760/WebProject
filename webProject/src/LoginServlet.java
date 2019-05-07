import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import org.jasypt.util.password.StrongPasswordEncryptor;

/**
 * This class is declared as LoginServlet in web annotation, 
 * which is mapped to the URL pattern /api/login
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    
    @Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//    	// determine request origin by HTTP Header User Agent string
//        String userAgent = request.getHeader("User-Agent");
//        System.out.println("recieved login request");
//        System.out.println("userAgent: " + userAgent);
//
//        // only verify recaptcha if login is from Web (not Android)
//        if (userAgent != null && !userAgent.contains("Android")) {
//            String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
//            // verify recaptcha first
//            try {
//                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
//            } catch (Exception e) {
//                System.out.println("recaptcha success");
//                JsonObject responseJsonObject = new JsonObject();
//                responseJsonObject.addProperty("status", "fail");
//                responseJsonObject.addProperty("message", e.getMessage());
//                response.getWriter().write(responseJsonObject.toString());
//                return;
//            }
//        }
    	
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String edashboard = request.getParameter("edashboard"); //bool value if trying to enter edashboard

        /**
         * This example only allows username/password to be anteater/123456
         * In real world projects, you should talk to the database to verify username/password
         */
//    	response.setContentType("application/json");
		
		PrintWriter out = response.getWriter();
		
		try {
			//connect to database
			Connection dbcon = dataSource.getConnection();
			
//			Statement statement = dbcon.createStatement();
			String query;
			if (edashboard.equals("true")) {
				query = "SELECT * FROM employees "
						+ "WHERE email = ? "
						+ "LIMIT 1;";
			} else {
				query = "SELECT * FROM customers "
						+ "WHERE email = ? "
						+ "LIMIT 1;";
			}
			
			PreparedStatement statement = dbcon.prepareStatement(query);
			statement.setString(1, username);
			//do query
			ResultSet rs = statement.executeQuery();
			
			if (rs.next()) {
				// Login succeeds
	            // Set this user into current session
				String encryptedPassword = rs.getString("password");
				
				boolean success = false;
				success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
				
				if (success) {
					String sessionId = ((HttpServletRequest) request).getSession().getId();
		            Long lastAccessTime = ((HttpServletRequest) request).getSession().getLastAccessedTime();
		            request.getSession().setAttribute("user", new User(username));
		            if (edashboard.equals("true")) {
			            request.getSession().setAttribute("employee", true);
					}

		            JsonObject jsonObject = new JsonObject();
		            jsonObject.addProperty("status", "success");
		            jsonObject.addProperty("message", "success");
		            if (edashboard.equals("true")) {
		            	jsonObject.addProperty("isemployee", "true");
					}

		            out.write(jsonObject.toString());
				} else {
					// Login fails
		            JsonObject jsonObject = new JsonObject();
		            jsonObject.addProperty("status", "fail");
		            jsonObject.addProperty("message", "Incorrect Password.");
		            out.write(jsonObject.toString());
				}
			} else {
				// Login fails
	            JsonObject jsonObject = new JsonObject();
	            jsonObject.addProperty("status", "fail");
	            jsonObject.addProperty("message", "Incorrect Username.");
	            out.write(jsonObject.toString());
			}
			
			rs.close();
			statement.close();
			dbcon.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("status", "fail");
			jsonObject.addProperty("message", e.getMessage());
			out.write(jsonObject.toString());
		}
		out.close();
    }
}