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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ij.IJ;

public class DataFetcher {

	private String id;
	private String url;
	private String fullAddress;
	private String ip;
	private String port;
	private String authentication;
	private Preferences jprefer = Preferences.userRoot().node("<unnamed>/biplugins");
	private Preferences jpreferPerso = Preferences.userRoot().node("<unnamed>/queryplugin");

	public DataFetcher(String id){
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
		this.id = id;
	}

	private String fetchData() throws IOException{
		URL url2  = new URL(url);
		HttpURLConnection conn = (HttpURLConnection) url2.openConnection();
		conn.setDoOutput(true);
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
		if (conn.getResponseCode() != 200) {
			if(conn.getResponseCode() == 401){
				IJ.runMacro("run(\"Launch setup\");");
			}
			throw new RuntimeException("Failed : HTTP error code : "
					+ conn.getResponseCode());
		}

		StringBuilder sb = new StringBuilder();
		String line = "";

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
	
	/*
	 * extracts data from /studies/.../
	 */
	public String extractData(String field) throws IOException{
		String response = "";
		String data = "";
		String pattern1;
		String pattern2;
		Pattern p;
		Matcher m;
		switch (field) {
		case "AnonymizedFrom":
			this.setUrl("studies/" + this.id);
			response = this.fetchData();
			pattern1 = "\"AnonymizedFrom\" : \"";
			pattern2 = "\"";
			p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			m = p.matcher(response.toString());
			while(m.find()){
				data = m.group(1);
			}
			break;
		case "PatientID":
			this.setUrl("studies/" + this.id);
			response = this.fetchData();
			pattern1 = "\"PatientID\" : \"";
			pattern2 = "\"";
			p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			m = p.matcher(response.toString());
			while(m.find()){
				data = m.group(1);
			}
			break;
		case "PatientName":
			this.setUrl("studies/" + this.id);
			response = this.fetchData();
			pattern1 = "\"PatientName\" : \"";
			pattern2 = "\"";
			p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			m = p.matcher(response.toString());
			while(m.find()){
				data = m.group(1);
			}
			break;
		case "StudyDate":
			this.setUrl("studies/" + this.id);
			response = this.fetchData();
			pattern1 = "\"StudyDate\" : \"";
			pattern2 = "\"";
			p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			m = p.matcher(response.toString());
			while(m.find()){
				data = m.group(1);
			}
			break;
		case "StudyDescription":
			this.setUrl("studies/" + this.id);
			response = this.fetchData();
			pattern1 = "\"StudyDescription\" : \"";
			pattern2 = "\"";
			p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			m = p.matcher(response.toString());
			while(m.find()){
				data = m.group(1);
			}
			break;
		case "StudyInstanceUID":
			this.setUrl("studies/" + this.id);
			response = this.fetchData();
			pattern1 = "\"StudyInstanceUID\" : \"";
			pattern2 = "\"";
			p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			m = p.matcher(response.toString());
			while(m.find()){
				data = m.group(1);
			}
			break;
		default:
			break;
		}
		return data;
	}
	
	
	/*
	 * extracts data from /studies/.../statistics
	 */
	public String extractStats(String field) throws IOException{
		String response = "";
		String data = "";
		String pattern1;
		String pattern2;
		Pattern p;
		Matcher m;
		switch (field) {
		case "CountInstances":
			this.setUrl("studies/" + this.id + "/statistics");
			response = this.fetchData();
			pattern1 = "\"CountInstances\" : ";
			pattern2 = ",";
			p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			m = p.matcher(response.toString());
			while(m.find()){
				data = m.group(1);
			}
			break;
		case "CountSeries":
			this.setUrl("studies/" + this.id + "/statistics");
			response = this.fetchData();
			pattern1 = "\"CountSeries\" : ";
			pattern2 = ",";
			p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			m = p.matcher(response.toString());
			while(m.find()){
				data = m.group(1);
			}
			break;
		case "DiskSize":
			this.setUrl("studies/" + this.id + "/statistics");
			response = this.fetchData();
			pattern1 = "\"DiskSizeMB\" : ";
			pattern2 = ",";
			p = Pattern.compile(Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
			m = p.matcher(response.toString());
			while(m.find()){
				data = m.group(1);
			}
			break;		
		default:
			break;
		}
		return data;
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
