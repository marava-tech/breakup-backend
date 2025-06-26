package com.breakupstories.enums;

public enum LANGUAGE {
    ENGLISH,
    HINDI,
    TELUGU,
    TAMIL,
    KANNADA,
    MALAYALAM,
    BENGALI,
    MARATHI,
    GUJARATI,
    PUNJABI,
    URDU,
    ORIYA,
    ASSAMESE,
    KASHMIRI,
    SINDHI,
    NEPALI,
    KONKANI,
    MANIPURI,
    SANSKRIT,
    DOGRI,
    BODO,
    SANTALI,
    MAITHILI,
    KONKANI_GOA;

    /**
     * Get LANGUAGE enum from string (case-insensitive)
     * @param languageStr The language string (e.g., "telugu", "TELUGU", "Telugu")
     * @return LANGUAGE enum or null if not found
     */
    public static LANGUAGE fromString(String languageStr) {
        if (languageStr == null || languageStr.trim().isEmpty()) {
            return null;
        }
        
        String normalized = languageStr.trim().toUpperCase();
        
        // Handle special cases
        if ("KONKANI".equals(normalized)) {
            return KONKANI_GOA; // Default to Goa Konkani
        }
        
        try {
            return LANGUAGE.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Get display name for the language
     * @return Display name in English
     */
    public String getDisplayName() {
        switch (this) {
            case ENGLISH: return "English";
            case HINDI: return "Hindi";
            case TELUGU: return "Telugu";
            case TAMIL: return "Tamil";
            case KANNADA: return "Kannada";
            case MALAYALAM: return "Malayalam";
            case BENGALI: return "Bengali";
            case MARATHI: return "Marathi";
            case GUJARATI: return "Gujarati";
            case PUNJABI: return "Punjabi";
            case URDU: return "Urdu";
            case ORIYA: return "Oriya";
            case ASSAMESE: return "Assamese";
            case KASHMIRI: return "Kashmiri";
            case SINDHI: return "Sindhi";
            case NEPALI: return "Nepali";
            case KONKANI: return "Konkani";
            case MANIPURI: return "Manipuri";
            case SANSKRIT: return "Sanskrit";
            case DOGRI: return "Dogri";
            case BODO: return "Bodo";
            case SANTALI: return "Santali";
            case MAITHILI: return "Maithili";
            case KONKANI_GOA: return "Konkani (Goa)";
            default: return this.name();
        }
    }
    
    /**
     * Get native name for the language
     * @return Native name in the language's script
     */
    public String getNativeName() {
        switch (this) {
            case ENGLISH: return "English";
            case HINDI: return "हिन्दी";
            case TELUGU: return "తెలుగు";
            case TAMIL: return "தமிழ்";
            case KANNADA: return "ಕನ್ನಡ";
            case MALAYALAM: return "മലയാളം";
            case BENGALI: return "বাংলা";
            case MARATHI: return "मराठी";
            case GUJARATI: return "ગુજરાતી";
            case PUNJABI: return "ਪੰਜਾਬੀ";
            case URDU: return "اردو";
            case ORIYA: return "ଓଡ଼ିଆ";
            case ASSAMESE: return "অসমীয়া";
            case KASHMIRI: return "کٲشُر";
            case SINDHI: return "سنڌي";
            case NEPALI: return "नेपाली";
            case KONKANI: return "कोंकणी";
            case MANIPURI: return "মৈতৈলোন্";
            case SANSKRIT: return "संस्कृतम्";
            case DOGRI: return "डोगरी";
            case BODO: return "बड़ो";
            case SANTALI: return "ᱥᱟᱱᱛᱟᱲᱤ";
            case MAITHILI: return "मैथिली";
            case KONKANI_GOA: return "कोंकणी";
            default: return this.name();
        }
    }
}
