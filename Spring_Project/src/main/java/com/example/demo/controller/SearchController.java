package com.example.demo.controller;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Website;
import com.example.demo.service.impl.GoogleSearchServiceImpl;
import com.example.demo.service.impl.ScoreServiceImpl;
import com.example.demo.service.impl.WebsiteServiceImpl;

@CrossOrigin
@Controller
@RestController
@RequestMapping(value = "/search", produces = { MediaType.APPLICATION_JSON_VALUE, "application/json;charset=UTF-8" })

public class SearchController {

  @Autowired
  private GoogleSearchServiceImpl googleSearchService;

  @Autowired
  private WebsiteServiceImpl websiteService;

  @Autowired
  private ScoreServiceImpl scoreService;

  @GetMapping(value = "/{query}", produces = { MediaType.APPLICATION_JSON_VALUE, "application/json;charset=UTF-8" })
  public ResponseEntity<ArrayList<Website>> getSearchResult(@PathVariable("query") String query) {

    ArrayList<Website> websites = googleSearchService.getSearchResult(query);

    ForkJoinPool forkJoinPool = new ForkJoinPool();
    forkJoinPool.submit(() -> websites.parallelStream().forEach(website -> {
      website.setContent(websiteService.getContent(website.getURL()));
      scoreService.calculateScore(website);
      ArrayList<Website> subpages = websiteService.getSubsites(website.getURL());
      subpages.parallelStream().forEach(subpage -> {
        subpage.setContent(websiteService.getContent(subpage.getURL()));
        scoreService.calculateScore(subpage);
        website.addSubpage(subpage);
      });
      scoreService.calculateTotalScore(website);
    })).join();

    websites.sort((a, b) -> {
      return a.getScore() < b.getScore() ? 1 : -1;
    });

    return ResponseEntity.ok(websites);

  }
}
