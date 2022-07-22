package ru.job4j.grabber;

import ru.job4j.quartz.AlertRabbit;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private Connection cnn;

    public PsqlStore(Properties cfg) {
        try {
            Class.forName(cfg.getProperty("jdbc.connection.driver"));
            cnn = DriverManager.getConnection(
                    cfg.getProperty("jdbc.connection.url"),
                    cfg.getProperty("jdbc.connection.username"),
                    cfg.getProperty("jdbc.connection.password"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps = cnn.prepareStatement(
                "INSERT INTO post(name, link, text, created) "
                        + "VALUES(?, ?, ?, ?) ")) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getLink());
            ps.setString(3, post.getDescription());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> list = new ArrayList<>();
        try (PreparedStatement ps = cnn.prepareStatement(
                "SELECT id, name, link, text, created FROM post")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(createPostByResultSet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement ps = cnn.prepareStatement(
                "SELECT id, name, link, text, created FROM post "
                        + "WHERE id = ?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                post = createPostByResultSet(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post createPostByResultSet(ResultSet rs) throws SQLException {
        return new Post(rs.getInt(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getTimestamp(5).toLocalDateTime());
    }

    private static Properties readProperties() {
        Properties properties = new Properties();
        try (InputStream in = AlertRabbit.class
                .getClassLoader()
                .getResourceAsStream("grabber.properties")) {
            properties.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static void main(String[] args) throws Exception {
        PsqlStore ps = new PsqlStore(readProperties());
        ps.save(new Post("Junior java developer",
                "https://career.habr.com/vacancies/1000103321",
                "Смузи и вкусняшки на кухне",
                LocalDateTime.of(2022, 1, 1, 0, 0)));
        ps.save(new Post("Middle java developer",
                "https://career.habr.com/vacancies/1000103322",
                "Профессиональный и карьерный рост",
                LocalDateTime.of(2022, 2, 1, 0, 0)));
        ps.save(new Post("Senior java developer",
                "https://career.habr.com/vacancies/1000107499",
                "Архитектура и оптимизация",
                LocalDateTime.of(2022, 3, 1, 0, 0)));
        ps.getAll().forEach(System.out::println);
        System.out.println(ps.findById(3));
        ps.close();
    }
}