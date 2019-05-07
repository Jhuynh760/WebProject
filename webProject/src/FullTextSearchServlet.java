import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * Servlet implementation class MovieListServlet
 */
@WebServlet(name = "FullTextSearchServlet", urlPatterns = "/api/FullTextSearchServlet")
public class FullTextSearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long tsStartTime = System.nanoTime();
		long tjSum = 0;
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
			
			String query = "SELECT DISTINCT m.id, m.title, m.year, m.director, r.rating " +
						   "FROM movies m INNER JOIN ratings r " + 
						   "ON m.id = r.movieid " +
						   "WHERE MATCH (m.title) AGAINST (? IN BOOLEAN MODE) " + 
						   "ORDER BY ";
			
			String sortby = request.getParameter("sortby");
			//IF ADVANCED SORT for any queries
			 if (sortby.equals("TitleAsc")){
				 query += "m.title ASC";
			} else if(sortby.equals("TitleDesc")) {
				query += "m.title DESC";
			} else if (sortby.equals("RatingAsc")) {
				query += "r.rating ASC";
			}else { //default sort setting
				query += "r.rating DESC";
			}
			query += " LIMIT ? OFFSET ?";
			
			// universal pagenum and offset used
//			query += "LIMIT " + Integer.parseInt(offset) + " OFFSET " + (Integer.parseInt(pagenum) * Integer.parseInt(offset)) + ";";
			//do query
			PreparedStatement statement = dbcon.prepareStatement(query);
			
			String search_query = request.getParameter("search_query");
			String[] splitQuery = search_query.split("\\s+");
			ArrayList<String> newSplit = new ArrayList<String>();
			for (String token : splitQuery) {
				newSplit.add("+" + token + "*");
			}
			String bool_query = String.join(" ", newSplit);
			statement.setString(1, bool_query);
			
			
			String pagenum = request.getParameter("pagenum");
			String offset = request.getParameter("offset");
			statement.setInt(2, Integer.parseInt(offset));
			statement.setInt(3, Integer.parseInt(pagenum) * Integer.parseInt(offset));
			
			long tjStartTime = System.nanoTime();
			ResultSet rs = statement.executeQuery();
			long tjEndTime = System.nanoTime();
			tjSum += tjEndTime - tjStartTime;
			
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
				
				tjStartTime = System.nanoTime();
				ResultSet genresResult = genreStatement.executeQuery();
				tjEndTime = System.nanoTime();
				tjSum += tjEndTime - tjStartTime;
				
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
				
				tjStartTime = System.nanoTime();
				ResultSet starsResult = starsStatement.executeQuery();
				tjEndTime = System.nanoTime();
				tjSum += tjEndTime - tjStartTime;
				
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
			e.printStackTrace();
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set response status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		
		out.close();
		long tsEndTime = System.nanoTime();
		long tsElapsedTime = tsEndTime - tsStartTime;
		String contextPath = getServletContext().getRealPath("/");
		String xmlFilePath = contextPath + "/TSTJLogFile.txt";
		System.out.println(xmlFilePath);
		File logFile = new File(xmlFilePath);
		logFile.createNewFile();
		try(FileWriter fw = new FileWriter(logFile, true)){
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter logOutput = new PrintWriter(bw);
			logOutput.println("TS: " + tsElapsedTime + ", TJ: " + tjSum);
			logOutput.close();
			bw.close();
			fw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
