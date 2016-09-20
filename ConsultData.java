/*
File: ConsultData.java
Justin Teare
*/

/** 
 * Support Class ConsultData
 * Stores the data fields for consultation entry
 */


import java.util.*;
import java.sql.*;

public class ConsultData {

	private java.sql.Date timeslot; //not sure if this is correct
	private String vet_ird;
	private int animal_id;
	private String description;

	//default constructor
	public ConsultData() { }

	/**sets the value of the data field timeslot to input parameter value*/
	public void setTimeslot(java.sql.Date timeslot){      
		this.timeslot = timeslot;      
	}//end method

	/**sets the value of vet_ird data field*/
	public void setVetIrd(String vet_ird){
		this.vet_ird = vet_ird;
		System.out.print("VET IRD: ");
		System.out.println(this.vet_ird);
	}//end method

	/**set the value of animal_id datafield*/
	public void setAnimalID (int Animal_id){
		this.animal_id = animal_id;
	}//end method

	/**set the value of description data field*/
	public void setDescription (String description){
		this.description = description;
	}
}
