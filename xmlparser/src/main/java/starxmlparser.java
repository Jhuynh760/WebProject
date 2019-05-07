import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class starxmlparser extends DefaultHandler{
	List<star> starList;
	List<String> invalidBdays;
	
	private String loginUser;
	private String loginPasswd;
	private String loginUrl;
	
	private String actorsXML;
	
	
	private String tempVal;
	private star tempStar;
	
	public starxmlparser() {
		loginUser = "mytestuser";
        loginPasswd = "Sugars62846897";
        loginUrl = "jdbc:mysql://localhost:3306/moviedb";
      
		actorsXML = "stanford-movies/actors63.xml";

		starList = new ArrayList<star>();
		invalidBdays = new ArrayList<String>();
	}
	
	public void runParser() {
		parseActors();
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
		try (PrintWriter validStars = new PrintWriter("stanford-movies/Stars.log")){
			Iterator<star> it = starList.iterator();
			while(it.hasNext()) {
				validStars.println(it.next().toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished writing Stars.log");
	}
	
	private void addDataToDb() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
	        Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);			
	        dbcon.setAutoCommit(false);
	        
	        String sqlAddStarQuery = "CALL add_star(?, ?);";
			PreparedStatement addStarStatement = dbcon.prepareStatement(sqlAddStarQuery);
			
			int counter = 0;
			Iterator<star> it = starList.iterator();
			while(it.hasNext()) {
				star currentStar = it.next();
				String name = currentStar.getName();
				addStarStatement.setString(1, name);
				
				int dob = currentStar.getBirthYear();
				if (dob == -1) {
					addStarStatement.setNull(2, java.sql.Types.NULL);
				} else {
					addStarStatement.setInt(2, dob);
				}
				addStarStatement.addBatch();
				counter++;
				it.remove();
			}
			addStarStatement.executeBatch();
			dbcon.commit();
			addStarStatement.close();
	        dbcon.close();
		} catch(Exception e) {
			e.printStackTrace();
		}

		System.out.println("Finished adding actors to database.");
	}
	
	public void parseActors() {
		//get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(actorsXML, this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        System.out.println("Finished parsing actors.");
    }
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("Actor")) {
            //create a new instance of employee
        	tempStar = new star();
        	tempStar.setType(attributes.getValue("type"));
        }
    }
	
	public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }
	
	public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("Actor")) {
            //add it to the list
        	if (!starList.contains(tempStar)) {
        		starList.add(tempStar);
        	}
        } else if (qName.equalsIgnoreCase("stagename")) {
        	tempStar.setName(tempVal);
        } else if (qName.equalsIgnoreCase("dob")) {
        	try {
        		int birthYear = Integer.parseInt(tempVal);
        		tempStar.setBirthYear(birthYear);
        	} catch (Exception e){
        		tempStar.setBirthYear(-1);
				invalidBdays.add(tempStar.toString());
        	}
        }
    }
	
	public static void main(String[] args) {
		starxmlparser xmlp = new starxmlparser();
		xmlp.runParser();
	}
}
