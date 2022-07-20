package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        for (int page = 1; page <= 5; page++) {
            Connection connection = Jsoup.connect(String.format("%s?page=%d", PAGE_LINK, page));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            if (rows.size() == 0) {
                break;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            HabrCareerDateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                LocalDateTime date = dateTimeParser.parse(row.select(".vacancy-card__date")
                        .first()
                        .child(0)
                        .attr("datetime"));
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", vacancyName, link, date.format(formatter));
                System.out.println(retrieveDescription(link));
            });
        }
    }

    private static String retrieveDescription(String link) {
        String rsl = "";
        Connection connection = Jsoup.connect(link);
        Document document;
        try {
            document = connection.get();
            rsl = document.select(".collapsible-description__content").toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rsl;
    }
}