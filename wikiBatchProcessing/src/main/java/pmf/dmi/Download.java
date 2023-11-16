package pmf.dmi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class Download implements Runnable {

	private String link;
	private File out;

	/**
	 * @param link
	 * @param out
	 */
	public Download(String link, File out) {
		super();
		this.link = link;
		this.out = out;
	}

	@Override
	public void run() {
		try {
			System.out.println("Download start... " + link);
			// out
			FileOutputStream fos = new FileOutputStream(this.out);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

			URL url = new URL(link);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			GZIPInputStream gzip = new GZIPInputStream(conn.getInputStream());
			BufferedReader reader = new BufferedReader(new InputStreamReader(gzip));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("en")) {
					writer.write(line);
					writer.newLine();
				}
			}
			reader.close();
			writer.close();
			System.out.println("Download complete. ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
