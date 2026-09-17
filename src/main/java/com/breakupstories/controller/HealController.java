package com.breakupstories.controller;

import com.breakupstories.dto.*;
import com.breakupstories.model.NoContactProfileDocument;
import com.breakupstories.repository.NoContactProfileRepository;
import com.breakupstories.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/heal")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class HealController {

    private static final Logger log = LoggerFactory.getLogger(HealController.class);

    private final NoContactProfileRepository noContactProfileRepository;
    private final UserService userService;

    @PostMapping("/sos-reality-check")
    public ResponseEntity<SosRealityCheckResponse> getSosRealityCheck(@RequestBody SosRealityCheckRequest request) {
        log.info("Received SOS reality check request. Days clean: {}, Urge: {}", 
                request.getDaysClean(), request.getUrgeLevel());

        String rawMsg = request.getMessage() != null ? request.getMessage().trim() : "";
        int days = request.getDaysClean();
        String nickname = (request.getExNickname() != null && !request.getExNickname().trim().isEmpty())
                ? request.getExNickname().trim() : "your ex";

        String realityCheck;
        String predictedResponse;
        String psychologicalExplanation;
        String recommendedAction;
        String streakQuote;

        if (rawMsg.length() > 100) {
            realityCheck = "Sending a long paragraph will not give you closure. It gives " + nickname + 
                    " the reassurance that you are still waiting on them, while making you feel exposed and vulnerable.";
            predictedResponse = "Left on Read (or a cold, dismissive 'k. wish you the best.').";
            psychologicalExplanation = "Paragraphs sent in distress are an attempt to force empathy from someone who chose to detach. Detached minds interpret emotional paragraphs as pressure, not love.";
            recommendedAction = "Take 3 slow, deep breaths. Put your phone face down in another room for 15 minutes.";
        } else {
            realityCheck = "Even a casual 'hey' or checking-in message resets your emotional detachment clock and signals that your boundary is porous.";
            predictedResponse = "Delayed reply hours later with cold polite detachment, or complete silence.";
            psychologicalExplanation = "A short impulse text is driven by a dopamine craving dip. Your brain wants reassurance, but reaching out will only trigger an adrenaline crash when they don't respond warmly.";
            recommendedAction = "Drink a full glass of cold water. Open the Urge Surfer breathing exercise.";
        }

        streakQuote = "You have stayed strong for " + days + " days. Dignity and peace are worth more than a temporary impulse.";

        SosRealityCheckResponse response = SosRealityCheckResponse.builder()
                .success(true)
                .realityCheck(realityCheck)
                .predictedExResponse(predictedResponse)
                .psychologicalExplanation(psychologicalExplanation)
                .recommendedAction(recommendedAction)
                .streakPreservedQuote(streakQuote)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/config")
    public ResponseEntity<HealConfigResponse> getHealConfig() {
        List<HealConfigResponse.MilestoneConfig> milestones = List.of(
            new HealConfigResponse.MilestoneConfig(1, "First Step", "Shock & Withdrawal", "🌱", 
                "Your brain experiences breakup withdrawal just like chemical addiction. Surviving Day 1 takes raw courage.", 
                "Today I choose dignity over a temporary dopamine hit."),
            new HealConfigResponse.MilestoneConfig(3, "Cold Turkey", "The Craving Peak", "🛡️", 
                "Urges peak around Day 3 to Day 5. Every hour you hold the line, your neural pathways begin to rewire.", 
                "I am stronger than an impulsive impulse."),
            new HealConfigResponse.MilestoneConfig(7, "One Week Clean", "Breaking The Cycle", "🔥", 
                "7 consecutive days without contact proves you can function independently. The fog is slowly starting to lift.", 
                "Silence is my boundary and my peace."),
            new HealConfigResponse.MilestoneConfig(14, "Fortnight of Freedom", "Reality Setting In", "⚡", 
                "Two weeks of space stops romanticizing the past and starts showing the relationship as it really was.", 
                "I am no longer addicted to someone who let me go."),
            new HealConfigResponse.MilestoneConfig(21, "Habit Breaker", "Neural Rewiring", "💎", 
                "Science shows 21 days is the foundation for breaking compulsive habits. Checking their socials is fading.", 
                "I am building a life that doesn't revolve around them."),
            new HealConfigResponse.MilestoneConfig(30, "Reclaiming Power", "Emotional Detachment", "👑", 
                "One full month. You preserved your self-respect completely. You no longer react from panic.", 
                "My worth is determined by me, not their validation."),
            new HealConfigResponse.MilestoneConfig(60, "The Unshakable", "Clarity & Rebuilding", "🦅", 
                "Memories no longer trigger an acute panic response. Emotional autonomy is taking full control.", 
                "The grief has turned into wisdom."),
            new HealConfigResponse.MilestoneConfig(90, "Total Liberation", "Rebirth & Peace", "✨", 
                "90 days of No Contact marks full cognitive detachment. You survived what felt impossible on Day 1.", 
                "I am completely free, healed, and proud of who I became.")
        );

        List<HealConfigResponse.DailyAffirmationConfig> affirmations = List.of(
            new HealConfigResponse.DailyAffirmationConfig(1, "Today I choose dignity over a temporary dopamine hit.", "Day 1 is acute withdrawal."),
            new HealConfigResponse.DailyAffirmationConfig(3, "My silence is my power.", "Urges peak around Day 3."),
            new HealConfigResponse.DailyAffirmationConfig(7, "One full week. I am proving to myself that I can survive this.", "One week clean breaks initial reflex."),
            new HealConfigResponse.DailyAffirmationConfig(14, "I am releasing the need for closure from someone who hurt me.", "Closure comes from within."),
            new HealConfigResponse.DailyAffirmationConfig(30, "A full month of choosing my own peace. I am unstoppable.", "Detachment is firmly taking root.")
        );

        return ResponseEntity.ok(HealConfigResponse.builder()
                .success(true)
                .milestones(milestones)
                .dailyAffirmations(affirmations)
                .build());
    }

    @GetMapping("/profile")
    public ResponseEntity<NoContactSyncResponse> getUserProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        String userId = userService.getUserEntityByEmail(email).getId();

        Optional<NoContactProfileDocument> opt = noContactProfileRepository.findByUserId(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.ok(NoContactSyncResponse.builder()
                    .success(true)
                    .message("No profile found for user")
                    .build());
        }

        NoContactProfileDocument doc = opt.get();
        return ResponseEntity.ok(mapDocumentToResponse(doc, "Profile fetched successfully"));
    }

    @PostMapping("/sync")
    public ResponseEntity<NoContactSyncResponse> syncUserProfile(
            @RequestBody NoContactSyncRequest request,
            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        String userId = userService.getUserEntityByEmail(email).getId();

        NoContactProfileDocument doc = noContactProfileRepository.findByUserId(userId)
                .orElse(NoContactProfileDocument.builder()
                        .userId(userId)
                        .createdAt(LocalDateTime.now())
                        .build());

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        if (request.getStartDate() != null) {
            try {
                doc.setStartDate(LocalDateTime.parse(request.getStartDate(), formatter));
            } catch (Exception e) {
                // Ignore parse errors, keep existing
            }
        }
        if (request.getTargetDays() > 0) doc.setTargetDays(request.getTargetDays());
        if (request.getExNickname() != null) doc.setExNickname(request.getExNickname());
        if (request.getMotivation() != null) doc.setMotivation(request.getMotivation());
        if (request.getLastCheckInDate() != null) {
            try {
                doc.setLastCheckInDate(LocalDateTime.parse(request.getLastCheckInDate(), formatter));
            } catch (Exception ignored) {}
        }
        if (request.getSosCount() >= 0) doc.setSosCount(request.getSosCount());

        if (request.getCheckIns() != null) {
            List<NoContactProfileDocument.MoodCheckInEntry> entries = request.getCheckIns().stream()
                    .map(c -> NoContactProfileDocument.MoodCheckInEntry.builder()
                            .date(parseIsoDateTime(c.getDate()))
                            .mood(c.getMood())
                            .urgeLevel(c.getUrgeLevel())
                            .note(c.getNote())
                            .build())
                    .collect(Collectors.toList());
            doc.setCheckIns(entries);
        }

        if (request.getResistedMessages() != null) {
            List<NoContactProfileDocument.ResistedMessageEntry> entries = request.getResistedMessages().stream()
                    .map(m -> NoContactProfileDocument.ResistedMessageEntry.builder()
                            .id(m.getId())
                            .timestamp(parseIsoDateTime(m.getTimestamp()))
                            .content(m.getContent())
                            .urgeLevel(m.getUrgeLevel())
                            .burned(m.isBurned())
                            .build())
                    .collect(Collectors.toList());
            doc.setResistedMessages(entries);
        }

        doc.setUpdatedAt(LocalDateTime.now());
        NoContactProfileDocument saved = noContactProfileRepository.save(doc);

        return ResponseEntity.ok(mapDocumentToResponse(saved, "Streak synced to cloud successfully"));
    }

    private LocalDateTime parseIsoDateTime(String str) {
        if (str == null) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(str, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private NoContactSyncResponse mapDocumentToResponse(NoContactProfileDocument doc, String message) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        List<NoContactSyncRequest.SyncMoodCheckIn> checkIns = doc.getCheckIns() != null
                ? doc.getCheckIns().stream()
                .map(c -> NoContactSyncRequest.SyncMoodCheckIn.builder()
                        .date(c.getDate() != null ? c.getDate().format(formatter) : null)
                        .mood(c.getMood())
                        .urgeLevel(c.getUrgeLevel())
                        .note(c.getNote())
                        .build())
                .collect(Collectors.toList())
                : new ArrayList<>();

        List<NoContactSyncRequest.SyncResistedMessage> messages = doc.getResistedMessages() != null
                ? doc.getResistedMessages().stream()
                .map(m -> NoContactSyncRequest.SyncResistedMessage.builder()
                        .id(m.getId())
                        .timestamp(m.getTimestamp() != null ? m.getTimestamp().format(formatter) : null)
                        .content(m.getContent())
                        .urgeLevel(m.getUrgeLevel())
                        .burned(m.isBurned())
                        .build())
                .collect(Collectors.toList())
                : new ArrayList<>();

        return NoContactSyncResponse.builder()
                .success(true)
                .message(message)
                .startDate(doc.getStartDate() != null ? doc.getStartDate().format(formatter) : null)
                .targetDays(doc.getTargetDays())
                .exNickname(doc.getExNickname())
                .motivation(doc.getMotivation())
                .lastCheckInDate(doc.getLastCheckInDate() != null ? doc.getLastCheckInDate().format(formatter) : null)
                .sosCount(doc.getSosCount())
                .checkIns(checkIns)
                .resistedMessages(messages)
                .build();
    }
}
