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
import java.io.IOException;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;

/**
 * This class is declared as LoginServlet in web annotation, 
 * which is mapped to the URL pattern /api/login
 */
@WebServlet(name = "EdashboardServlet", urlPatterns = "/api/edashboard")
public class EdashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    
    @Resource(name = "jdbc/moviedb")
	private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("application/json");
    	
    	String querytype = request.getParameter("querytype");	
    	
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
			JsonArray jsonArray = new JsonArray();
    		if (querytype.toLowerCase().equals("allmetadata")) {
    			DatabaseMetaData dbmeta = dbcon.getMetaData();
    			
    			ResultSet rs = dbmeta.getTables(null,  null, null, new String[] {"TABLE"});
    			
    			while (rs.next()) {
    				JsonObject table = new JsonObject();
    				String table_name = rs.getString("TABLE_NAME");
    				table.addProperty("name", table_name);
    				
    				ResultSet columns = dbmeta.getColumns(null, null, table_name, null);
    				
    				JsonArray metadata = new JsonArray();
    				while (columns.next()) {
    					JsonObject columndata = new JsonObject();
    					String columnName = columns.getString("COLUMN_NAME");
    					String datatype = columns.getString("TYPE_NAME");
    					int datasize = columns.getInt("COLUMN_SIZE");
    					columndata.addProperty("columnName", columnName);
    					columndata.addProperty("datatype", datatype);
    					columndata.addProperty("datasize", datasize);
    					
    					metadata.add(columndata);
    				}
    				table.add("metadata", metadata);
    				jsonArray.add(table);
    				
    				columns.close();
    			}
    			rs.close();
    		}
			out.write(jsonArray.toString());
			
			response.setStatus(200);
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("application/json");
    	
    	String querytype = request.getParameter("querytype");	
    	
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
    		if (querytype.toLowerCase().equals("addstar")) {
    			String star_name = request.getParameter("star_name");
				String star_birth_year = request.getParameter("star_birth_year");
    			//System.out.println(star_name.isEmpty() + " - " + star_birth_year.isEmpty());
    			
    			String query = "{CALL add_star(?, ?)}";
    			CallableStatement addStarStatement = dbcon.prepareCall(query);
    			addStarStatement.setString(1, star_name);
    			if (star_birth_year.isEmpty()) {
    				addStarStatement.setNull(2, java.sql.Types.NULL);
    			} else {
    				addStarStatement.setInt(2, Integer.parseInt(star_birth_year));
    			}
    			addStarStatement.execute();
    			addStarStatement.close();
    			JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("status", "success");
				jsonArray.add(jsonObject);
    				
    		} else if (querytype.toLowerCase().equals("addmovie")) {
    			String movie_title = request.getParameter("movie_title");
    			String movie_year = request.getParameter("movie_year");
    			String movie_director = request.getParameter("movie_director");
    			String star_name = request.getParameter("link_star_to_movie");
    			String genre = request.getParameter("link_genre_to_movie");
    			
				//String star_birth_year = request.getParameter("star_birth_year");
    			//System.out.println(star_name.isEmpty() + " - " + star_birth_year.isEmpty());
    			
    			String query = "{CALL add_movie(?, ?, ?, ?, ?, ?, ?, ?)}";
    			CallableStatement addStarStatement = dbcon.prepareCall(query);
    			addStarStatement.setString(1, movie_title);
    			addStarStatement.setInt(2, Integer.parseInt(movie_year));
    			addStarStatement.setString(3, movie_director);
    			
    			if (star_name.isEmpty()) {
    				addStarStatement.setNull(4, java.sql.Types.NULL);
    			} else {
    				addStarStatement.setString(4, star_name);
    			}
    			
    			if (genre.isEmpty()) {
    				addStarStatement.setNull(5, java.sql.Types.NULL);
    			} else {
    				addStarStatement.setString(5, genre);
    			}
    			addStarStatement.registerOutParameter(6, java.sql.Types.INTEGER);
    			addStarStatement.registerOutParameter(7, java.sql.Types.INTEGER);
    			addStarStatement.registerOutParameter(8, java.sql.Types.INTEGER);
    			
    			addStarStatement.execute();
    			
    			int movie_created = addStarStatement.getInt(6);
    			int linked_star_to_movie = addStarStatement.getInt(7);
    			int linked_genre_to_movie = addStarStatement.getInt(8);
    			
    			addStarStatement.close();
    			JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("status", "success");
				jsonObject.addProperty("movieCreated", movie_created);
				jsonObject.addProperty("linked_star_to_movie", linked_star_to_movie);
				jsonObject.addProperty("linked_genre_to_movie", linked_genre_to_movie);
				
				jsonArray.add(jsonObject);
    		}
			out.write(jsonArray.toString());
			response.setStatus(200);
			dbcon.close();
			
		} catch (Exception e) {
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("status", "fail");
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());
			// set response status to 500 (Internal Server Error)
			response.setStatus(500);
		}
		
		out.close();
    }
}