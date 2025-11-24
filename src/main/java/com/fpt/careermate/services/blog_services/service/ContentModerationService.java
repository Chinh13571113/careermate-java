package com.fpt.careermate.services.blog_services.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for automatic content moderation
 * Detects profanity, controversial topics, and inappropriate content
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ContentModerationService {

    // Profanity and inappropriate words (comprehensive list)
    static Set<String> PROFANITY_WORDS = new HashSet<>(Arrays.asList(
            // F-word family
            "fuck", "fucking", "fucked", "fucker", "fuk", "fck", "fook", "phuck", "feck",
            // S-word family
            "shit", "shitty", "shitting", "shite", "sht", "shyt", "crap", "crappy", "crapping",
            // A-word family
            "ass", "asshole", "arse", "arsehole", "azz", "butthole", "butt",
            // B-word family
            "bitch", "bitchy", "bitching", "biatch", "beotch", "bish",
            // D-word family
            "damn", "damned", "dammit", "damnit", "dang",
            // P-word family
            "piss", "pissed", "pissing", "pissoff",
            // C-word family (explicit)
            "cock", "cunt", "dick", "pussy", "penis", "vagina", "balls", "ballsack", "nutsack",
            // Bastard family
            "bastard", "basterd", "basstard",

            // Sexual/Explicit content
            "porn", "porno", "pornography", "sex", "sexy", "nude", "naked", "xxx", "nsfw",
            "boobs", "tits", "titties", "breast", "nipple", "cum", "cumming", "jizz", "sperm",
            "orgasm", "masturbate", "masturbation", "horny", "erection", "boner",
            "blowjob", "handjob", "anal", "oral", "69", "dildo", "vibrator",
            "whore", "slut", "hooker", "prostitute", "hoe", "thot",

            // Animal-based insults
            "dog", "bitch", "pig", "cow", "donkey", "jackass", "ape", "monkey",
            "rat", "snake", "worm", "scum", "vermin", "beast",

            // Body parts used as insults
            "dickhead", "prick", "knob", "wanker", "tosser",

            // Offensive slurs (racial/discriminatory)
            "nigger", "nigga", "niga", "negro", "faggot", "fag", "dyke", "tranny",
            "retard", "retarded", "spastic", "mongoloid", "cripple",
            "idiot", "moron", "stupid", "dumb", "dumbass", "imbecile",

            // Violence/Death
            "kill", "murder", "rape", "suicide", "bomb", "terrorist", "die", "death",
            "stab", "shoot", "strangle", "torture", "mutilate", "slaughter",

            // Compound profanity
            "bullshit", "horseshit", "chickenshit", "dogshit", "dipshit", "apeshit",
            "batshit", "catshit", "ratshit",
            "shithead", "shitface", "shitbag", "shitstain",
            "dumbass", "jackass", "smartass", "badass", "fatass", "lardass",
            "motherfucker", "sisterfucker", "asswipe", "asshat", "assclown", "asslicker",
            "fuckface", "fuckhead", "fuckwit", "fucktard", "fuckboy",
            "bitchass", "bitchface", "sonofabitch",

            // Bodily functions
            "poop", "turd", "fart", "puke", "vomit", "snot", "booger",

            // Derogatory terms
            "loser", "scumbag", "dirtbag", "douche", "douchebag", "jerk", "creep",
            "pervert", "perv", "sicko", "freak", "weirdo", "psycho",
            "trash", "garbage", "scum", "filth", "sleaze", "slime",

            // Internet slang/abbreviations
            "wtf", "stfu", "gtfo", "omfg", "af", "smh", "kys", "fml",
            "pos", "sob"));

    // Controversial topics that require review
    static Set<String> CONTROVERSIAL_KEYWORDS = new HashSet<>(Arrays.asList(
            // Political extremism
            "nazi", "hitler", "fascist", "fascism", "communist", "communism", "propaganda", "dictator",
            // Religious extremism
            "jihad", "infidel", "crusade", "extremist",
            // Discrimination
            "racist", "racism", "sexist", "sexism", "homophobic", "homophobia", "xenophobic", "xenophobia",
            // Hate speech
            "hate", "hatred", "supremacist", "genocide",
            // Drugs
            "cocaine", "heroin", "meth", "weed", "marijuana", "drug dealer",
            // Spam indicators
            "click here", "buy now", "limited offer", "prize winner", "free money",
            "earn from home", "work from home scam", "get rich quick", "mlm", "pyramid scheme"));

    // Patterns for detecting problematic content
    static List<Pattern> SUSPICIOUS_PATTERNS = Arrays.asList(
            // Multiple exclamation/question marks
            Pattern.compile("[!?]{3,}"),
            // All caps (possible spam/shouting)
            Pattern.compile("\\b[A-Z]{5,}\\b"),
            // Excessive repetition
            Pattern.compile("(.)\\1{4,}"),
            // URLs (potential spam)
            Pattern.compile("(https?://|www\\.)\\S+"),
            // Email addresses (potential spam)
            Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"),
            // Phone numbers (potential spam)
            Pattern.compile("\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b"));

    /**
     * Analyze content and determine if it should be flagged
     * Returns ModerationResult with flagging decision and reasons
     */
    public ModerationResult analyzeContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new ModerationResult(false, null);
        }

        List<String> flagReasons = new ArrayList<>();
        String normalizedContent = content.toLowerCase().trim();

        // Check for profanity
        Set<String> foundProfanity = detectProfanity(normalizedContent);
        if (!foundProfanity.isEmpty()) {
            flagReasons.add("Profanity detected: " + String.join(", ", foundProfanity));
        }

        // Check for controversial keywords
        Set<String> foundControversial = detectControversialKeywords(normalizedContent);
        if (!foundControversial.isEmpty()) {
            flagReasons.add("Controversial content: " + String.join(", ", foundControversial));
        }

        // Check for suspicious patterns
        List<String> patternMatches = detectSuspiciousPatterns(content);
        if (!patternMatches.isEmpty()) {
            flagReasons.add("Suspicious patterns: " + String.join(", ", patternMatches));
        }

        // Check for excessive length (potential spam)
        if (content.length() > 2000) {
            flagReasons.add("Excessive length (possible spam)");
        }

        // Determine if should be flagged
        boolean shouldFlag = !flagReasons.isEmpty();
        String combinedReason = shouldFlag ? String.join(" | ", flagReasons) : null;

        if (shouldFlag) {
            log.warn("Content flagged for moderation: {}", combinedReason);
        }

        return new ModerationResult(shouldFlag, combinedReason);
    }

    /**
     * Detect profanity in content
     * Catches exact matches, variations with special chars, and spaced variations
     */
    private Set<String> detectProfanity(String normalizedContent) {
        Set<String> found = new HashSet<>();

        // Remove common obfuscation techniques for better detection
        String cleanedContent = normalizedContent
                .replaceAll("[\\*\\@\\#\\$\\&]", "") // Remove special chars
                .replaceAll("\\s+", " "); // Normalize spaces

        for (String word : PROFANITY_WORDS) {
            // 1. Check for exact word match with word boundaries
            Pattern exactPattern = Pattern.compile("\\b" + Pattern.quote(word) + "\\b", Pattern.CASE_INSENSITIVE);
            if (exactPattern.matcher(normalizedContent).find()) {
                found.add(word);
                continue;
            }

            // 2. Check for variations with special characters (e.g., "f*ck", "sh!t")
            String specialCharRegex = word.chars()
                    .mapToObj(c -> "[" + (char) c + "\\*\\@\\#\\$\\&]")
                    .collect(Collectors.joining());
            Pattern specialPattern = Pattern.compile("\\b" + specialCharRegex + "\\b", Pattern.CASE_INSENSITIVE);
            if (specialPattern.matcher(normalizedContent).find()) {
                found.add(word + " (obfuscated)");
                continue;
            }

            // 3. Check for spaced variations (e.g., "f u c k", "s h i t")
            String spacedRegex = word.chars()
                    .mapToObj(c -> String.valueOf((char) c))
                    .collect(Collectors.joining("\\s*"));
            Pattern spacedPattern = Pattern.compile("\\b" + spacedRegex + "\\b", Pattern.CASE_INSENSITIVE);
            if (spacedPattern.matcher(normalizedContent).find()) {
                found.add(word + " (spaced)");
                continue;
            }

            // 4. Check in cleaned content (after removing obfuscation)
            if (cleanedContent.contains(word)) {
                found.add(word + " (hidden)");
            }
        }

        return found;
    }

    /**
     * Detect controversial keywords
     */
    private Set<String> detectControversialKeywords(String normalizedContent) {
        Set<String> found = new HashSet<>();

        for (String keyword : CONTROVERSIAL_KEYWORDS) {
            if (normalizedContent.contains(keyword)) {
                found.add(keyword);
            }
        }

        return found;
    }

    /**
     * Detect suspicious patterns
     */
    private List<String> detectSuspiciousPatterns(String content) {
        List<String> matches = new ArrayList<>();

        for (Pattern pattern : SUSPICIOUS_PATTERNS) {
            if (pattern.matcher(content).find()) {
                matches.add(pattern.pattern());
            }
        }

        return matches;
    }

    /**
     * Calculate severity score (0-100)
     * Higher score = more severe content
     * Multiple violations compound the score
     */
    public int calculateSeverityScore(ModerationResult result) {
        if (!result.shouldFlag) {
            return 0;
        }

        int score = 0;
        String reason = result.flagReason.toLowerCase();

        // Count profanity occurrences (each instance: +40 points)
        if (reason.contains("profanity")) {
            // Count how many profanity words were detected
            long profanityCount = Arrays.stream(reason.split("\\|"))
                    .filter(s -> s.contains("profanity"))
                    .count();

            // Check for multiple instances in the profanity section
            if (reason.contains("profanity detected:")) {
                String profanitySection = reason.substring(reason.indexOf("profanity detected:"));
                long wordCount = Arrays.stream(profanitySection.split(",")).count();
                score += Math.min(wordCount * 40, 100); // Cap at 100
            } else {
                score += 40;
            }
        }

        // Controversial: +30 points
        if (reason.contains("controversial")) {
            score += 30;
        }

        // Suspicious patterns: +20 points
        if (reason.contains("suspicious patterns")) {
            score += 20;
        }

        // Excessive length: +10 points
        if (reason.contains("excessive length")) {
            score += 10;
        }

        return Math.min(score, 100);
    }

    /**
     * Get priority level based on severity
     */
    public String getPriorityLevel(int severityScore) {
        if (severityScore >= 70) {
            return "HIGH";
        } else if (severityScore >= 40) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * Result of content moderation analysis
     */
    public static class ModerationResult {
        public final boolean shouldFlag;
        public final String flagReason;

        public ModerationResult(boolean shouldFlag, String flagReason) {
            this.shouldFlag = shouldFlag;
            this.flagReason = flagReason;
        }
    }

    /**
     * Add custom profanity word (for dynamic configuration)
     */
    public void addProfanityWord(String word) {
        PROFANITY_WORDS.add(word.toLowerCase());
        log.info("Added custom profanity word: {}", word);
    }

    /**
     * Add custom controversial keyword (for dynamic configuration)
     */
    public void addControversialKeyword(String keyword) {
        CONTROVERSIAL_KEYWORDS.add(keyword.toLowerCase());
        log.info("Added custom controversial keyword: {}", keyword);
    }

    /**
     * Get statistics about flagging rules
     */
    public Map<String, Object> getModerationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProfanityWords", PROFANITY_WORDS.size());
        stats.put("totalControversialKeywords", CONTROVERSIAL_KEYWORDS.size());
        stats.put("totalSuspiciousPatterns", SUSPICIOUS_PATTERNS.size());
        return stats;
    }
}
