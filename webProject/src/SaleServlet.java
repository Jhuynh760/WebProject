import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import org.json.JSONObject;
import org.json.JSONArray;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * This class is declared as LoginServlet in web annotation, 
 * which is mapped to the URL pattern /api/login
 */
@WebServlet(name = "SaleServlet", urlPatterns = "/api/sale")
public class SaleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    
    @Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String customerId = request.getParameter("id");
    	JSONArray cart = new JSONArray(request.getParameter("cart"));
        /**
         * This example only allows username/password to be anteater/123456
         * In real world projects, you should talk to the database to verify username/password
         */
    	response.setContentType("application/json");
		
		PrintWriter out = response.getWriter();
		
		try {
			//connect to database
			Context initCtx = new InitialContext();
			
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			if (envCtx == null)
                System.out.println("envCtx is NULL");
			
			DataSource ds = (DataSource) envCtx.lookup("jdbc/MasterDB");
			
			if (ds == null) {
				System.out.println("ds is null");
			}
			
			Connection dbcon = ds.getConnection();
			if (dbcon == null) {
				System.out.println("dbcon is null.");
			}
//			Connection dbcon = dataSource.getConnection();
			
			JsonArray jsonArray = new JsonArray();
			
			for (int i = 0; i < cart.length(); i++) {
	    		JSONObject movie = cart.getJSONObject(i);
	    		String movieId = movie.getString("id");
	    		float movieRating = movie.getFloat("rating");
	    		String movieTitle = movie.getString("title");
	    		int movieYear = movie.getInt("year");
	    		String director = movie.getString("director");
	    		int quantity = movie.getInt("quantity");

	    		
	    		String query = "INSERT INTO sales (customerId, movieId, saleDate) " + 
	    					   "VALUES(?, ?, CURDATE());";
	    		PreparedStatement statement = dbcon.prepareStatement(query);
	    		statement.setString(1, customerId);
	    		statement.setString(2, movieId);
	    		
	    		query = "Select * from Sales " + 
	    				"WHERE id = last_insert_id() AND customerId = ? AND movieId = ? AND saleDate = CURDATE();";
	    		PreparedStatement recordStatement = dbcon.prepareStatement(query);
	    		recordStatement.setString(1, customerId);
	    		recordStatement.setString(2, movieId);
	    		
				for (int s = 0; s < quantity; s++) {
		    		statement.executeUpdate();
		    		ResultSet rs = recordStatement.executeQuery();
					rs.next();
					int saleId = rs.getInt("id");
					
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("saleId", saleId);
					jsonObject.addProperty("movieId", movieId);
					jsonObject.addProperty("movieRating", movieRating);
					jsonObject.addProperty("movieTitle", movieTitle);
					jsonObject.addProperty("movieYear", movieYear);
					jsonObject.addProperty("director", director);
		            
					jsonArray.add(jsonObject);
					
					rs.close();
				}
				statement.close();
			}
			out.write(jsonArray.toString());
			dbcon.close();
		} catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("status", "fail");
			jsonObject.addProperty("message", e.getMessage());
			e.printStackTrace();
			out.write(jsonObject.toString());
		}
		out.close();
    }
}