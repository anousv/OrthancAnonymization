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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;

public class TableDataExportSeries extends AbstractTableModel{
	private static final long serialVersionUID = 1L;

	private String[] entetes = {"Serie description*", "Modality", "Instances", "Secondary capture", "ID"};
	private ArrayList<Serie> series = new ArrayList<Serie>();
	private ArrayList<String> instancesWithSecondaryCapture = new ArrayList<String>();
	private String studyID = "";
	private String url;
	private String fullAddress;
	private String ip;
	private String port;
	private String authentication;
	private JFrame frame;
	private JLabel stateExport;
	private Preferences jprefer = Preferences.userRoot().node("<unnamed>/biplugins");
	private Preferences jpreferPerso = Preferences.userRoot().node("<unnamed>/queryplugin");

	public TableDataExportSeries(JFrame frame, JLabel stateExport){
		super();
		int curDb = jprefer.getInt("current database", 0);
		int typeDb = jprefer.getInt("db type" + curDb, 5);
		if(typeDb == 5){
			if(!jprefer.get("db path" + curDb, "none").equals("none")){
				String pathBrut = jprefer.get("db path" + curDb, "none") + "/";
				int index = ordinalIndexOf(pathBrut, "/", 3);
				this.fullAddress = pathBrut.substring(0, index);
			}else{
				String address = jprefer.get("ODBC" + curDb, "localhost");
				String pattern1 = "@";
				String pattern2 = ":";
				Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
				Matcher m = p.matcher(address);
				while(m.find()){
					this.ip = "http://" + m.group(1);
				}
			}
			if(jprefer.get("db user" + curDb, null) != null && jprefer.get("db pass" + curDb, null) != null){
				authentication = Base64.getEncoder().encodeToString((jprefer.get("db user" + curDb, null) + ":" + jprefer.get("db pass" + curDb, null)).getBytes());
			}
		}else{
			this.ip = jpreferPerso.get("ip", "http://localhost");
			this.port = jpreferPerso.get("port", "8042");
			if(jpreferPerso.get("username", null) != null && jpreferPerso.get("username", null) != null){
				authentication = Base64.getEncoder().encodeToString((jpreferPerso.get("username", null) + ":" + jpreferPerso.get("password", null)).getBytes());
			}
		}
		this.frame = frame;
		this.stateExport = stateExport;
	}

	@Override
	public int getColumnCount(){
		return entetes.length;
	}

	@Override
	public String getColumnName(int columnIndex){
		return entetes[columnIndex];
	}

	@Override
	public int getRowCount(){
		return series.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex){
		switch(columnIndex){
		case 0:
			return series.get(rowIndex).getSerieDescription();
		case 1:
			return series.get(rowIndex).getModality();
		case 2:
			return series.get(rowIndex).getNbInstances();
		case 3:
			return series.get(rowIndex).isSecondaryCapture();
		case 4:
			return series.get(rowIndex).getId();
		default:
			return null; //Ne devrait jamais arriver
		}
	}

	public boolean isCellEditable(int row, int col){
		if(col == 0){
			return true; 
		}
		return false;
	}
	
	public void setValueAt(Object value, int row, int col) {
		String uid = this.getValueAt(row, 4).toString();
		String oldDesc = this.getValueAt(row, 0).toString();
		if(!oldDesc.equals(value.toString()) && col == 0){
			series.get(row).setSerieDescription(value.toString());
			fireTableCellUpdated(row, col);
		}
		SwingWorker<Void,Void> worker = new SwingWorker<Void,Void>(){

			@Override
			protected Void doInBackground() {
				try {
					setUrl("series/" + uid + "/modify");
					URL url2 = new URL(url);
					HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
					conn.setDoOutput(true);
					conn.setRequestMethod("POST");
					if((fullAddress != null && fullAddress.contains("https")) || (ip != null && ip.contains("https"))){
						try{
							HttpsTrustModifier.Trust(conn);
						}catch (Exception e){
							throw new IOException("Cannot allow self-signed certificates");
						}
					}
					if(authentication != null){
						conn.setRequestProperty("Authorization", "Basic " + authentication);
					}
					stateExport.setText("<html>Modifying a serie description <font color='red'> <br>(Do not use the toolbox while the current operation is not done)</font></html>");
					frame.pack();
					OutputStream os = conn.getOutputStream();
					os.write(("{\"Replace\":{\"SeriesDescription\":\"" + value.toString() + "\"}}").getBytes());
					os.flush();
					conn.getResponseMessage();
					conn.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void done(){
				clear();
				try {
					addSerie(studyID);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
				stateExport.setText("<html><font color='green'>The description has been changed.</font></html>");
				frame.pack();
			}
		};
		if(!oldDesc.equals(value.toString()) && col == 0){
			worker.execute();
		}
	}

	
	public Serie getSerie(int rowIndex){
		return this.series.get(rowIndex);
	}

	public boolean checkSopClassUid(String instanceUid) throws IOException{
		if(instanceUid != null){
			this.setUrl("instances/" + instanceUid + "/metadata/SopClassUid");
			URL url = new URL(this.url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			if((fullAddress != null && fullAddress.contains("https")) || (ip != null && ip.contains("https"))){
				try{
					HttpsTrustModifier.Trust(conn);
				}catch (Exception e){
					throw new IOException("Cannot allow self-signed certificates");
				}
			}
			if(this.authentication != null){
				conn.setRequestProperty("Authorization", "Basic " + this.authentication);
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			StringBuilder sb = new StringBuilder();
			String output;
			while ((output = br.readLine()) != null) {
				sb.append(output);
			}
			conn.disconnect();

			ArrayList<String> sopClassUIDs = new ArrayList<String>();
			sopClassUIDs.add("1.2.840.10008.5.1.4.1.1.7");
			sopClassUIDs.add("1.2.840.10008.5.1.4.1.1.7.1");
			sopClassUIDs.add("1.2.840.10008.5.1.4.1.1.7.2");
			sopClassUIDs.add("1.2.840.10008.5.1.4.1.1.7.3");
			sopClassUIDs.add("1.2.840.10008.5.1.4.1.1.7.4");
			sopClassUIDs.add("1.2.840.10008.5.1.4.1.1.88.11");
			sopClassUIDs.add("1.2.840.10008.5.1.4.1.1.88.22");
			sopClassUIDs.add("1.2.840.10008.5.1.4.1.1.88.33");
			sopClassUIDs.add("1.2.840.10008.5.1.4.1.1.88.40");
			sopClassUIDs.add("1.2.840.10008.5.1.4.1.1.88.50");
			sopClassUIDs.add("1.2.840.10008.5.1.4.1.1.88.59");
			sopClassUIDs.add("1.2.840.10008.5.1.4.1.1.88.65");
			sopClassUIDs.add("1.2.840.10008.5.1.4.1.1.88.67");

			if(sopClassUIDs.contains(sb.toString())){
				this.instancesWithSecondaryCapture.add(instanceUid);
				return true;
			}
			return false;
		}
		return false;
	}

	public void removeAllSecondaryCaptures() throws IOException{
		ArrayList<Serie> seriesToRemove = new ArrayList<Serie>();
		for(Serie s : series){
			if(s.isSecondaryCapture()){
				this.setUrl("series/" + s.getId());
				URL url = new URL(this.url);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("DELETE");
				if((fullAddress != null && fullAddress.contains("https")) || (ip != null && ip.contains("https"))){
					try{
						HttpsTrustModifier.Trust(conn);
					}catch (Exception e){
						throw new IOException("Cannot allow self-signed certificates");
					}
				}
				if(this.authentication != null){
					conn.setRequestProperty("Authorization", "Basic " + this.authentication);
				}
				conn.getResponseMessage();
				conn.disconnect();
				seriesToRemove.add(s);
			}
		}
		for(Serie s : seriesToRemove){
			removeSerie(series.indexOf(s));
		}
	}

	public void removeSerie(int rowIndex){
		this.series.remove(rowIndex);
		fireTableRowsDeleted(rowIndex, rowIndex);
	}

	public ArrayList<Serie> getSeries(){
		return this.series;
	}
	
	public void addSerie(String studyID) throws IOException, ParseException{
		this.studyID = studyID;
		QueryFillStore queryID = new QueryFillStore("Series", null, studyID, null, null);
		QueryFillStore queryDescription = new QueryFillStore("Series", null, studyID, null, null);
		QueryFillStore queryModality = new QueryFillStore("Series", null, studyID, null, null);
		QueryFillStore queryNbInstances = new QueryFillStore("Series", null, studyID, null, null);
		QueryFillStore queryInstance = new QueryFillStore("Series", null, studyID, null, null);
		QueryFillStore queryStudy = new QueryFillStore("Series", null, studyID, null, null);
		String[] id = queryID.extractData("id").split("SEPARATOR");
		String[] description = new String[id.length];
		String[] modality = new String[id.length];
		String[] nbInstances = new String[id.length];
		String[] instance = new String[id.length];
		String[] study = new String[id.length];

		// We manage the null fields by skipping them
		String[] descBrut = queryDescription.extractData("description").split("SEPARATOR");
		for(int i = 0; i < descBrut.length; i++){
			description[i] = descBrut[i];
		}


		String[] modBrut = queryModality.extractData("modality").split("SEPARATOR");
		for(int i = 0; i < modBrut.length; i++){
			modality[i] = modBrut[i];
		}

		String[] nbInstBrut = queryNbInstances.extractData("nbInstances").split("SEPARATOR");
		for(int i = 0; i < nbInstBrut.length; i++){
			nbInstances[i] = nbInstBrut[i];
		}

		String[] instanceBrut = queryInstance.extractData("instance").split("SEPARATOR");
		for(int i = 0; i < instanceBrut.length; i++){
			instance[i] = instanceBrut[i];
		}

		String[] studyBrut = queryStudy.extractData("study").split("SEPARATOR");
		for(int i = 0; i < studyBrut.length; i++){
			study[i] = studyBrut[i];
		}		

		//checkSopClass will be set on click
		for(int i = 0; i < id.length; i++){
			Serie s = new Serie(description[i], modality[i], nbInstances[i], id[i], study[i], instance[i], this.checkSopClassUid(instance[i]));
			if(instancesWithSecondaryCapture.contains(instance[i])){
				s = new Serie(description[i], modality[i], nbInstances[i], id[i], study[i], instance[i], true);
			}
			if(!this.series.contains(s)){
				this.series.add(s);
				fireTableRowsInserted(series.size() - 1, series.size() - 1);
			}
		}

	}

	/*
	 * This method clears the series list
	 */
	public void clear(){
		if(this.getRowCount() !=0){
			for(int i = this.getRowCount(); i > 0; i--){
				this.removeSerie(i-1);
			}
		}
	}

	/*
	 * This method sets the url
	 */
	public void setUrl(String url) {
		if(this.fullAddress != null && !this.fullAddress.equals("none")){
			this.url = this.fullAddress + "/" + url;
		}else{
			if(this.ip != null && port != null){
				this.url = ip + ":" + port + "/" + url;
			}
		}
	}

	public int ordinalIndexOf(String str, String substr, int n) {
		int pos = str.indexOf(substr);
		while (--n > 0 && pos != -1)
			pos = str.indexOf(substr, pos + 1);
		return pos;
	}
}
