package io.github.vishalmysore.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.extern.java.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Log
@Service
public class ScraperService {

    public String scrape(String url){
        try {
            log.info("Scraping URL: " + url);
            // Fetch the webpage
            Document document = Jsoup.connect(url).get();

            // Get the entire page's text content (visible text only)
            String pageText = document.text();

            return pageText;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "could not scrape the page "+url+" please check the url and try again"   ;
    }
        public String scrapePW(String url){
            try (Playwright playwright = Playwright.create()) {
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
                Page page = browser.newPage();
                page.navigate(url);
                String title = page.title();
                String pageText = page.innerText("body");
                log.info("Page Title: " + title);
                log.info("Pager Url"+url);
                browser.close();
                return title+" : "+ pageText;
            }

        }
}
