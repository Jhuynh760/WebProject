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
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet implementation class MovieListServlet
 */
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movies")
public class MovieListServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		

		String search_type = request.getParameter("search_type");
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
			
			String query = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating " +
					"FROM movies m INNER JOIN ratings r " + //INNER JOIN stars_in_movies sm INNER JOIN stars s " + INNER JOIN genres g INNER JOIN genres_in_movies gm " +
					"ON m.id = r.movieid "; //AND m.id = sm.movieId and sm.starId = s.id AND m.id = gm.movieId and gm.genreId = g.id ";
			
			if(search_type != null) { //if search button clicked
				String hidden_search_query = request.getParameter("hidden_search_query");
				String search_query = request.getParameter("search_query");
				String search_title = request.getParameter("search_title");
				String search_year = request.getParameter("search_year");
				String search_director = request.getParameter("search_director");
				String search_star = request.getParameter("search_star");
				String sortby = request.getParameter("sortby");
				
				if ((!search_title.equals("null") || !search_year.equals("null") || !search_director.equals("null") || !search_star.equals("null")) && !search_query.equals("")) {
					//if at least one search option is selected and query is not empty.
					if (search_type.equals("genre") || !search_star.equals("null")){
						query = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating " +
								"FROM movies m INNER JOIN ratings r INNER JOIN stars_in_movies sm INNER JOIN stars s INNER JOIN genres g INNER JOIN genres_in_movies gm " +
								"ON m.id = r.movieid AND m.id = sm.movieId and sm.starId = s.id and m.id = gm.movieId and gm.genreId = g.id ";
						if (search_type.equals("genre")) {
							query += "AND g.id = " + hidden_search_query + " ";
						}
					}
					//if field search selected
					List<String> categories = new ArrayList<>();
					if (!search_star.equals("null")) {
						categories.add("s.name");
					}
					if (!search_title.equals("null")) {
						categories.add("m.title");
					}
					if(!search_year.equals("null")) {
						categories.add("m.year");
					}
					if (!search_director.equals("null")) {
						categories.add("m.director");
					}

					query += "WHERE CONCAT(";
					query += String.join(", ", categories);
					query += ") LIKE \'%" + search_query + "%\' ";
					
					if (search_type.equals("alphabet")){
						query += "AND m.title ";
						if (hidden_search_query.equals("etc")) {
							query += "regexp \'^[^a-z0-9]\' ";
						} else {
							query += "LIKE \'" + hidden_search_query + "%\' ";
						}
					}
				} else if (!search_query.equals("")){
					//if no search options selected but there is a search query
					query += "WHERE CONCAT(m.title, m.year, m.director) LIKE \'%" + search_query + "%\' ";
					if (search_type.equals("alphabet")){
						query += "AND m.title ";
						if (hidden_search_query.equals("etc")) {
							query += "regexp \'^[^a-z0-9]\' ";
						} else {
							query += "LIKE \'" + hidden_search_query + "%\' ";
						}
					}
				} else { 
					//if field search not selected and no search query
					//used for genre genre hyperlink or alphabet hyper link
					if (search_type.equals("genre")){
						query = "SELECT m.id, m.title, m.year, m.director, r.rating "
								+ "FROM movies m INNER JOIN ratings r INNER JOIN genres g INNER JOIN genres_in_movies gm "
								+ "ON m.id = r.movieid  AND m.id = gm.movieId AND gm.genreId = g.id AND g.id = " + hidden_search_query + " ";
					} else {
						query = "SELECT m.id, m.title, m.year, m.director, r.rating "
								+ "FROM movies m INNER JOIN ratings r "
								+ "ON m.id = r.movieid ";
					}
					//if search query empty
					if (search_type.equals("alphabet")){
						query += "WHERE m.title ";
						if (hidden_search_query.equals("etc")) {
							query += "regexp \'^[^a-z0-9]\' ";
						} else {
							query += "LIKE \'" + hidden_search_query + "%\' ";
						}
					}
				}
				//IF ADVANCED SORT for any queries
				 if (sortby.equals("TitleAsc")){
					query += "ORDER BY m.title ASC ";
				} else if(sortby.equals("TitleDesc")) {
					query += "ORDER BY m.title DESC ";
				} else if (sortby.equals("RatingAsc")) {
					query += "ORDER BY r.rating ASC ";
				}else { //default sort setting
					query += "ORDER BY r.rating DESC ";
				}
				
			} else {
				//movies landing page
				// DEFAULT MOVIES SORTING IS BY RATING
				query += "ORDER BY r.rating DESC ";
			}
			
			// universal pagenum and offset used
			String pagenum = request.getParameter("pagenum");
			String offset = request.getParameter("offset");
			query += "LIMIT " + Integer.parseInt(offset) + " OFFSET " + (Integer.parseInt(pagenum) * Integer.parseInt(offset)) + ";";
			//do query
			PreparedStatement statement = dbcon.prepareStatement(query);
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
					JsonObject movie = new JsonObject();
					String genre = genresResult.getString("name");
					String id = genresResult.getString("id");
					movie.addProperty("genre", genre);
					movie.addProperty("genre_id", id);
					movie_genres.add(movie);
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
