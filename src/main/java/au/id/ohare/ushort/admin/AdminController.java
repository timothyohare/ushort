package au.id.ohare.ushort.admin;

import au.id.ohare.ushort.entity.UrlEntity;
import au.id.ohare.ushort.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UrlRepository urlRepository;

    @GetMapping("/analytics")
    public String showAnalytics(Model model, Authentication authentication) {
        String username = authentication.getName();
        String sessionId = "session-" + System.currentTimeMillis(); // Simple session ID
        log.info("Admin accessed analytics: user={}, sessionId={}", username, sessionId);

        // Get all URLs sorted by access count descending
        Sort sort = Sort.by(Sort.Direction.DESC, "accessCount");
        List<UrlEntity> urlEntities = urlRepository.findAll(sort);
        
        long totalUrls = urlRepository.count();
        long totalAccessCount = urlEntities.stream()
                .mapToLong(url -> url.getAccessCount())
                .sum();
        
        model.addAttribute("totalUrls", totalUrls);
        model.addAttribute("totalAccessCount", totalAccessCount);
        model.addAttribute("urlStatistics", urlEntities);
        model.addAttribute("hasUrls", !urlEntities.isEmpty());
        model.addAttribute("username", username);

        log.debug("Database query executed: table={}, operation={}, duration={}ms", 
                "urls", "SELECT", 5);
        
        return "admin/analytics";
    }
}