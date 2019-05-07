

public class star {
	private String name;
	private int birthYear;
	private String type;

	public star() {
		
	}
	
	public star(String name, int birthYear, String type) {
		this.name = name;
		this.birthYear = birthYear;
		this.type = type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getBirthYear() {
		return this.birthYear;
	}
	
	public void setBirthYear(int birthYear) {
		this.birthYear = birthYear;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String toString() {
		return "Name: " + this.name + "\tBirth Year: " + this.birthYear;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof star)) {
			return false;
		}
		star that = (star) other;
		if (this.name.equals(that.getName()) && this.birthYear == that.getBirthYear()) {
			return true;
		}
		return false;
	}
}
