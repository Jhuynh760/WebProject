import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
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
import java.sql.PreparedStatement;;

/**
 * This class is declared as LoginServlet in web annotation, 
 * which is mapped to the URL pattern /api/login
 */
@WebServlet(name = "CheckoutServlet", urlPatterns = "/api/checkout")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    
    @Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String cardNumber = request.getParameter("cardNumber");
        String expirationDate = request.getParameter("expirationDate");

        /**
         * This example only allows username/password to be anteater/123456
         * In real world projects, you should talk to the database to verify username/password
         */
//    	response.setContentType("application/json");
		
		PrintWriter out = response.getWriter();
		
		try {
			//connect to database
			Context initCtx = new InitialContext();
			
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			if (envCtx == null)
                System.out.println("envCtx is NULL");
			
			DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");
			
			if (ds == null) {
				System.out.println("ds is null");
			}
			
			Connection dbcon = ds.getConnection();
			if (dbcon == null) {
				System.out.println("dbcon is null.");
			}
//			Connection dbcon = dataSource.getConnection();
			
			String query = "SELECT c.id, c.firstName, c.lastName, c.ccId FROM creditcards cc INNER JOIN customers c " +
					   "ON cc.id = c.ccId " + 
					   "WHERE cc.id = ? " + 
					   		"AND cc.firstName = ? " + 
					   		"AND cc.lastName = ? " + 
					   		"AND cc.expiration = ? " +
					   "LIMIT 1;";
			PreparedStatement statement = dbcon.prepareStatement(query);
			statement.setString(1, cardNumber);
			statement.setString(2, firstName);
			statement.setString(3, lastName);
			statement.setString(4, expirationDate);
			//do query
			ResultSet rs = statement.executeQuery();
			
			if (rs.first()) {
	            JsonObject jsonObject = new JsonObject();
	            jsonObject.addProperty("id", rs.getString("id"));
	            jsonObject.addProperty("status", "success");
	            jsonObject.addProperty("message", "success");

	            out.write(jsonObject.toString());
			} else {
				// Login fails
	            JsonObject jsonObject = new JsonObject();
	            jsonObject.addProperty("status", "fail");
	            jsonObject.addProperty("message", "Incorrect card information.");
	            out.write(jsonObject.toString());
			}
			
			rs.close();
			statement.close();
			dbcon.close();
			
		} catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("status", "fail");
			jsonObject.addProperty("message", e.getMessage());
			out.write(jsonObject.toString());
		}
		out.close();
    }
}