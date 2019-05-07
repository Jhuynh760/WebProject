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
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/movies/single")
public class SingleMovieServlet extends HttpServlet {
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
			
			
			String query = "SELECT m.id, m.title, m.year, m.director, r.rating "
					+ "FROM movies m INNER JOIN ratings r "
					+ "ON m.id = r.movieid "
					+ "WHERE m.id = ?;";
			PreparedStatement statement = dbcon.prepareStatement(query);
			statement.setString(1, id);
			//do query
			ResultSet rs = statement.executeQuery();
			
			JsonArray jsonArray = new JsonArray();
			
			while (rs.next()) {
				String movie_id = rs.getString("id");
				String movie_title = rs.getString("title");
				String movie_year = rs.getString("year");
				String movie_director = rs.getString("director");
				String movie_rating = rs.getString("rating");
				
				
				//GENRES QUERY
				query = "SELECT g.name, g.id "
						+ "FROM genres g INNER JOIN genres_in_movies gm "
						+ "ON g.id = gm.genreId "
						+ "WHERE gm.movieId = ?;"; 
				PreparedStatement genreStatement = dbcon.prepareStatement(query);
				genreStatement.setString(1, movie_id);
				
				ResultSet genresResult = genreStatement.executeQuery();
				
				JsonArray movie_genres = new JsonArray();
				while (genresResult.next()) {
					JsonObject genre_info = new JsonObject();
					genre_info.addProperty("genre", genresResult.getString("name"));
					genre_info.addProperty("genre_id", genresResult.getString("id"));
					movie_genres.add(genre_info);
				}
				genreStatement.close();
				genresResult.close();
				//GENRES QUERY
				
				//STARS QUERY
				query = "Select s.id, s.name "
						+ "FROM stars s INNER JOIN stars_in_movies sm "
						+ "ON s.id = sm.starId "
						+ "WHERE sm.movieId = ?;";
				PreparedStatement starsStatement = dbcon.prepareStatement(query);
				starsStatement.setString(1, movie_id);
				ResultSet starsResult = starsStatement.executeQuery();
				
				JsonArray movie_stars = new JsonArray();
				while(starsResult.next()) {
					JsonObject star = new JsonObject();
					star.addProperty("star_name", starsResult.getString("name"));
					star.addProperty("star_id", starsResult.getString("id"));
					movie_stars.add(star);
				}
				
				starsStatement.close();
				starsResult.close();
				//STARS QUERY				
				
				JsonObject jsonObject= new JsonObject();
				
				jsonObject.addProperty("movie_id", movie_id);
				jsonObject.addProperty("movie_title", movie_title);
				jsonObject.addProperty("movie_year", movie_year);
				jsonObject.addProperty("movie_director", movie_director);
				jsonObject.addProperty("movie_rating", movie_rating);
				jsonObject.add("movie_genres", movie_genres);
				jsonObject.add("movie_stars", movie_stars);
				
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
