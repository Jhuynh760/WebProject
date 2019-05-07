import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class director {
	private String directorName;
	private List<film> filmList;
	private List<String> invalidFilms;
	
	private String type;
	
	private String tempTitle;
	private int tempYear;
	private String tempMovieId;
	private List<String> tempGenres;
	
	public director() {
		this.directorName = "";
		this.filmList = new ArrayList<film>();
		this.invalidFilms = new ArrayList<String>();
		this.tempGenres = new ArrayList<String>();
	}
	public director(String name, List<film> filmList, String type, List<String> invalidFilms, List<String> genres) {
		this.directorName = name;
		this.filmList = filmList;
		this.invalidFilms = invalidFilms;
		this.type = type;
		this.tempGenres = genres;;
	}
	
	public String getDirectorName() {
		return this.directorName;
	}
	
	public void setDirectorName(String directorName) {
		this.directorName = directorName;
	}
	
	public List<film> getFilmList(){
		return this.filmList;
	}
	
	public void setFilmList(List<film> filmList) {
		this.filmList = filmList;
	}
	
	public void addToFilmList(film filmEntry) {
		this.filmList.add(filmEntry);
	}
	
	public void addToFilmList(String title, int year, String movieId, List<String> genre) {
		film tempFilm = new film(title, year, movieId, genre);
		this.addToFilmList(tempFilm);
	}
	
	public List<String> getInvalidFilms(){
		return this.invalidFilms;
	}
	
	public void setInvalidFilms(List<String> invalidFilms) {
		this.invalidFilms = invalidFilms;
	}
	
	public void addToInvalidFilms(String invalidFilm) {
		this.invalidFilms.add(invalidFilm);
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getTempTitle() {
		return this.tempTitle;
	}
	
	public void setTempTitle(String tempTitle) {
		this.tempTitle = tempTitle;
	}
	
	public int getTempYear() {
		return this.tempYear;
	}
	
	public void setTempYear(int tempYear) {
		this.tempYear = tempYear;
	}
	
	public String getTempMovieId() {
		return this.tempMovieId;
	}
	
	public void setTempMovieId(String tempMovieId) {
		this.tempMovieId = tempMovieId;
	}
	
	public List<String> getTempGenres(){
		return this.tempGenres;
	}
	
	public void setTempGenres(List<String> tempGenres) {
		this.tempGenres = tempGenres;
	}
	
	public void addToTempGenres(String genre) {
		this.tempGenres.add(genre);
	}
	
	public void addFilm() {
		this.addToFilmList(this.tempTitle, this.tempYear, this.tempMovieId, this.tempGenres);
	}
	
	public String toString() {
		String outString = "Director: " + this.directorName;
		if (filmList.size()>0){
			outString += "\n\t-----------------FILMS-----------------";
			Iterator<film> it = filmList.iterator();
			while(it.hasNext()) {
				film tempFilm = it.next();
				outString += "\n\t\t" + tempFilm.toString();
			}
			outString += "\n\t-----------------FILMS-----------------";
		}
		
		if (invalidFilms.size() > 0) {
			outString += "\n\n\t-----------------INVALID FILMS-----------------";
			Iterator<String> invit = invalidFilms.iterator();
			while(invit.hasNext()) {
				String tempFilm = invit.next();
				outString += "\n\t\t" + tempFilm.toString();
			}
			outString += "\n\t-----------------INVALID FILMS-----------------";
		}

		return outString;
	}
}
