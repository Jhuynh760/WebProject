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
import java.util.ArrayList;
import java.sql.PreparedStatement;

/**
 * Servlet implementation class MovieListServlet
 */
@WebServlet(name = "MovieSuggestionServlet", urlPatterns = "/api/movie-suggestion")
public class MovieSuggestionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//		response.setContentType("application/json");
		
		JsonArray jsonArray = new JsonArray();
		
		String query_search = request.getParameter("query").trim();	
		
		if (query_search == null || query_search.trim().isEmpty()) {
			response.getWriter().write(jsonArray.toString());
			return;
		}
		
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
			
			
			String query = "SELECT m.id, m.title, m.year, m.director, r.rating "
					+ "FROM movies m INNER JOIN ratings r "
					+ "ON m.id = r.movieid "
					+ "WHERE MATCH (m.title) AGAINST (? IN BOOLEAN MODE) LIMIT 10;";
			PreparedStatement statement = dbcon.prepareStatement(query);
			
			String[] splitQuery = query_search.split("\\s+");
			ArrayList<String> newSplit = new ArrayList<String>();
			for (String token : splitQuery) {
				newSplit.add("+" + token + "*");
			}
			String bool_query = String.join(" ", newSplit);
			System.out.println(bool_query);
			statement.setString(1, bool_query);
			//do query
			ResultSet rs = statement.executeQuery();
			
			while (rs.next()) {
				String movie_id = rs.getString("id");
				String movie_title = rs.getString("title");
				String movie_year = rs.getString("year");
				String movie_director = rs.getString("director");
				String movie_rating = rs.getString("rating");
				
				JsonObject jsonObject= new JsonObject();
				
				String value = "Title: " + movie_title + "\tDirector: " + movie_director + "\tYear: " + movie_year;

				jsonObject.addProperty("value", value);
				
				JsonObject data= new JsonObject();
				
				data.addProperty("movie_id", movie_id);
				data.addProperty("movie_title", movie_title);
				data.addProperty("movie_year", movie_year);
				data.addProperty("movie_director", movie_director);
				data.addProperty("movie_rating", movie_rating);
				
				jsonObject.add("data", data);
				
				jsonArray.add(jsonObject);
			}
			
			response.getWriter().write(jsonArray.toString());
			rs.close();
			statement.close();
			dbcon.close();
			
		} catch (Exception e) {
			response.sendError(500, e.getMessage());
		}
	}
}
