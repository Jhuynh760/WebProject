import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
/**
 * Servlet implementation class MovieListServlet
 */
@WebServlet(name = "GenreServlet", urlPatterns = "/api/genre")
public class GenreServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		
		
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
			
			String genrequery = request.getParameter("genrequery");
			String genreid = request.getParameter("genreid");
			String pagenum = request.getParameter("pagenum");
			String offset = request.getParameter("offset");
			
			
			String query = "SELECT * FROM genres;";
			PreparedStatement statement = dbcon.prepareStatement(query);
			ResultSet rs = statement.executeQuery();
			JsonArray jsonArray = new JsonArray();
			if (genrequery.equals("general")){
				//if getting genres to insert html
				while (rs.next()) {
					String genre_id = rs.getString("id");
					String genre_name = rs.getString("name");
					JsonObject jsonObject= new JsonObject();
					
					jsonObject.addProperty("genre_id", genre_id);
					jsonObject.addProperty("genre_name", genre_name);
					jsonArray.add(jsonObject);
				}
			}
			
			out.write(jsonArray.toString());
			
			response.setStatus(200);
			rs.close();
			statement.close();
			dbcon.close();
			
		} catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set response status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		
		out.close();
		
	}
}
