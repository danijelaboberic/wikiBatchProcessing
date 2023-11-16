package pmf.dmi;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkIterator implements Iterable<LinkIterator> {
	private final String BASE_URL = "https://dumps.wikimedia.org/other/pageview_complete/";
	public static LocalDateTime END;
	private final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	public LocalDateTime getCurrDate() {
		return currDate;
	}

	private LocalDateTime currDate;
	private String currDateTxt;
	private int year;
	private int month;
	private int day;
	public String value;

	/**
	 * @param startY
	 * @param startM
	 * @param startD
	 */
	LinkIterator(int startY, int startM, int startD) {
		year = startY;
		month = startM;
		day = startD;
		String m, d, h;
		if (month < 10) {
			m = "0" + month;
		} else {
			m = "" + month;
		}
		if (day < 10) {
			d = "0" + day;
		} else {
			d = "" + day;
		}
		value = BASE_URL + year + "/" + year + "-" + m + "/pageviews-" + year + m + d + "-user.bz2";
		currDate = LocalDate.parse("" + year + "-" + m + "-" + d).atTime(0, 0);
		currDateTxt = currDate.format(DF);

	}

	@Override
	public Iterator<LinkIterator> iterator() {
		return new Iterator<LinkIterator>() {
			private LinkIterator link = new LinkIterator(LinkIterator.this.year, LinkIterator.this.month,
					LinkIterator.this.day);

			@Override
			public boolean hasNext() {
				return link.currDate.isBefore(END);
			}

			@Override
			public LinkIterator next() {
				if (!hasNext())
					throw new NoSuchElementException();

				LinkIterator curr = link;
				LocalDateTime next = link.currDate.plusDays(1);
				link = new LinkIterator(next.getYear(), next.getMonthValue(), next.getDayOfMonth());

				return curr;
			}
		};
	}

	public String getCurrDateTxt() {
		return currDateTxt;
	}

	public static void main(String[] args) {
		for (LinkIterator link : new LinkIterator(2022, 1, 1))
			System.out.println(link.value);
	}

}
