package com.test.spring.boot.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.h2.tools.DeleteDbFiles;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController {

	private static final String DRIVER = "org.h2.Driver";
	private static final String CONNECTION = "jdbc:h2:~/test";

	@RequestMapping(value = "/demo", method = RequestMethod.GET)
	public String demo() {
		return "Hello world";
	}

	
	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	public @ResponseBody String uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam("metaData") String metaData) {
		try {
			File fileOut = new File("C://workspace//spring-boot-demo//sample_output.txt");
			FileOutputStream fos = new FileOutputStream(fileOut);
			IOUtils.copy(file.getInputStream(), fos);

			JSONObject json = new JSONObject(metaData);
			String uuid = json.getString("UUID");
			String fileName = json.getString("fileName");

			createDbAndTables();
			insertFile(uuid, fileName, fileOut);

			InputStream inputStream = getFile(uuid);

			StringWriter writer = new StringWriter();
			IOUtils.copy(inputStream, writer, "UTF-8");
			String finalOutPutFile = writer.toString();

			return "Request processing sucessfull";

		} catch (Exception e) {
			return "Exception occured";
		}
	}

	private InputStream getFile(String uuid) throws SQLException {
		Connection connection = getDBConnection();
		PreparedStatement pstmt = connection.prepareStatement("SELECT fileContent from FILE_STORAGE where uuid = ?");

		pstmt.setString(1, uuid);

		ResultSet result = pstmt.executeQuery();

		if (result.next()) {
			Clob clob = result.getClob("fileContent");
			return clob.getAsciiStream();
		}

		return null;
	}

	private void insertFile(String uuid, String fileName, File fileOut) throws SQLException, FileNotFoundException {
		Connection connection = getDBConnection();
		PreparedStatement pstmt = connection.prepareStatement("INSERT INTO FILE_STORAGE VALUES(?,?,?)");

		FileInputStream fis = new FileInputStream(fileOut);
		pstmt.setString(1, uuid);
		pstmt.setString(2, fileName);
		pstmt.setBinaryStream(3, fis, (int) fileOut.length());
		pstmt.execute();
		connection.commit();
	}

	private void createDbAndTables() {

		try {
			DeleteDbFiles.execute("~", "test", true);
			createTable();
		} catch (Exception e) {
			System.out.println("**Exception occured while creating tables **");
			e.printStackTrace();
		}

	}

	private void createTable() {
		Connection connection = getDBConnection();
		Statement stmt = null;

		String query = "CREATE TABLE FILE_STORAGE(uuid varchar2(100), fileName varchar2(100), fileContent clob)";
		try {
			stmt = connection.createStatement();
			int count = stmt.executeUpdate(query);
			System.out.println("table created" + count);
			stmt.close();
		} catch (Exception e) {
			System.out.println(" exception is " + e);
			e.printStackTrace();
		}

	}

	private static Connection getDBConnection() {
		Connection dbConnection = null;
		try {
			Class.forName(DRIVER);
			dbConnection = DriverManager.getConnection(CONNECTION, "", "");
			return dbConnection;
		} catch (ClassNotFoundException | SQLException e) {
			System.out.println(" exception is " + e);
		}
		return dbConnection;
	}

}