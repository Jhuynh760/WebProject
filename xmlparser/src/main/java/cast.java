
public class cast {
	private String name;
	private String movieId;
	private String type;
	
	public cast() {
		this.name = "";
		this.movieId = "";
	}
	
	public cast(String name, String movieId, String type) {
		this.name = name;
		this.movieId = movieId;
		this.type = type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getMovieId() {
		return this.movieId;
	}
	
	public void setMovieId(String movieId) {
		this.movieId = movieId;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String toString() {
		return "Name: " + this.name + "\tMovieId: " + movieId;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof cast)) {
			return false;
		}
		cast that = (cast) other;
		if (this.name.equals(that.getName()) && this.movieId.equals(that.getMovieId())) {
			return true;
		}
		return false;
	}
}
