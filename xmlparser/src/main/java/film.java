import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class film {
	private String title;
	private int year;
	private String movieId;
	private List<String> genres;
	
	public film(){
		this.title = "";
		this.year = -1;
		this.movieId = "";
		this.genres = new ArrayList<String>();
	}
	public film(String title, int year, String movieId, List<String> genres){
		this.title = title;
		this.year = year;
		this.movieId = movieId;
		this.genres = genres;
	}
	
	public film(String title, int year, String movieId) {
		this.title = title;
		this.year = year;
		this.movieId = movieId;
		this.genres = new ArrayList<String>();
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public int getYear() {
		return this.year;
	}
	
	public void setYear(int year){
		this.year = year;
	}
	
	public String getMovieId() {
		return this.movieId;
	}
	
	public void setMovieId(String movieId) {
		this.movieId = movieId;
	}
	
	public List<String> getGenres(){
		return this.genres;
	}
	
	public void setGenres(List<String> genres) {
		this.genres = genres;
	}
	
	public void addGenre(String genre) {
		this.genres.add(genre);
	}
	
	public String toString() {
		String out = "MovieId: " + this.movieId + "\tTitle: " + this.title + "\tYear: " + this.year;
		if (this.genres.size()>0) {
			out += "\n\t\t\tGenres: ";
			Iterator<String> genreIt = this.genres.iterator();
			while (genreIt.hasNext()) {
				out += "\n\t\t\t\t" + genreIt.next();
			}
		}
		return out;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof cast)) {
			return false;
		}
		film that = (film) other;
		if (this.title.equals(that.getTitle()) && this.year == that.getYear() && this.movieId.equals(that.getMovieId())) {
			return true;
		}
		return false;
	}
}
