package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private final DateTimeParser dateTimeParser;

    private static final String PAGE_LINK = "/vacancies/java_developer";

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
        String rsl = "";
        Connection connection = Jsoup.connect(link);
        Document document;
        try {
            document = connection.get();
            rsl = document.select(".collapsible-description__content").text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rsl;
    }

    private LocalDateTime retrieveDate(Element row) {
        return dateTimeParser.parse(row.select(".vacancy-card__date")
                .first()
                .child(0)
                .attr("datetime"));
    }

    @Override
    public List<Post> list(String link) {
        List<Post> rsl = new ArrayList<>();
        for (int page = 1; page <= 5; page++) {
            try {
                Connection connection = Jsoup.connect(String.format("%s%s?page=%d", link, PAGE_LINK, page));
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                if (rows.size() == 0) {
                    break;
                }
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    String vacancyLink = String.format("%s%s", link,
                            titleElement.child(0).attr("href"));
                    rsl.add(new Post(titleElement.text(),
                            vacancyLink,
                            retrieveDescription(vacancyLink),
                            retrieveDate(row)));
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rsl;
    }

    public static void main(String[] args) {
        HabrCareerParse hcp = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> list = hcp.list("https://career.habr.com");
        list.forEach(System.out::println);
    }
}