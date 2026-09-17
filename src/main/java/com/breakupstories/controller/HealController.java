package com.breakupstories.controller;

import com.breakupstories.dto.SosRealityCheckRequest;
import com.breakupstories.dto.SosRealityCheckResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/heal")
@CrossOrigin(origins = "*")
public class HealController {

    private static final Logger log = LoggerFactory.getLogger(HealController.class);

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

        // Tailor psychology based on message length and days
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

    @GetMapping("/daily-affirmation")
    public ResponseEntity<Map<String, Object>> getDailyAffirmation(@RequestParam(defaultValue = "1") int day) {
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("day", day);

        String affirmation;
        String fact;

        if (day <= 3) {
            affirmation = "Today I choose dignity over a temporary dopamine hit.";
            fact = "Days 1 to 3 are intense chemical withdrawal. Every hour you resist texting re-regulates your nervous system.";
        } else if (day <= 7) {
            affirmation = "Silence is my loudest boundary and my deepest self-respect.";
            fact = "One week clean breaks the immediate behavioral reflex. The urge wave peaks and begins to subside.";
        } else if (day <= 21) {
            affirmation = "I am rebuilding a peaceful life that does not depend on their validation.";
            fact = "At 21 days, neural pathways shift from craving attachment to building independent habits.";
        } else if (day <= 30) {
            affirmation = "My worth was never tied to their ability to see it.";
            fact = "One month of No Contact restores cognitive clarity. Romanticizing gives way to objective memory.";
        } else {
            affirmation = "I am healed, whole, and grateful for my resilience.";
            fact = "Long-term No Contact transforms emotional grief into lasting wisdom and self-trust.";
        }

        res.put("affirmation", affirmation);
        res.put("psychologicalFact", fact);

        return ResponseEntity.ok(res);
    }
}
