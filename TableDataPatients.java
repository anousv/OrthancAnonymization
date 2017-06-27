/**
Copyright (C) 2017 VONGSALAT Anousone

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public v.3 License as published by
the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.imagej;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.table.AbstractTableModel;

public class TableDataPatients extends AbstractTableModel{
	private static final long serialVersionUID = 1L;

	private String[] entetes = {"Patient Name", "Patient ID", "ID", "Birthdate"};
	private ArrayList<Patient> patients = new ArrayList<Patient>();
	private final Class<?>[] columnClasses = new Class<?>[] {String.class, String.class, String.class, Date.class};

	public TableDataPatients(){
		super();
	}

	@Override
	public int getColumnCount() {
		return entetes.length;
	}

	@Override
	public String getColumnName(int columnIndex){
		return entetes[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int column){
		return columnClasses[column];
	}

	@Override
	public int getRowCount() {
		return patients.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return patients.get(rowIndex).getPatientName();
		case 1:
			return patients.get(rowIndex).getPatientId();
		case 2:
			return patients.get(rowIndex).getId();
		case 3:
			return patients.get(rowIndex).getBirthdate();
		default:
			return null; //Ne devrait jamais arriver
		}
	}

	public void removePatient(int rowIndex){
		this.patients.remove(rowIndex);
		fireTableRowsDeleted(rowIndex, rowIndex);
	}

	/*
	 * This method adds patient to the patients list, which will eventually be used by the JTable
	 */
	public void addPatient(String inputType, String input, String date, String studyDesc) throws IOException, ParseException{
		DateFormat parser = new SimpleDateFormat("yyyyMMdd");
		QueryFillStore queryName = new QueryFillStore("Patients", inputType, input, date, studyDesc);
		QueryFillStore queryPatientID = new QueryFillStore("Patients", inputType, input, date, studyDesc);
		QueryFillStore queryID = new QueryFillStore("Patients", inputType, input, date, studyDesc);
		QueryFillStore queryBirthDate = new QueryFillStore("Patients", inputType, input, date, studyDesc);
		String[] name = queryName.extractData("name").split("SEPARATOR");
		String[] patientID = queryPatientID.extractData("patientID").split("SEPARATOR");
		String[] id = queryID.extractData("id").split("SEPARATOR");
		String[] birthdateBrut = queryBirthDate.extractData("birthdate").split("SEPARATOR");
		
		Date[] birthdate = new Date[id.length]; 
		for(int i = 0; i < birthdateBrut.length; i++){
			if(birthdateBrut[i].length() > 1){
				birthdate[i] = parser.parse(birthdateBrut[i]);
			}
		}
		
		for(int i = 0; i < id.length; i++){
			Patient p = new Patient(name[i], patientID[i], id[i], null, null);
			if(birthdate[i] != null){
				p = new Patient(name[i], patientID[i], id[i], birthdate[i], null);
			}
			if(!this.patients.contains(p) && id[i].length() > 0){
				this.patients.add(p);
				fireTableRowsInserted(patients.size() - 1, patients.size() - 1);
			}
		}
	}
	
	/*
	 * This method clears the series list
	 */
	public void clear(){
		if(this.getRowCount() !=0){
			for(int i = this.getRowCount(); i > 0; i--){
				this.removePatient(i-1);
			}
		}
	}
}
