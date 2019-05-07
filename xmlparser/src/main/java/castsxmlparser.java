import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.Lists;

public class castsxmlparser extends DefaultHandler{
	HashMap<String, String[]> hCastList;
	List<String> InvalidCastList;
	
	private String loginUser;
	private String loginPasswd;
	private String loginUrl;
	
	private String castsXML;
	
	private String tempVal;
	private String tempType;
	private String tempName;
	private String tempMovieId;
	
	public castsxmlparser() {
		loginUser = "mytestuser";
        loginPasswd = "Sugars62846897";
        loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        
		castsXML = "stanford-movies/casts124.xml";
		
		InvalidCastList = new ArrayList<String>();
		
		hCastList = new HashMap<String, String[]>();
		
		tempType = "";
		tempName = "";
		tempMovieId = "";
	}
	
	public void runParser() {
		parseCast();
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
		try(PrintWriter castOut = new PrintWriter("stanford-movies/casts.log")){
			if (this.InvalidCastList.size() > 0) {
				castOut.println("--------------INVALID CAST ENTRIES--------------");
				Iterator<String> invalidIt = this.InvalidCastList.iterator();
				while (invalidIt.hasNext()) {
					castOut.println(invalidIt.next());
				}
				castOut.println("--------------INVALID CAST ENTRIES--------------");
			} if (this.hCastList.size()>0) {
				castOut.println("\n\n--------------VALID CAST ENTRIES--------------");
				for (String[] cast : this.hCastList.values()) {
					castOut.println("Name: " + cast[0] + "\tMovieId: " + cast[1]);
				}

				castOut.println("--------------VALID CAST ENTRIES--------------");
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		System.out.println("Finished writing to casts.log");
	}
	
	private void addDataToDb() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
	        Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);		
	        Statement setStatement = dbcon.createStatement();
	        setStatement.execute("SET  FOREIGN_KEY_CHECKS = 0;");
	        setStatement.execute("SET  UNIQUE_CHECKS = 0;");
	        setStatement.close();
	        
	        dbcon.setAutoCommit(false);	
	        
	        String linkStarToMovieQuery = "CALL moviedb.link_star_to_movie_no_out_params(?, ?);";
			PreparedStatement linkStarToMovieStatement = dbcon.prepareStatement(linkStarToMovieQuery);
	        
			int counter = 0;
			int batchNumber = 0;
			for (String[] tempCast : this.hCastList.values()) {
				linkStarToMovieStatement.setString(1, tempCast[0]);
				linkStarToMovieStatement.setString(2, tempCast[1]);
				
				linkStarToMovieStatement.addBatch();
				counter++;
				if (counter % 500 == 0 || counter == this.hCastList.size()) {
					linkStarToMovieStatement.executeBatch();
					batchNumber++;
					System.out.println("Executing batch: " + batchNumber);
    			}
			}
			dbcon.commit();
			linkStarToMovieStatement.close();

			setStatement = dbcon.createStatement();
	        setStatement.execute("SET  UNIQUE_CHECKS = 1;");
			setStatement.execute("SET  FOREIGN_KEY_CHECKS = 1;");
	        setStatement.close();
	        dbcon.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Finished adding casts.xml to database.");
	}
	
	public void parseCast() {
		//get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(castsXML, this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
        System.out.println("Finished parsing casts.xml");
    }
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempVal = "";
        if (qName.equalsIgnoreCase("m")) {
        	this.tempType = attributes.getValue("type");
        	this.tempName = "";
        	this.tempMovieId = "";
        }
    }
	
	public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }
	
	public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("m")) {
            //add it to the list
        	if (!this.tempName.equals("") && !this.tempMovieId.equals("")) {
        		String key = this.tempMovieId + this.tempName;
        		String[] entry = {this.tempName, this.tempMovieId};
    			this.hCastList.put(key, entry);
        	} else {
        		String name;
        		String movieId;
        		if (this.tempName.isEmpty()) {
        			name = "null";
        		} else {
        			name = this.tempName;
        		} if (this.tempMovieId.isEmpty()) {
        			movieId = "null";
        		} else {
        			movieId = this.tempMovieId;
        		}
        		this.InvalidCastList.add("Name: " + name + "\tMovieId: " + movieId);
        	}

        } else if (qName.equalsIgnoreCase("f")) {
        	this.tempMovieId = tempVal.trim();
        } else if (qName.equalsIgnoreCase("a")) {
        	this.tempName = tempVal.trim();
        }
    }
	
	public static void main(String[] args) {
		castsxmlparser xmlp = new castsxmlparser();
		xmlp.runParser();
	}
}
