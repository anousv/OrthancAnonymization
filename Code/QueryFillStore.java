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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ij.IJ;

public class QueryFillStore {

	private String url;
	private String fullAddress;
	private String ip;
	private String port;
	private Preferences jprefer = Preferences.userRoot().node("<unnamed>/biplugins");
	private Preferences jpreferPerso = Preferences.userRoot().node("<unnamed>/queryplugin");
	private String query;
	private String level;
	private String input;
	private String authentication;
	private StringBuilder response = new StringBuilder();
	private ArrayList<String> ids = new ArrayList<String>();
	private String toolboxListContent;

	public QueryFillStore(){
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
			if(jpreferPerso.get("username", null) != null && jpreferPerso.get("password", null) != null){
				authentication = Base64.getEncoder().encodeToString((jpreferPerso.get("username", null) + ":" + jpreferPerso.get("password", null)).getBytes());
			}
		}
		this.setUrl("/tools/find");
	}
	
	public QueryFillStore(String level, String inputType, String input, 
			String date, String studyDesc) throws MalformedURLException{
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
		this.setUrl("/tools/find");
		this.level = level;
		this.input = input;
		this.query = "{ \"Level\" : \"" + "Patients" + "\","
				+ "\"Query\" : {\"PatientName\" : \"" + input +"\"}"
				+ "}";
		if(level != null && level.equals("Patients")){
			switch (inputType) {
			case "Patient name":
				if(studyDesc.equals("*")){
					this.query = "{ \"Level\" : \"" + "Patients" + "\","
							+ "\"Query\" : {\"PatientID\" : \"" + "*" +"\", "
							+ "\"PatientName\" : \"" + input +"\","
							+ "\"StudyDate\" : \"" + date +"\","
							+ "\"AccessionNumber\" : \"" + "*" +"\" }"
							+ "}";
				}else{
					this.query = "{ \"Level\" : \"" + "Patients" + "\","
							+ "\"Query\" : {\"PatientID\" : \"" + "*" +"\", "
							+ "\"PatientName\" : \"" + input +"\","
							+ "\"StudyDate\" : \"" + date +"\","
							+ "\"StudyDescription\" : \"" + studyDesc + "\","
							+ "\"AccessionNumber\" : \"" + "*" +"\" }"
							+ "}";
				}
				break;
			case "Accession number":
				if(studyDesc.equals("*")){
					this.query = "{ \"Level\" : \"" + "Patients" + "\","
							+ "\"Query\" : {\"PatientID\" : \"" + "*" +"\", "
							+ "\"PatientName\" : \"" + "*" +"\","
							+ "\"StudyDate\" : \"" + date +"\","
							+ "\"AccessionNumber\" : \"" + input +"\" }"
							+ "}";
				}else{
					this.query = "{ \"Level\" : \"" + "Patients" + "\","
							+ "\"Query\" : {\"PatientID\" : \"" + "*" +"\", "
							+ "\"PatientName\" : \"" + "*" +"\","
							+ "\"StudyDate\" : \"" + date +"\","
							+ "\"StudyDescription\" : \"" + studyDesc + "\","
							+ "\"AccessionNumber\" : \"" + input +"\" }"
							+ "}";
				}
				break;
			case "Patient ID":
				if(studyDesc.equals("*")){
					this.query = "{ \"Level\" : \"" + "Patients" + "\","
							+ "\"Query\" : {\"PatientID\" : \"" + input +"\", "
							+ "\"PatientName\" : \"" + "*" +"\","
							+ "\"StudyDate\" : \"" + date +"\","
							+ "\"AccessionNumber\" : \"" + "*" +"\" }"
							+ "}";
				}else{
					this.query = "{ \"Level\" : \"" + "Patients" + "\","
							+ "\"Query\" : {\"PatientID\" : \"" + input +"\", "
							+ "\"PatientName\" : \"" + "*" +"\","
							+ "\"StudyDate\" : \"" + date +"\","
							+ "\"StudyDescription\" : \"" + studyDesc + "\","
							+ "\"AccessionNumber\" : \"" + "*" +"\" }"
							+ "}";
				}
				break;
			default:
				break;
			}
		}
	}

	public String getQuery(){
		return this.query;
	}
	
	private String sendQuery() throws IOException{
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		StringBuilder sb = new StringBuilder();
		String line = "";
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

		if(stackTraceElements[2].getMethodName().equals("storeIDs") && !this.level.equals("Series")){
			if(this.url.toString().contains("tools/find")){
				OutputStream os = conn.getOutputStream();
				os.write((this.query).getBytes());
				os.flush();
			}
		}else if(stackTraceElements[2].getMethodName().equals("store")){
			OutputStream os = conn.getOutputStream();
			os.write((this.toolboxListContent).getBytes());
			os.flush();
		}

		if (conn.getResponseCode() != 200) {
			if(conn.getResponseCode() == 401){
				IJ.runMacro("run(\"Launch setup\");");
			}
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

		while (line != null) {
			sb.append(line);
			line = br.readLine();
		}
		br.close();
		conn.disconnect();
		return sb.toString();
	}

	public void storeIDs() throws IOException{
		if(this.level.equals("Series")){
			this.setUrl("/studies/" + this.input);
			String studyResponse = this.sendQuery();
			String pattern1 = "Series\" : [ ";
			String pattern2 = " ]";
			Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			Matcher m = p.matcher(studyResponse);
			while(m.find()){
				String pattern3 = "\"";
				String pattern4 = "\"";
				Pattern p2 = Pattern.compile(Pattern.quote(pattern3) + "(.*?)" + Pattern.quote(pattern4));
				Matcher m2 = p2.matcher(m.group(1));
				while(m2.find()){
					this.ids.add(m2.group(1));
				}
			}
		}else if(this.level.equals("Patients")){
			String[] test = this.sendQuery().split("[^0-9-a-zA-Z]");
			for(String s : Arrays.asList(test)){
				if(!s.matches("^\\s*$")){
					ids.add(s);
				}
			}
		}else if(this.level.equals("Studies")){
			this.setUrl("patients/" + this.input);
			String studyResponse = this.sendQuery();
			String pattern1 = "Studies\" : [ ";
			String pattern2 = " ]";
			Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			Matcher m = p.matcher(studyResponse);
			while(m.find()){
				String pattern3 = "\"";
				String pattern4 = "\"";
				Pattern p2 = Pattern.compile(Pattern.quote(pattern3) + "(.*?)" + Pattern.quote(pattern4));
				Matcher m2 = p2.matcher(m.group(1));
				while(m2.find()){
					this.ids.add(m2.group(1));
				}
			}
		}
	}

	public ArrayList<String> getIDs(){
		return this.ids;
	}

	// This method may only be called once because the query depends on the constructor's one
	public String openIDs() throws IOException{
		ids.removeAll(ids);
		this.storeIDs();
		switch (this.level) {
		case "Patients":
			this.level = "patients";
			break;
		case "Studies":
			this.level = "studies";
			break;
		case "Series":
			this.level = "series";
			break;
		default:
			// Ignore
			break;
		}
		for(String id : ids){
			this.setUrl("/" + level + "/" + id + "/");
			this.response.append(this.sendQuery());
		}
		return response.toString();
	}

	public String extractData(String field) throws IOException{
		StringBuilder data = new StringBuilder();
		String pattern1;
		String pattern2;
		Pattern p;
		Matcher m;
		this.openIDs();
		switch (level) {
		case "patients":
			if(field.equals("name")){
				pattern1 = "PatientName\" : \"";
			}else if(field.equals("id")){
				pattern1 = "\"ID\" : \"";
			}else if(field.equals("birthdate")){
				pattern1 = "\"PatientBirthDate\" : \"";
			}else{
				pattern1 = "PatientID\" : \"";
			}
			pattern2 = "\"";
			p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			m = p.matcher(response.toString());
			while(m.find()){
				data.append(m.group(1)+"SEPARATOR");
			}
			break;
		case "series":
			StringBuilder nbInstancesString = new StringBuilder();
			int nbInstances = 0;
			String instance = "";
			pattern1 = "\"ID\" : ";
			pattern2 = "}";
			if(field.equals("nbInstances") || field.equals("instance")){
				pattern1 = "\"Instances\" : [";
				pattern2 = "],";
			}
			p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			m = p.matcher(response.toString());
			while(m.find()){
				if(field.equals("nbInstances") || field.equals("instance")){
					String pattern3 = "\"";
					String pattern4 = "\"";
					Pattern p2 = Pattern.compile(Pattern.quote(pattern3) + "(.*?)" + Pattern.quote(pattern4));
					Matcher m2 = p2.matcher(m.group(1));
					while(m2.find()){
						nbInstances++;
						if(field.equals("instance")){
							if(instance.equals("")){
								instance = m2.group(1);
							}
						}
					}
				}
				nbInstancesString.append(nbInstances+"SEPARATOR");
				if(field.equals("instance")){
					data.append(instance + "SEPARATOR");
					instance = "";
				}
				nbInstances = 0;
				if(field.equals("description")){
					String pattern3 = "SeriesDescription\" : \"";
					String pattern4 = "\"";
					Pattern p2 = Pattern.compile(Pattern.quote(pattern3) + "(.*?)" + Pattern.quote(pattern4));
					Matcher m2 = p2.matcher(m.group(1));
					if(m2.find()){
						data.append(m2.group(1)+"SEPARATOR");
					}else{
						data.append(" SEPARATOR");
					}
				}
				if(field.equals("study")){
					String pattern3 = "ParentStudy\" : \"";
					String pattern4 = "\"";
					Pattern p2 = Pattern.compile(Pattern.quote(pattern3) + "(.*?)" + Pattern.quote(pattern4));
					Matcher m2 = p2.matcher(m.group(1));
					if(m2.find()){
						data.append(m2.group(1)+"SEPARATOR");
					}else{
						data.append(" SEPARATOR");
					}
				}

				if(field.equals("modality")){
					String pattern3 = "Modality\" : \"";
					String pattern4 = "\"";
					Pattern p2 = Pattern.compile(Pattern.quote(pattern3) + "(.*?)" + Pattern.quote(pattern4));
					Matcher m2 = p2.matcher(m.group(1));
					if(m2.find()){
						data.append(m2.group(1)+"SEPARATOR");
					}else{
						data.append(" SEPARATOR");
					}
				}
				else if(field.equals("id")){
					String pattern3 = "\"";
					String pattern4 = "\"";
					Pattern p2 = Pattern.compile(Pattern.quote(pattern3) + "(.*?)" + Pattern.quote(pattern4));
					Matcher m2 = p2.matcher(m.group(1));
					if(m2.find()){
						data.append(m2.group(1)+"SEPARATOR");
					}
				}
			}
			if(field.equals("nbInstances")){
				return nbInstancesString.toString();
			}
			break;
		case "studies":
			pattern1 = "\"ID\" :";
			pattern2 = "}";
			p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			m = p.matcher(this.response.toString());
			while(m.find()){
				if(field.equals("description")){
					String pattern3 = "StudyDescription\" : \"";
					String pattern4 = "\"";
					Pattern p2 = Pattern.compile(Pattern.quote(pattern3) + "(.*?)" + Pattern.quote(pattern4));
					Matcher m2 = p2.matcher(m.group(1));
					if(m2.find()){
						data.append(m2.group(1) + "SEPARATOR");
					}else{
						data.append(" SEPARATOR");
					}
				}else if(field.equals("id")){
					String pattern3 = "\"";
					String pattern4 = "\"";
					Pattern p2 = Pattern.compile(Pattern.quote(pattern3) + "(.*?)" + Pattern.quote(pattern4));
					Matcher m2 = p2.matcher(m.group(1));
					if(m2.find()){
						data.append(m2.group(1) + "SEPARATOR");
					}else{
						data.append(" SEPARATOR");
					}
				}else if(field.equals("accession")){
					String pattern3 = "AccessionNumber\" : \"";
					String pattern4 = "\"";
					Pattern p2 = Pattern.compile(Pattern.quote(pattern3) + "(.*?)" + Pattern.quote(pattern4));
					Matcher m2 = p2.matcher(m.group(1));
					if(m2.find()){
						data.append(m2.group(1) + "SEPARATOR");
					}else{
						data.append(" SEPARATOR");
					}
				}else if(field.equals("date")){
					String pattern3 = "StudyDate\" : \"";
					String pattern4 = "\"";
					Pattern p2 = Pattern.compile(Pattern.quote(pattern3) + "(.*?)" + Pattern.quote(pattern4));
					Matcher m2 = p2.matcher(m.group(1));
					if(m2.find()){
						data.append(m2.group(1) + "SEPARATOR");
					}else{
						data.append(" SEPARATOR");
					}
				}else if(field.equals("studyInstanceUID")){
					String pattern3 = "StudyInstanceUID\" : \"";
					String pattern4 = "\"";
					Pattern p2 = Pattern.compile(Pattern.quote(pattern3) + "(.*?)" + Pattern.quote(pattern4));
					Matcher m2 = p2.matcher(m.group(1));
					if(m2.find()){
						data.append(m2.group(1) + "SEPARATOR");
					}else{
						data.append(" SEPARATOR");
					}
				}
			}
			break;
		default:
			// Ignore
			break;
		}
		return data.toString();
	}

	public String getStudyDescriptionAndUID(String orthancUID) throws IOException{
		this.setUrl("studies/" + orthancUID);
		StringBuilder sb = new StringBuilder();
		String response = this.sendQuery();
		String pattern1 = "StudyDescription\" : \"";
		String pattern2 = "\"";
		Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
		Matcher m = p.matcher(response.toString());
		while(m.find()){
			sb.append(m.group(1));
		}
		sb.append("SEPARATOR");
		pattern1 = "StudyInstanceUID\" : \"";
		pattern2 = "\"";
		p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
		m = p.matcher(response.toString());
		while(m.find()){
			sb.append(m.group(1));
		}
		sb.append("SEPARATOR");
		pattern1 = "StudyDate\" : \"";
		pattern2 = "\"";
		p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
		m = p.matcher(response.toString());
		while(m.find()){
			sb.append(m.group(1));
		}
		return sb.toString();
	}

	public Object[] getAET() throws IOException{
		this.setUrl("modalities");
		String aet = this.sendQuery();

		// We split the server response in a tab
		ArrayList<String> indexes = new ArrayList<String>();

		// We store the indexes from serverResponse in the ArrayList indexes
		String pattern1 = "\"";
		String pattern2 = "\"";

		Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
		Matcher m = p.matcher(aet);
		while (m.find()) {
			indexes.add(m.group(1));
		}

		// We convert the ArrayList to an Object[]
		return indexes.toArray();
	}
	
	public void store(String aet, ArrayList<String> idList) throws IOException{
		StringBuilder ids = new StringBuilder();
		ids.append("[");
		for(int i = 0; i < idList.size(); i++){
			ids.append("\"" + idList.get(i) + "\",");
		}
		ids.replace(ids.length()-1, ids.length(), "]");
		this.toolboxListContent = ids.toString();
		this.setUrl("modalities/" + aet + "/store");
		this.sendQuery();
	}

	public Object[] getPeers() throws IOException{
		this.setUrl("peers");
		String peers = this.sendQuery();

		// We split the server response in a tab
		ArrayList<String> indexes = new ArrayList<String>();

		// We store the indexes from serverResponse in the ArrayList indexes
		String pattern1 = "\"";
		String pattern2 = "\"";

		Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
		Matcher m = p.matcher(peers);
		while (m.find()) {
			indexes.add(m.group(1));
		}

		// We convert the ArrayList to an Object[]
		return indexes.toArray();
	}
	
	public void sendPeer(String peer, ArrayList<String> idList) throws IOException{
		StringBuilder ids = new StringBuilder();
		ids.append("[");
		for(int i = 0; i < idList.size(); i++){
			ids.append("\"" + idList.get(i) + "\",");
		}
		ids.replace(ids.length()-1, ids.length(), "]");
		this.toolboxListContent = ids.toString();
		this.setUrl("peers/" + peer + "/store");
		this.sendQuery();
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
