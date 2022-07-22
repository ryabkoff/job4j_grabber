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

    private static final int PAGE_COUNT = 5;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    private String retrieveDescription(String link) {
        String rsl;
        Connection connection = Jsoup.connect(link);
        Document document;
        try {
            document = connection.get();
            rsl = document.select(".collapsible-description__content").text();
        } catch (IOException e) {
            throw new IllegalArgumentException();
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
        for (int page = 1; page <= PAGE_COUNT; page++) {
            try {
                String pageLink = String.format(link, page);
                Connection connection = Jsoup.connect(pageLink);
                Document document = connection.get();
                Elements rows = document.select(".vacancy-card__inner");
                if (rows.size() == 0) {
                    break;
                }
                rows.forEach(row -> {
                    Element titleElement = row.select(".vacancy-card__title").first();
                    String vacancyLink = String.format("%s%s", pageLink,
                            titleElement.child(0).attr("href"));
                    rsl.add(new Post(titleElement.text(),
                            vacancyLink,
                            retrieveDescription(vacancyLink),
                            retrieveDate(row)));
                });
            } catch (IOException e) {
                throw new IllegalArgumentException();
            }
        }
        return rsl;
    }

    public static void main(String[] args) {
        HabrCareerParse hcp = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> list = hcp.list("https://career.habr.com/vacancies/java_developer?page=%d");
        list.forEach(System.out::println);
    }
}