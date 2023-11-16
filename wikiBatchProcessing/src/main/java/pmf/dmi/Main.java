package pmf.dmi;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Main {
	private static HashMap<String, HashMap<Integer, Integer>> dataMap = new HashMap<>();
	private static LinkIterator ITERATOR;
	private static FileWriter fw;

	public static void main(String[] args) throws InterruptedException, IOException {
		if (args.length != 2) {
			System.out.println("Enter: start_date(yyyy-mm-dd) end date(yyyy-mm-dd)");
			System.out.println("Interval includes start day but it excudes end day");
			return;
		}
		LocalDate startDate = LocalDate.parse(args[0]);
		ITERATOR = new LinkIterator(startDate.getYear(), startDate.getMonthValue(), startDate.getDayOfMonth());
		LinkIterator.END = LocalDate.parse(args[1]).atTime(0, 0);
		File OUT = new File(Paths.get("wikidumps_" + args[0] + ".csv").toString());

		PropertyConfigurator.configure(Main.class.getResourceAsStream("/log4j.properties"));
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					fw = new FileWriter(OUT);
					fw.append("measurement,viewCount,time");
					fw.append('\n');
					for (LinkIterator link : ITERATOR) {
						Logger.getLogger(Main.class).info(" == Processing link == " + link.value);
						HashMap<String, Integer> dataMap = new HashMap<>();
						try (BufferedReader is = getBufferedReaderForCompressedFile(link.value)) {
							is.lines().filter(l -> l.startsWith("en.")).map(l -> l.split("\\s+"))
									.filter(parts -> startsWithDigitOrLatter(parts[1]))
									.filter(parts -> notContainSpecialKeywords(parts[1])).forEach(parts -> {
										if (parts[4] != null && !parts[4].isEmpty()) {
											if (dataMap.get(parts[1]) == null) {
												dataMap.put(parts[1], Integer.parseInt(parts[4]));
											} else {
												int viewcount = dataMap.get(parts[1]);
												dataMap.put(parts[1], Integer.parseInt(parts[4]) + viewcount);
											}

										}
									});
							dataMap.forEach((k, v) -> {
								try {
									if (k.contains("\\")) {
										Logger.getLogger(Main.class).info("Nedozvoljeni karakter " + k);
									} else {
										fw.append(k.replaceAll("\"", "_quote_").replaceAll(",", "_"));
										fw.append("," + v);
										fw.append(",");
										fw.append(link.getCurrDateTxt());
										fw.append('\n');
									}
								} catch (IOException e) {
									e.printStackTrace();
									Logger.getLogger(Main.class).error(e.getMessage());

								}

							});
						} catch (Exception e2) {
							e2.printStackTrace();
							Logger.getLogger(Main.class).error(e2.getMessage());
						}

						Logger.getLogger(Main.class).info("  == Processing done.");
					}
					Logger.getLogger(Main.class)
							.info(" ===================== ALL DONE, CSV LOCATION: " + OUT.toString());
					fw.flush();
					fw.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		runnable.run();
	}

	public static BufferedReader getBufferedReaderForCompressedFile(String fileIn)
			throws IOException, CompressorException, CompressorException {
		BufferedInputStream bis = new BufferedInputStream(new URL(fileIn).openConnection().getInputStream());
		CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(bis);
		BufferedReader br2 = new BufferedReader(new InputStreamReader(input));
		return br2;
	}

	static boolean startsWithDigitOrLatter(String s) {
		return Pattern.compile("^[a-zA-Z0-9]").matcher(s).find();
	}

	static boolean notContainSpecialKeywords(String s) {
		return !(s.startsWith("File:") || s.startsWith("Category:") || s.startsWith("Module:")
				|| s.startsWith("Template:") || s.startsWith("Talk:") || s.startsWith("Special:")
				|| s.startsWith("User_talk:") || s.startsWith("User:") || s.startsWith("Wikipedia:")
				|| s.startsWith("Help_talk:") || s.startsWith("Help:") || s.startsWith("Draft:")
				|| s.startsWith("MediaWiki:") || s.startsWith("Portal:") || s.startsWith("TimedText:")

		);

	}
}
