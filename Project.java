
public class Project {
	
	// Attributes
	String projectNumber;
	String projectName;
	String buildingType;
	String projectAddress;
	String erfNumber;
	float projectFee;
	float amountPaid;
	String projectDeadline;
	
	// Constructor
	public Project(
			String projectNumber,
			String projectName,
			String buildingType,
			String projectAddress,
			String erfNumber,
			float projectFee,
			float amountPaid,
			String projectDeadline) {
		
		this.projectNumber = projectNumber;
		this.projectName = projectName;
		this.buildingType = buildingType;
		this.projectAddress = projectAddress;
		this.erfNumber = erfNumber;	
		this.projectFee = projectFee;
		this.amountPaid = amountPaid;
		this.projectDeadline = projectDeadline;
	}

	// toString method
	public String toString() {
		String output = "Proj Number\t: " + projectNumber;
		output += "\nProj Name\t: " + projectName;
		output += "\nBuilding Type\t: " + buildingType;
		output += "\nProj Address\t: " + projectAddress;
		output += "\nERF Number\t: " + erfNumber;
		output += "\nProj Fee\t: R" + projectFee;
		output += "\nAmount Paid\t: R" + amountPaid;
		output += "\nProj Deadline\t: " + projectDeadline;	
		return output;
	}
}
