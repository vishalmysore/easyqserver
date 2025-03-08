package io.github.vishalmysore.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import io.github.vishalmysore.data.NewsBlast;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class NewsService {

    public NewsBlast getNews(String topic, int limit) {
        NewsBlast newsBlast = new NewsBlast();
        List<String> titles = new ArrayList<>();
        List<String> links = new ArrayList<>();
        List<String> descriptions = new ArrayList<>();
        StringBuilder overallNews = new StringBuilder();

        try {
            String encodedTopic = URLEncoder.encode(topic, "UTF-8");
            String rssUrl = "https://news.google.com/rss/search?q=" + encodedTopic + "&hl=en-US&gl=US&ceid=US:en";
            URL feedUrl = new URL(rssUrl);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));

            List<SyndEntry> entries = feed.getEntries();
            int count = 0;
            for (SyndEntry entry : entries) {
                if (count >= limit) {
                    break;
                }
                if (entry.getTitle().contains(topic) || entry.getDescription().getValue().contains(topic)) {
                    String title = entry.getTitle();
                    String link = entry.getLink();
                    String description = entry.getDescription().getValue();
                    String extractedText = extractTextBetweenAnchorTags(description);

                    titles.add(title);
                    links.add(link);
                    descriptions.add(extractedText);
                    overallNews.append("Title: ").append(title).append("\nDescription: ").append(extractedText).append("\n\n");

                    count++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        newsBlast.setOverallNews(overallNews.toString());
        newsBlast.setTitles(titles);
        newsBlast.setLinks(links);
        newsBlast.setDescriptions(descriptions);

        return newsBlast;
    }

    private static String extractTextBetweenAnchorTags(String text) {
        Pattern pattern = Pattern.compile("<a href=.*?>(.*?)</a>");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}