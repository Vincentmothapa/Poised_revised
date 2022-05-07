
public class Person {
	
	// Attributes
	String projectNumber;
	String name;
	String contactNumber;
	String contactEmail;
	String address;
	
	// Constructor
	public Person(
			String projectNumber,
			String name,
			String contactNumber,
			String contactEmail,
			String address) {
		
		this.projectNumber = projectNumber;
		this.name = name;
		this.contactNumber = contactNumber;
		this.contactEmail = contactEmail;
		this.address = address;		
	}
	
	// toString method
	public String toString() {
		String output = "Project Number\t: " + projectNumber;
		output += "\nName\t\t: " + name;
		output += "\nContact Number\t: " + contactNumber;
		output += "\nContact Email\t: " + contactEmail;
		output += "\nAddress\t\t: " + address;		
		return output;		
	}
	
}
