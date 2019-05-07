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
@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/stars/single")
public class SingleStarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		
		
		String id = request.getParameter("id");	
		
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
			
			String query = "SELECT * "
					+ "FROM stars "
					+ "WHERE id = ?;";
			PreparedStatement statement = dbcon.prepareStatement(query);
			statement.setString(1, id);
			//do query
			ResultSet rs = statement.executeQuery();
			
			JsonArray jsonArray = new JsonArray();
			
			while (rs.next()) {
				String star_id = rs.getString("id");
				String star_name = rs.getString("name");
				String star_birth_year = rs.getString("birthYear");
				
				//Movies QUERY
				query = "Select m.title, m.id , s.starId "
						+ "FROM movies m INNER JOIN stars_in_movies s "
						+ "ON m.id = s.movieId "
						+ "WHERE s.starId = ?;";
				PreparedStatement moviesStatement = dbcon.prepareStatement(query);
				moviesStatement.setString(1, star_id);
				ResultSet moviesResult = moviesStatement.executeQuery();
				
				JsonArray movies = new JsonArray();
				while(moviesResult.next()) {
					JsonObject star = new JsonObject();
					star.addProperty("movie_id", moviesResult.getString("id"));
					star.addProperty("movie_title", moviesResult.getString("title"));
					movies.add(star);
				}
				
				moviesStatement.close();
				moviesResult.close();
				//Movies QUER				
				
				JsonObject jsonObject= new JsonObject();
				
				jsonObject.addProperty("star_id", star_id);
				jsonObject.addProperty("star_name", star_name);
				jsonObject.addProperty("star_birth_year", star_birth_year);
				jsonObject.add("movies", movies);
				
				jsonArray.add(jsonObject);
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
