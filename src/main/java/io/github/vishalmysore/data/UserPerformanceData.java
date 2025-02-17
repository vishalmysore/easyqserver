package io.github.vishalmysore.data;

import lombok.*;

import java.util.List;
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserPerformanceData {

    private List<String> topSkills;
    private List<String> bottomSkills;
    private int overallScore;
    private List<String> improvements;
    private List<ArticleToReadAgain> articlesToReadAgain;
    private String textSummary;
    private String userId;
    private String emailId;
    private String avatar;

}
