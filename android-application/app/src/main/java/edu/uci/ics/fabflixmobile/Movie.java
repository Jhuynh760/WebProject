package edu.uci.ics.fabflixmobile;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

public class Movie {
    String movieId;
    String movieTitle;
    String movieYear;
    String movieDir;
    String movieRating;
    ArrayList<String> movieGenres;
    HashMap<String, String> movieStars;

    public Movie(){
        this.movieId = "";
        this.movieTitle = "";
        this.movieYear = "";
        this.movieDir = "";
        this.movieRating = "";
        this.movieGenres = new ArrayList<>();
        this.movieStars = new HashMap<>();
    }

    public Movie(String movieId, String movieTitle, String movieYear, String movieDir, String movieRating){
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.movieYear = movieYear;
        this.movieDir = movieDir;
        this.movieRating = movieRating;
        this.movieGenres = new ArrayList<>();
        this.movieStars = new HashMap<>();
    }

    public Movie(String movieId, String movieTitle, String movieYear, String movieDir, String movieRating, ArrayList<String> movieGenres, HashMap<String, String> movieStars){
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.movieYear = movieYear;
        this.movieDir = movieDir;
        this.movieRating = movieRating;
        this.movieGenres = movieGenres;
        this.movieStars = movieStars;
    }

    public void setMovieId(String movieId){
        this.movieId = movieId;
    }

    public String getMovieId(){
        return this.movieId;
    }

    public void setMovieTitle(String movieTitle){
        this.movieTitle = movieTitle;
    }

    public String getMovieTitle(){
        return this.movieTitle;
    }

    public void setMovieYear(String movieYear){
        this.movieYear = movieYear;
    }

    public String getMovieYear(){
        return this.movieYear;
    }

    public void setMovieDir(String movieDir){
        this.movieDir = movieDir;
    }

    public String getMovieDir(){
        return this.movieDir;
    }

    public void setMovieRating(String movieRating){
        this.movieRating = movieRating;
    }

    public String getMovieRating(){
        return this.movieRating;
    }

    public void setMovieGenres(ArrayList<String> movieGenres){
        this.movieGenres = movieGenres;
    }

    public ArrayList<String> getMovieGenres(){
        return this.movieGenres;
    }

    public void addMovieGenre(String movieGenre){
        this.movieGenres.add(movieGenre);
    }

    public void setMovieStars(HashMap<String, String> movieStars){
        this.movieStars = movieStars;
    }

    public HashMap<String, String> getMovieStars(){
        return this.movieStars;
    }

    public void addMovieStar(String starName, String starId){
        this.movieStars.put(starName, starId);
    }

    @Override
    public String toString(){
        String output = "";
        output += "ID: " + this.getMovieId() + "\tRating: " + this.getMovieRating() + "\n";
        output += "\tTitle: " + this.getMovieTitle() + "\n";
        output += "\tDirector: " + this.getMovieDir() + "\n";
        output += "\tYear: " + this.getMovieYear();
        if (this.getMovieGenres().size() > 0){
            output += "\n\tGenres: ";
            for (String genre: this.getMovieGenres()){
                output += "\n\t\t" + genre;
            }
        } if (this.movieStars.size() > 0){
            output += "\n\tStars: ";
            for (String key : this.movieStars.keySet()){
                output += "\n\t\tSID: " + this.movieStars.get(key) + "\tSName: " + key;
            }

        }
        return output;
    }
}
