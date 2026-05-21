package roomescape.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.theme.ThemeRequest;
import roomescape.controller.dto.theme.ThemeResponse;
import roomescape.global.auth.LoginRequired;
import roomescape.service.ThemeService;
import roomescape.service.dto.theme.ThemeResult;

@RestController
@RequestMapping("/admin/themes")
@RequiredArgsConstructor
@LoginRequired(managerOnly = true)
public class AdminThemeController {

    private final ThemeService themeService;

    @PostMapping
    public ResponseEntity<ThemeResponse> createTheme(@Valid @RequestBody ThemeRequest request) {
        ThemeResult themeResult = themeService.createTheme(request.toCommand());
        ThemeResponse theme = ThemeResponse.from(themeResult);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(theme);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable Long id) {
        themeService.deleteTheme(id);
        return ResponseEntity.noContent().build();
    }
}
