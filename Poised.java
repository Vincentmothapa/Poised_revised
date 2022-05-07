import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.sql.*;

/** 
 * <h1>Building Project Management System</h1>

This is a project management program that is to be used by a company called Poised to keep track of their 
various building projects. This program was done as part of the Software Engineering Bootcamp requirements 
by HyperionDev. The program takes in information as specified below.

<h2>Project Information</h2>
 
<li>Project name and number</li>
<li>Type of building</li>
<li>Project address</li>
<li>ERF number</li>
<li>Total fee and amount paid to date</li>
<li>The deadline</li>

<h2>Customer, Structural Engineer, Project Manager and Architect Information</h2>
 
<li>Name and Telephone number</li>
<li>Email address</li>
<li>Physical address</li>
 
<h2>Functionality</h2>

The program consists of two object: Person and Project. Project information is stored in two tables on the database, namely 
project_information and finalized_projects and each person's information is stored in it's own table in the database. 
The functionality is summarized below:
 
<li>Capture new project information.</li>
<li>Update information about existing projects - Only the deadline and amount paid can be edited.</li>
<li>Finalize existing projects and generate an invoice - The invoice is generated if there is payment is still due.</li>
<li>See projects that are still in progress - You can see all projects or choose to see an individual project's information</li>
<li>See projects that are past their due date</li>
<li>See projects that have been finalized</li>
 
*/

public class Poised {
	/**
	 * Main Method
	 * @param args - String array arguments
	 */
	public static void main(String[] args) {
		try {
			// Connect to the library_db database, via the jdbc:mysql:channel on
			// localhost (this PC).
			// Use username "otheruser", password "swordfish".
			Connection connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/poisepms?useSSL=false",
				"otheruser", "swordfish"
				);
			// Create a direct line to the database for running our queries
			Statement statement = connection.createStatement();
			
			// MENU
			System.out.println("MAIN MENU");
			System.out.println("a - Add New Project");
			System.out.println("b - Get Project Information");
			System.out.println("c - Get Contact Information");
			System.out.println("d - See All Projects");
			System.out.println("e - Finalize Project");
			System.out.println("f - Edit Project Information");
			System.out.println("g - Edit Contact Information");
	
			//Get user preference
			Scanner object = new Scanner(System.in);
			System.out.print("\nEnter option: ");
			String userInput = object.nextLine();
			
			// If user wants to add a new project
			if (userInput.equalsIgnoreCase("a")) {
				System.out.println("\nYou have chosen to add a new project");
				addProject(statement, object);
			}

			// If user wants to get project information
			// Only projects not yet finalized are searched
			else if (userInput.equalsIgnoreCase("b")) {
				System.out.println("\nYou have chosen to get project information");
				getProjectInformation(statement, object);
			}
		
			// If user wants to get contact information
			else if (userInput.equalsIgnoreCase("c")) {
				System.out.println("\nYou have chosen to get contact information");
				System.out.print("Enter their project number: ");
				String projectNumber = object.nextLine();
				getContactInfo(projectNumber, "customer");
				getContactInfo(projectNumber, "architect");
				getContactInfo(projectNumber, "project_manager");
				getContactInfo(projectNumber, "structural_engineer");
			}
			
			// If user wants to see all projects
			else if (userInput.equalsIgnoreCase("d")) {
				System.out.println("\nYou have chosen to see projects\n");
				seeProjects(statement, object);
			}

			// If user wants to finalize project
			else if (userInput.equalsIgnoreCase("e")) {
				finalizeProject(object);
			}

			// If user wants to edit project
			else if (userInput.equalsIgnoreCase("f")) {
				editProject(statement, object);
			}

			// If user wants to edit a contact
			else if (userInput.equalsIgnoreCase("g")) {
				System.out.println("\nYou have chosen to edit a contact");
				editContact(statement, object);
			}

			// If the user input is not one of the defined options
			else {
				System.out.println("\nInvalid selection");
				object.close();
				return;
			}
			object.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
//####################################################################################################
	// METHODS
	
	/**
	 * This finalizes a specified project and returns an invoice if the customer still owes.
	 * The information regarding the project is moved from project_information to 
	 * finalized_projects and the date on which it is finalized is added.
	 * 
	 * @param object User input when selecting method.
	 */
	// Method to finalize project
	private static void finalizeProject(Scanner object) {
		System.out.println("\nYou have chosen to finalize a project");
		System.out.print("Enter the project number: ");
		String projectNumber = object.nextLine();
		
		boolean foundProject = false;
		
		LocalDate now = LocalDate.now();
		String formattedDate = now.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
		
		try {
			// Connect to the library_db database, via the jdbc:mysql:channel on
			// localhost (this PC).
			// Use username "otheruser", password "swordfish".
			Connection connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/poisepms?useSSL=false",
				"otheruser", "swordfish"
				);
			// Create a direct line to the database for running our queries
			Statement statement = connection.createStatement();

			ResultSet results = statement.executeQuery("SELECT * FROM project_information");
			while (results.next()) {
				if (projectNumber.equals(results.getString("project_number"))) {
					foundProject = true;
					
					// Calculate money owed
					float moneyOwed = results.getFloat("project_fee") - results.getFloat("amount_paid");
					
					// Print invoice if there is money owed
					if (moneyOwed > 0) {
						getContactInfo(projectNumber, "customer");
						
						System.out.println(
							"\nProject Number\t: " + results.getInt("project_number") +
							"\nProject Name\t: " + results.getString("project_name") +
							"\nBuilding type\t: " + results.getString("building_type") +
							"\nProject Address : " + results.getString("project_address") +
							"\nERF Number\t: " + results.getString("erf_number") +
							"\nProject Fee\t: R" + results.getString("project_fee") +
							"\nAmount Paid\t: R" + results.getString("amount_paid") +
							"\nProject Deadline: " + results.getString("project_deadline") +
							"\nDate Finalized\t: " + formattedDate +
							"\n\nAMOUNT DUE\t: R" + moneyOwed
							);
					} else {
						System.out.println("\nProject Finalized!");
					}
					
					// Add to finalized_projects and delete from project_information
					String temp = results.getInt("project_number") + ", '" +
							results.getString("project_name") + "', '" +
							results.getString("building_type") + "', '" +
							results.getString("project_address") + "', '" +
							results.getString("erf_number") + "', '" +
							results.getString("project_fee") + "', '" +
							results.getString("amount_paid") + "', '" +
							results.getString("project_deadline") +  "', '" +
							formattedDate + "'";
					
					String temp2 = "INSERT INTO finalized_projects VALUES (" + temp + ")";
					statement.executeUpdate(temp2);
					statement.executeUpdate("DELETE FROM project_information WHERE project_number ='"+projectNumber+"'");
					break;
				}
			}
			
			if (foundProject == false) {
				System.out.println("\nProject not found");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}	

//####################################################################################################	
	/**
	 * This prints the contact information for all the persons involved in the specified project.
	 * 
	 * @param projectNumber The unique project number.
	 * @return foundContact Tracks whether the project is found or not.
	 */
	private static void getContactInfo(String projectNumber, String tableName) {
		try {
			// Connect to the library_db database, via the jdbc:mysql:channel on
			// localhost (this PC).
			// Use username "otheruser", password "swordfish".
			Connection connection = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/poisepms?useSSL=false",
				"otheruser", "swordfish"
				);
			// Create a direct line to the database for running our queries
			Statement statement = connection.createStatement();
			boolean foundProject = false;
			
			String temp = "SELECT * FROM " + tableName;
			ResultSet results = statement.executeQuery(temp);
			while (results.next()) {
				if (projectNumber.equals(results.getString("project_number"))) {
					System.out.println("\n" + tableName);
					foundProject = true;
					System.out.println(
						"Project Number\t: " + results.getInt("project_number") +
						"\nName\t\t: " + results.getString("name") +
						"\nNumber\t\t: " + results.getString("contact_number") +
						"\nEmail\t\t: " + results.getString("contact_email") +
						"\nAddress\t\t: " + results.getString("address")
						);
					break;
				}
			}

	        // If project number is not found
	        if (foundProject == false) {
		    	System.out.println("\nThere is no project " + projectNumber);
	        }
	        
		} catch (SQLException e) {
		      e.printStackTrace();
		}
	}

//####################################################################################################
	/**
	 * This method allows the user to see all projects.
	 * The user can choose between finalized or overdue projects or projects not yet finalized.
	 * @param statement
	 * @param object
	 * @throws SQLException
	 */
	private static void seeProjects(Statement statement, Scanner object) throws SQLException {
		System.out.println("OPTIONS:");
		System.out.println("a - Finalized Projects");
		System.out.println("b - Projects not yet finalized");
		System.out.println("c - Overdue Projects\n");
		System.out.print("Enter selection: ");
		String option = object.nextLine();
		
		// Finalized Projects
		if (option.equalsIgnoreCase("a")) {
			System.out.println("Finalized Projects:");
			ResultSet results = statement.executeQuery("SELECT * FROM finalized_projects");
			while (results.next()) {
				System.out.println(
					"\nProject Number\t: " + results.getInt("project_number") +
					"\nProject Name\t: " + results.getString("project_name") +
					"\nBuilding type\t: " + results.getString("building_type") +
					"\nProject Address : " + results.getString("project_address") +
					"\nERF Number\t: " + results.getString("erf_number") +
					"\nProject Fee\t: R" + results.getString("project_fee") +
					"\nAmount Paid\t: R" + results.getString("amount_paid") +
					"\nProject Deadline: " + results.getString("project_deadline") + 
					"\nDate Finalized\t: " + results.getString("date_finalized")
					);
				}
			System.exit(0);
		}
		
		// Projects not yet finalized
		if (option.equalsIgnoreCase("b")) {
			System.out.println("Projects not yet finalized:");
			ResultSet results = statement.executeQuery("SELECT * FROM project_information");
			while (results.next()) {
				System.out.println(
					"\nProject Number\t: " + results.getInt("project_number") +
					"\nProject Name\t: " + results.getString("project_name") +
					"\nBuilding type\t: " + results.getString("building_type") +
					"\nProject Address : " + results.getString("project_address") +
					"\nERF Number\t: " + results.getString("erf_number") +
					"\nProject Fee\t: R" + results.getString("project_fee") +
					"\nAmount Paid\t: R" + results.getString("amount_paid") +
					"\nProject Deadline: " + results.getString("project_deadline")
					);
				}
			System.exit(0);
		}
		
		// Projects Overdue.
		// This will require the date today
		if (option.equalsIgnoreCase("c")) {
			System.out.println("Overdue Projects");
			boolean foundProject = false;
			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d MMM uuuu");
			LocalDate now = LocalDate.now();
			
			ResultSet results = statement.executeQuery("SELECT * FROM project_information");
			while (results.next()) {
				if (
					LocalDate.parse(results.getString("project_deadline"), dateFormat).isBefore(now)
					) {
					foundProject = true;
					System.out.println(
							"\nProject Number\t: " + results.getInt("project_number") +
							"\nProject Name\t: " + results.getString("project_name") +
							"\nBuilding type\t: " + results.getString("building_type") +
							"\nProject Address : " + results.getString("project_address") +
							"\nERF Number\t: " + results.getString("erf_number") +
							"\nProject Fee\t: R" + results.getString("project_fee") +
							"\nAmount Paid\t: R" + results.getString("amount_paid") +
							"\nProject Deadline: " + results.getString("project_deadline")
							);
				}
			}
			
			// Check if project is found
			if (foundProject == false) {
				System.out.println("\nThere are no overdue projects!");
			}
		}
		
		else {
			System.out.println("Invalid project type selection");
			object.close();
			return;
		}
	}
	
//####################################################################################################
	/**
	 * Method to edit a project.
	 * Only the due date or amount paid for projects not yet finalized can be edited.
	 * @param statement
	 * @param object
	 */
	private static void editProject(Statement statement, Scanner object) {
		System.out.println("\nYou have chosen to edit a project");
		System.out.print("Enter the project number: ");
		String projectNumber = object.nextLine();
		
		// Display options for editing
		System.out.println("OPTIONS:");
		System.out.println("a - Due Date");
		System.out.println("b - Amount Paid\n");
		System.out.print("Enter selection: ");
		String option = object.nextLine();
		boolean foundProject = false;
		
		// EDIT DUE DATE
		if (option.equalsIgnoreCase("a")) {
			try {				
				ResultSet results = statement.executeQuery("SELECT * FROM project_information");
				
				// Loop over the results, printing them all.
				while (results.next()) {
					if (projectNumber.equals(results.getString("project_number"))) {
						foundProject = true;
						System.out.print("\nEnter new project deadline (dd mmm yyyy): ");
		        		String newDueDate = object.nextLine();
		        		
		        		// Check that date is in the correct format
		    			DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d MMM uuuu");
		    			try {
		    				LocalDate.parse(newDueDate, dateFormat);
		    			} catch (java.time.format.DateTimeParseException e) {
		    				System.out.println("The date entered is not in the correct format");
		    				return;
		    			}
		    			
		    			statement.executeUpdate("UPDATE project_information SET project_deadline = '"+newDueDate+
		    					"' where project_number = '"+projectNumber+"'");
		    			System.out.println("\nDone!");
		    			break;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		else if(option.equalsIgnoreCase("b")) {
			try {				
				ResultSet results = statement.executeQuery("SELECT * FROM project_information");
				
				// Loop over the results, printing them all.
				while (results.next()) {
					if (projectNumber.equals(results.getString("project_number"))) {
						foundProject = true;
			    		System.out.print("\nEnter new amount: ");
			    		float amountPaid = 0;
			    		
			    		// Check that the amount paid is a valid number
			    		// This will be used for calculations
						try{
							amountPaid = object.nextFloat();
						} catch (InputMismatchException ex) {
							System.out.println("Number not valid.");
							return;
						}
		    			
		    			statement.executeUpdate("UPDATE project_information SET amount_paid = '"+amountPaid+
		    					"' where project_number = '"+projectNumber+"'");
		    			System.out.println("\nDone!");
		    			break;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if (foundProject == false) {
			System.out.println("\nProject not found.");
			return;
		}
	}

//####################################################################################################
	/**
	 * Method to edit a contact person's number or email address.
	 * @param statement
	 * @param object
	 */
	private static void editContact(Statement statement, Scanner object) {
		System.out.print("Enter their project number: ");
		String projectNumber = object.nextLine();
		
		System.out.println("\nChoose contact type");
		System.out.println("1 - Customer");
		System.out.println("2 - Architect");
		System.out.println("3 - Project Manager");
		System.out.println("4 - Structural Engineer");
		System.out.print("\nChoose contact to edit: ");
		int contactType = object.nextInt();
		object.nextLine();
		
		System.out.println("OPTIONS:");
		System.out.println("a - Contact Number");
		System.out.println("b - Email Address\n");
		System.out.print("Enter selection: ");
		String option = object.nextLine();
		
		boolean foundContact = false;
		/*
		 * PLEASE NOTE
		 * The table being iterated through is the customer. It has the same columns and
		 * project numbers as the other contacts' tables. This will not pose a problem
		 * because when a project is added a customer, project manager, structural engineer and
		 * architect are assigned to it. They all have the same project number.
		 */
		try {
			// edit contact phone number
			if (option.equalsIgnoreCase("a")) {
				ResultSet results = statement.executeQuery("SELECT * FROM customer");
				while (results.next()) {
					if (contactType == 1 && projectNumber.equals(results.getString("project_number"))) {
						foundContact = true;
						System.out.println("Enter new customer contact number: ");
						String newContactNumber = object.nextLine();
						statement.executeUpdate("UPDATE customer SET contact_number = '"+newContactNumber+
								"' WHERE project_number = '"+projectNumber+"'");
						System.out.println("\nDone!");
						System.exit(0);
						
					} else if (contactType == 2 && projectNumber.equals(results.getString("project_number"))) {
						foundContact = true;
						System.out.println("Enter new architect contact number: ");
						String newContactNumber = object.nextLine();
						statement.executeUpdate("UPDATE architect SET contact_number = '"+newContactNumber+
								"' WHERE project_number = '"+projectNumber+"'");
						System.out.println("\nDone!");
						System.exit(0);
						
					} else if (contactType == 3 && projectNumber.equals(results.getString("project_number"))) {
						foundContact = true;
						System.out.println("Enter new project manager contact number: ");
						String newContactNumber = object.nextLine();
						statement.executeUpdate("UPDATE project_manager SET contact_number = '"+newContactNumber+
								"' WHERE project_number = '"+projectNumber+"'");
						System.out.println("\nDone!");
						System.exit(0);
						
					} else if (contactType == 4 && projectNumber.equals(results.getString("project_number"))) {
						foundContact = true;
						System.out.println("Enter new structural engineer contact number: ");
						String newContactNumber = object.nextLine();
						statement.executeUpdate("UPDATE structural_engineer SET contact_number = '"+newContactNumber
								+"' WHERE project_number = '"+projectNumber+"'");
						System.out.println("\nDone!");
						System.exit(0);	
					}
				}
			}
			
			// edit contact email
			if (option.equalsIgnoreCase("b")) {
				ResultSet results = statement.executeQuery("SELECT * FROM customer");
				while (results.next()) {
					if (contactType == 1 && projectNumber.equals(results.getString("project_number"))) {
						foundContact = true;
						System.out.println("Enter new customer email address: ");
						String newContactEmail = object.nextLine();
						statement.executeUpdate("UPDATE customer SET contact_email = '"+newContactEmail+
								"' WHERE project_number = '"+projectNumber+"'");
						System.out.println("\nDone!");
						System.exit(0);
						
					} else if (contactType == 2 && projectNumber.equals(results.getString("project_number"))) {
						foundContact = true;
						System.out.println("Enter new architect email address: ");
						String newContactEmail = object.nextLine();
						statement.executeUpdate("UPDATE architect SET contact_email = '"+newContactEmail+
								"' WHERE project_number = '"+projectNumber+"'");
						System.out.println("\nDone!");
						System.exit(0);
						
					} else if (contactType == 3 && projectNumber.equals(results.getString("project_number"))) {
						foundContact = true;
						System.out.println("Enter new project manager email address: ");
						String newContactEmail = object.nextLine();
						statement.executeUpdate("UPDATE project_manager SET contact_email = '"+newContactEmail+
								"' WHERE project_number = '"+projectNumber+"'");
						System.out.println("\nDone!");
						System.exit(0);
						
					} else if (contactType == 4 && projectNumber.equals(results.getString("project_number"))) {
						foundContact = true;
						System.out.println("Enter new structural engineer email address: ");
						String newContactEmail = object.nextLine();
						statement.executeUpdate("UPDATE structural_engineer SET contact_email = '"+newContactEmail
								+"' WHERE project_number = '"+projectNumber+"'");
						System.out.println("\nDone!");
						System.exit(0);	
					}
				}
			}
			
			
			if (foundContact == false) {
				System.out.println("\nContact with project number " + projectNumber + " not found.");
				System.exit(0);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
//####################################################################################################
	/**
	 * Method to get a specific project's information.
	 * 
	 * @param statement
	 * @param object
	 * @throws SQLException
	 */
	private static void getProjectInformation(Statement statement, Scanner object) throws SQLException {
		System.out.print("Enter the project name or number: ");
		String projectNumber = object.nextLine();
		boolean foundProject = false;
		
		ResultSet results = statement.executeQuery("SELECT * FROM project_information");
		while (results.next()) {
			if (projectNumber.equals(results.getString("project_number")) ||
					projectNumber.equalsIgnoreCase(results.getString("project_name"))) {
				
				foundProject = true;
				System.out.println(
					"\nProject Number\t: " + results.getInt("project_number") +
					"\nProject Name\t: " + results.getString("project_name") +
					"\nBuilding type\t: " + results.getString("building_type") +
					"\nProject Address : " + results.getString("project_address") +
					"\nERF Number\t: " + results.getString("erf_number") +
					"\nProject Fee\t: R" + results.getString("project_fee") +
					"\nAmount Paid\t: R" + results.getString("amount_paid") +
					"\nProject Deadline: " + results.getString("project_deadline")
					);
			}
		}

		// If project number is not found
		if (foundProject == false) {
			System.out.println("\nThere is no project " + projectNumber);
		}
		object.close();
	}
	
//####################################################################################################
	/**
	 * Method to add new project.
	 * This includes adding all contact information for the person's involved in this project
	 * 
	 * @param statement
	 * @param object
	 * @throws SQLException
	 */
	private static void addProject(Statement statement, Scanner object) throws SQLException {
		System.out.print("Project Number: ");
		String projectNumber = object.nextLine();
		
		System.out.print("Project Name: ");
		String projectName = object.nextLine();						
		
		System.out.print("Building Type: ");
		String buildingType = object.nextLine();
		
		// If project name is empty, substitute with building type and surname
		if (projectName.equals("")) {
			System.out.print("Enter client surname: ");
			String temp = object.nextLine();
			projectName = buildingType + " " + temp;
		}
		
		System.out.print("Project Address: ");
		String projectAddress = object.nextLine();
		
		System.out.print("ERF Number: ");
		String erfNumber = object.nextLine();
		
		System.out.print("Project Deadline (dd mmm yyyy): ");
		String projectDeadline = object.nextLine();
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d MMM uuuu");
		try {
			LocalDate.parse(projectDeadline, dateFormat);
		} catch (java.time.format.DateTimeParseException e) {
			System.out.println("The date entered is not in the correct format");
			object.close();
			return;
		}
		
		// Ensure that project fee and amount paid below are actual numbers.
		// It will be used in calculations later.
		System.out.print("Project Fee: ");
		float projectFee = 0;
		try{
			projectFee = object.nextFloat();
		} catch (InputMismatchException ex) {
			System.out.println("Number not valid");
			object.close();
			return;
		}
		
		System.out.print("Amount Paid: ");
		float amountPaid = 0;
		try{
			amountPaid = object.nextFloat();
			object.nextLine();
		} catch (InputMismatchException ex) {
			System.out.println("Number not valid.");
			object.close();
			return;
		}
		
		// Create Project variable 
		Project projectDetails = new Project(projectNumber,
								  projectName,
								  buildingType,
								  projectAddress,
								  erfNumber,
								  projectFee,
								  amountPaid,
								  projectDeadline);
		
		// Display the information to be saved
		System.out.println("\n" + projectDetails);
		String temp = "INSERT INTO project_information values (" + projectNumber + ", '" + 
				projectName + "', '" + buildingType + "', '" + projectAddress + "', '" + erfNumber + "', " +
				projectFee + ", " + amountPaid + ", '" + projectDeadline + "')";
				
		// Add project information to table
		statement.executeUpdate(temp);

		// CREATE CONTACTS
		// customer
		System.out.println("\nAdd customer details");
		addContact(statement, object, projectNumber, "customer");
		
		// architect
		System.out.println("\nAdd architect details");
		addContact(statement, object, projectNumber, "architect");
		
		// project manager
		System.out.println("\nAdd project manager details");
		addContact(statement, object, projectNumber, "project_manager");
		
		// structural engineer
		System.out.println("\nAdd structural engineer details");
		addContact(statement, object, projectNumber, "structural_engineer");

		System.out.println("Project Successfully Added!");
	}
	
//####################################################################################################
	/**
	 * Method to add contact information.
	 * This methods works together with the addProject method
	 * 
	 * @param statement
	 * @param object
	 * @param projectNumber
	 * @throws SQLException
	 */
	private static void addContact(Statement statement, Scanner object, String projectNumber, String tableName) throws SQLException {
		System.out.print("Enter Full Name: ");
		String name = object.nextLine();
		
		System.out.print("Enter Contact Number: ");
		String contactNumber = object.nextLine();
		
		System.out.print("Enter Contact Email: ");
		String contactEmail = object.nextLine();
		
		System.out.print("Enter Address: ");
		String address = object.nextLine();
		
		Person personDetails = new Person(
				projectNumber,
				name,
				contactNumber,
				contactEmail,
				address);		
		
		System.out.println("\n" + personDetails);
		String temp = "INSERT INTO " + tableName + " values (" + projectNumber + ", '" + 
				name + "', '" + contactNumber + "', '" + contactEmail + "', '" + address + "')";
		
		// Add to table
		statement.executeUpdate(temp);
	}	

//####################################################################################################	
}
