package roomescape.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.theme.ThemeRankResponses;
import roomescape.controller.dto.theme.ThemeRankResponse;
import roomescape.controller.dto.theme.ThemeResponse;
import roomescape.controller.dto.theme.ThemeRankingQuery;
import roomescape.controller.dto.theme.ThemeResponses;
import roomescape.service.ThemeService;

@RestController
@RequestMapping("/themes")
@RequiredArgsConstructor
public class ThemeController {

    private final ThemeService themeService;

    @GetMapping
    public ResponseEntity<ThemeResponses> getThemes() {
        List<ThemeResponse> themes = themeService.getThemes().stream()
                .map(ThemeResponse::from)
                .toList();
        return ResponseEntity.ok(new ThemeResponses(themes));
    }

    @GetMapping("/rank")
    public ResponseEntity<ThemeRankResponses> getThemeRankings(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit
    ) {
        ThemeRankingQuery query = new ThemeRankingQuery(days, limit);
        List<ThemeRankResponse> themeRankings = themeService.getThemeRankings(query.toCondition())
                .stream()
                .map(ThemeRankResponse::from)
                .toList();
        return ResponseEntity.ok(new ThemeRankResponses(themeRankings));
    }

}
