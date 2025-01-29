package io.github.vishalmysore.service;

import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import sun.jvm.hotspot.debugger.Page;

@Log
@Service
public class ScraperService {

        public String scrape(String url){
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
            return "Problem Scraping " + url;
        }
}
