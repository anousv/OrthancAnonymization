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
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class TableDataExportStudies extends AbstractTableModel{

	private static final long serialVersionUID = 1L;
	private String[] entetes = {"Patient name", "Patient ID", "Study date", "Study description", "Accession number", "ID"};
	private final Class<?>[] columnClasses = new Class<?>[] {String.class, String.class, Date.class, String.class, String.class, String.class};
	private ArrayList<Study> studies = new ArrayList<Study>();
	private ArrayList<String> ids = new ArrayList<String>();

	public TableDataExportStudies(){
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
		return studies.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return studies.get(rowIndex).getPatientName();
		case 1:
			return studies.get(rowIndex).getPatientID();
		case 2:
			return studies.get(rowIndex).getDate();
		case 3:
			return studies.get(rowIndex).getStudyDescription();
		case 4:
			return studies.get(rowIndex).getAccession();
		case 5:
			return studies.get(rowIndex).getId();
		default:
			return null; //Ne devrait jamais arriver
		}
	}

	public ArrayList<Study> getStudiesList(){
		return this.studies;
	}

	public void removeStudy(int rowIndex){
		this.studies.remove(rowIndex);
		this.ids.remove(rowIndex);
		fireTableRowsDeleted(rowIndex, rowIndex);
	}

	/*
	 * This method adds patient to the patients list, which will eventually be used by the JTable
	 */
	public void addStudy(String patientName, String patientID, String patientUID) throws IOException, ParseException{
		DateFormat parser = new SimpleDateFormat("yyyyMMdd");
		QueryFillStore queryID = new QueryFillStore("Studies", null, patientUID, null, null);
		QueryFillStore queryDate = new QueryFillStore("Studies", null, patientUID, null, null);
		QueryFillStore queryDescription = new QueryFillStore("Studies", null, patientUID, null, null);
		QueryFillStore queryAccession = new QueryFillStore("Studies", null, patientUID, null, null);
		QueryFillStore queryStudyInstanceUID = new QueryFillStore("Studies", null, patientUID, null, null);
		
		String[] id = queryID.extractData("id").split("SEPARATOR");
		String[] description = new String[id.length];
		String[] accession = new String[id.length];
		Date[] date = new Date[id.length];
		String[] studyInstanceUID = new String[id.length];

		String[] descBrut = queryDescription.extractData("description").split("SEPARATOR");
		for(int i = 0; i < descBrut.length; i++){
			description[i] = descBrut[i];
		}

		String[] dateBrut = queryDate.extractData("date").split("SEPARATOR");
		for(int i = 0; i < dateBrut.length; i++){
			if(dateBrut[i].length() > 1){
				date[i] = parser.parse(dateBrut[i]);
			}
		}

		String[] accessionBrut = queryAccession.extractData("accession").split("SEPARATOR");
		for(int i = 0; i < accessionBrut.length; i++){
			accession[i] = accessionBrut[i];
		}
		
		String[] studyInstanceUIDBrut = queryStudyInstanceUID.extractData("studyInstanceUID").split("SEPARATOR");
		for(int i = 0; i < studyInstanceUIDBrut.length; i++){
			studyInstanceUID[i] = studyInstanceUIDBrut[i];
		}

		for(int i = 0; i < id.length; i++){
			Study s = new Study(description[i], null, accession[i], id[i], null, patientName, patientID, studyInstanceUID[i]);
			if(date[i]!=null){
				s = new Study(description[i], date[i], accession[i], id[i], null, patientName, patientID, studyInstanceUID[i]);
			}
			if(!this.studies.contains(s)){
				this.studies.add(s);
				fireTableRowsInserted(studies.size() - 1, studies.size() - 1);
				this.ids.add(s.getId());
			}
		}
	}

	public void clearIdsList(){
		this.ids.removeAll(ids);
	}
	
	public ArrayList<String> getOrthancIds(){
		return this.ids;
	}

	/*
	 * This method clears the studies list
	 */
	public void clear(){
		if(this.getRowCount() !=0){
			for(int i = this.getRowCount(); i > 0; i--){
				this.removeStudy(i-1);
			}
		}
	}
}