import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class mainsxmlparser extends DefaultHandler{
	List<director> directorList;
	
	private String loginUser;
	private String loginPasswd;
	private String loginUrl;
	
	private String mainXML;
	
	
	private String tempVal;
	private director tempDirector;
	
	public mainsxmlparser() {
		loginUser = "mytestuser";
        loginPasswd = "Sugars62846897";
        loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        
        mainXML = "stanford-movies/mains243.xml";
        
        directorList = new ArrayList<director>();
	}
	
	public void runParser() {
		parseMains();
		printData();
		long startTime = System.currentTimeMillis();
		addDataToDb();
		long endTime = System.currentTimeMillis();
		long totalMs = endTime - startTime;
		System.out.println("Total time to execute: " + totalMs + " ms");
		System.out.println("Total time to execute: " + String.format("%02dH:%02dM:%02dS", 
				TimeUnit.MILLISECONDS.toHours(totalMs),
				TimeUnit.MILLISECONDS.toMinutes(totalMs) -  
				TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalMs)), // The change is in this line
				TimeUnit.MILLISECONDS.toSeconds(totalMs) - 
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalMs))));
	}
	
	private void printData() {
		try(PrintWriter outDirectorList = new PrintWriter("stanford-movies/mains.log")){
			Iterator<director> it = directorList.iterator();
			while(it.hasNext()) {
				outDirectorList.println(it.next().toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished writing to mains.log");
	}
	
	private void addDataToDb() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
	        Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);	
	        dbcon.setAutoCommit(false);
	        
	        String query = "CALL add_movie_no_out_params(?, ?, ?, ?, ?);";
			PreparedStatement addMovieStatement = dbcon.prepareStatement(query);
	        
			Iterator<director> it = directorList.iterator();
			while(it.hasNext()) {
				director tempDirector = it.next();
				String directorName = tempDirector.getDirectorName();
				addMovieStatement.setString(4, directorName);
				
				int counter = 0;
				Iterator<film> filmit = tempDirector.getFilmList().iterator();
				while(filmit.hasNext()) {
					film tempFilm = filmit.next();
					filmit.remove();
					String filmTitle = tempFilm.getTitle();
					String movieId = tempFilm.getMovieId();
					int filmYear = tempFilm.getYear();
					addMovieStatement.setString(1, movieId);
					addMovieStatement.setString(2, filmTitle);
					addMovieStatement.setInt(3, filmYear);
					
					if (tempFilm.getGenres().size() > 0) {
						Iterator<String> genreIt = tempFilm.getGenres().iterator();
						while(genreIt.hasNext()) {
							String genre = genreIt.next();
							addMovieStatement.setString(5, genre);
							addMovieStatement.addBatch();
							counter++;
							if (counter % 500 == 0) {
								addMovieStatement.executeBatch();
							}
						}
					} else {
						addMovieStatement.setNull(5, java.sql.Types.NULL);
						addMovieStatement.addBatch();
						counter++;
						if (counter % 500 == 0) {
							addMovieStatement.executeBatch();
						}
					}
				}
			}
			addMovieStatement.executeBatch();
			dbcon.commit();
			
			addMovieStatement.close();
	        dbcon.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished addings mains.xml to database.");
	}
	
	public void parseMains() {
		//get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(mainXML, this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        System.out.println("Finished parsing mains.xml");
    }
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("directorfilms")) {
            //create a new instance of employee
        	tempDirector = new director();
        	tempDirector.setType(attributes.getValue("type"));
        }
    }
	
	public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }
	
	public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("directorfilms")) {
            //add it to the list
        	if (!tempDirector.getDirectorName().isEmpty() && tempDirector.getFilmList().size() > 0) {
        		directorList.add(tempDirector);
        	}
        }else if (qName.equalsIgnoreCase("dirname")) {
        	tempDirector.setDirectorName(tempVal.trim());
        } else if (qName.equalsIgnoreCase("t")) {
        	tempDirector.setTempTitle(tempVal.trim());
        } else if (qName.equalsIgnoreCase("fid")) {
        	tempDirector.setTempMovieId(tempVal.trim());
        } else if (qName.equalsIgnoreCase("year")) {
        	try {
        		int filmYear = Integer.parseInt(tempVal);
        		tempDirector.setTempYear(filmYear);
        	} catch (Exception e){
        		tempDirector.setTempYear(-1);
        	}
        } else if (qName.equalsIgnoreCase("cat")) {
        	if (!tempDirector.getTempGenres().contains(tempVal.trim())) {
        		tempDirector.addToTempGenres(tempVal.trim());
        	}
        } else if (qName.equalsIgnoreCase("film")) {
        	film tempFilm = new film(tempDirector.getTempTitle(), tempDirector.getTempYear(), tempDirector.getTempMovieId());
        	if (tempDirector.getTempMovieId() != null && !tempDirector.getTempTitle().isEmpty() && tempDirector.getTempYear() != -1) {
        		if (!tempDirector.getFilmList().contains(tempFilm)) {
        			tempDirector.addFilm();
        		}
        	} else {
        		String tempId;
        		String temptitle;
        		String tempYear;
        		if (tempDirector.getTempMovieId() != null) {
        			tempId = tempDirector.getTempMovieId();
        		} else {
            		tempId = "null";
        		} if (!tempDirector.getTempTitle().isEmpty()) {
        			temptitle = tempDirector.getTempTitle();
        		} else {
        			temptitle = "null";
        		} if (tempDirector.getTempYear() != -1) {
        			tempYear = "" + tempDirector.getTempYear();
        		} else {
        			tempYear = "null";
        		}
        		String out = "MovieId: " + tempId + "\tTitle: " + temptitle + "\tYear: " + tempYear;
        		if (tempDirector.getTempGenres().size()>0) {
        			out += "\n\tGenres: ";
        			Iterator it = tempDirector.getTempGenres().iterator();
        			while (it.hasNext()) {
        				out += "\n\t\t" + it.next().toString();
        			}
        		}
        		
        		tempDirector.addToInvalidFilms(out);
        	}
        }
    }
	
	public static void main(String[] args) {
		mainsxmlparser xmlp = new mainsxmlparser();
		xmlp.runParser();
	}
}
