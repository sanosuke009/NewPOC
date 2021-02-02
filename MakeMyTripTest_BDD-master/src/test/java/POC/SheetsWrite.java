package POC;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridCoordinate;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.RowData;
import com.google.api.services.sheets.v4.model.UpdateCellsRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SheetsWrite {
	private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";
	private static final String spreadsheetId = "1WQa-pdEwN72Nr6TAolF5QG3pQNQixjN5n6fBcvAE0E8";
	private static final String range = "A:ZZ";
	public static Map<String,Map<String,String>> map = new HashMap<>();
	public static List<String> columns = new ArrayList<String>();
	public static List<String> allkeywords = new ArrayList<String>();
	public static int keywordcolumnnumber = 0;

	/**
	 * Global instance of the scopes required by this quickstart.
	 * If modifying these scopes, delete your previously saved tokens/ folder.
	 */
	private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
	// private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
	private static final String CREDENTIALS_FILE_PATH = System.getProperty("user.dir").replace("\\", "/")+"/roysano999/credentials.json";

	/**
	 * Creates an authorized Credential object.
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.

		//File initialFile = new File("src/main/resources/sample.txt");
		File initialFile = new File(CREDENTIALS_FILE_PATH);
		InputStream in = new FileInputStream(initialFile);

		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
				.setAccessType("offline")
				.build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	@SuppressWarnings("unchecked")
	public static String setMap()
	{
		String res="";

		// Build a new authorized API client service.
		NetHttpTransport HTTP_TRANSPORT;
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME)
					.build();
			ValueRange response = service.spreadsheets().values()
					.get(spreadsheetId, range)
					.execute();


			List<List<Object>> values = response.getValues();

			if (values == null || values.isEmpty()) {
				System.out.println("No data found.");
			} else {
				//System.out.println("List size "+values.size());
				int count = 0;
				for (@SuppressWarnings("rawtypes") List row : values) {
					// Print columns A and E, which correspond to indices 0 and 4.
					//System.out.printf("%s, %s\n", row.get(0), row.get(4));
					if(count == 0)
					{
						columns = row;
					}
					else {
						Map<String,String> valuemap = new HashMap<>();
						for(int j=0;j<row.size();j++)
						{
							valuemap.put(columns.get(j).toString(), row.get(j).toString());
						}
						allkeywords.add(row.get(keywordcolumnnumber).toString());
						map.put(row.get(keywordcolumnnumber).toString(), valuemap);
					}
					count++;
				}
			}
		} catch (GeneralSecurityException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

	public static String get(String testcasename, String columnname)
	{
		String res = "";
		Map<String,String> lst = new HashMap<String,String>();
		try {
			lst = map.get(testcasename);
		}
		catch(Exception e)
		{
			System.out.println("The keyword "+testcasename+" is invalid. Please check.");
		}
		try {
			res = lst.get(columnname).toString();
		}
		catch(Exception e)
		{
			System.out.println("The column name "+columnname+" is invalid. Please check.");
		}

		return res;
	}

	public static boolean write(String testcasename, String columnname, String value)//
	{
		boolean res =false;
		int rowindex=0;
		int colindex=0;
		// Build a new authorized API client service.
		NetHttpTransport HTTP_TRANSPORT;
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
					.setApplicationName(APPLICATION_NAME)
					.build();
			List<Request> requests = new ArrayList<>();

			List<CellData> values = new ArrayList<>();

			try {
				rowindex = allkeywords.indexOf(testcasename) + 1;
			}
			catch(NullPointerException e)
			{
				System.out.println("The keyword "+testcasename+" is invalid. Please check.");
				return false;
			}
			try {
				colindex = columns.indexOf(columnname);
			}
			catch(NullPointerException e)
			{
				System.out.println("The column name "+columnname+" is invalid. Please check.");
				return false;
			}

			values.add(new CellData()
					.setUserEnteredValue(new ExtendedValue()
							.setStringValue(value)));
			requests.add(new Request()
					.setUpdateCells(new UpdateCellsRequest()
							.setStart(new GridCoordinate()
									.setSheetId(0)
									.setRowIndex(rowindex)
									.setColumnIndex(colindex))
							.setRows(Arrays.asList(
									new RowData().setValues(values)))
							.setFields("userEnteredValue,userEnteredFormat.backgroundColor")));

			BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
					.setRequests(requests);

			service.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest)
			.execute();
			res = true;
		} catch (IOException | GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			res =false;
		}

		return res;
	}

	/**
	 * Prints the names and majors of students in a sample spreadsheet:
	 * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
	 */
	public static void main(String... args) throws IOException, GeneralSecurityException {

		setMap();
		System.out.println(get("hbcva","Column7"));
		System.out.println(get("vwqdgc","Column3"));
		System.out.println(get("bch","Column2"));
		System.out.println(get("vwqdgc","Column5"));
		System.out.println(get("bch","Column4"));
		write("vwqdgc","Column3","Demo of selenium");
		write("bch","Column4","");
		setMap();
		System.out.println(get("hbcva","Column7"));
		System.out.println(get("vwqdgc","Column3"));
		System.out.println(get("bch","Column2"));
		System.out.println(get("vwqdgc","Column5"));
		System.out.println(get("bch","Column4"));
	}
}