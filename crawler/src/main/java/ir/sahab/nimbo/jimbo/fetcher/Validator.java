package ir.sahab.nimbo.jimbo.fetcher;

import com.google.common.base.Optional;
import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Validator {

    private static final Logger logger = LoggerFactory.getLogger(Validator.class);

    static List<String> banWords = null;
    private static TextObjectFactory textObjectFactory = null;
    private static LanguageDetector languageDetector = null;
    private static List<LanguageProfile> languageProfiles = null;
    private static final double acceptProbability = 0.5;

    static {
        init();
    }

    public static boolean isValidBody(Document document) {
        return isEnglish(document.text()) && isNotBan(document);
    }

    static boolean isValidUrl(URL url) {
        return isBadUrl(url);
    }

    public static boolean allValidation(Document document){
        String article = document.text();
        if (article.length() > 400)
            article = article.substring(0, 400);
        return isEnglish(article) && isNotBan(document) && isNotBanBody(article);

    }

    static boolean isEnglish(String article) {
        TextObject textObject = textObjectFactory.forText(article);
        Optional<LdLocale> lang = languageDetector.detect(textObject);

        if (lang.isPresent()) {
            return lang.get().toString().equals("en");
        }
        double tmp = 1.0;
        for (DetectedLanguage detectedLanguage : languageDetector.getProbabilities(article)) {
            if (tmp < acceptProbability) return false;
            if (detectedLanguage.getProbability() > acceptProbability) {
                return detectedLanguage.getLocale().toString().equals("en");
            }
            tmp -= detectedLanguage.getProbability();
        }
        return false;
    }

    static boolean isBadUrl(URL url) {
        for (String word : banWords) {
            if ((url.getQuery() != null && url.getQuery().toLowerCase().contains(word))
                    || (url.getHost() != null && url.getHost().toLowerCase().contains(word)))
                return true;
        }
        return false;
    }

    static boolean isNotBan(Document document) {
        for (String word : banWords) {
            if ((document.title() != null && document.title().toLowerCase().contains(word))
                    || (document.head() != null && document.head().text().toLowerCase().contains(word))) {
                return false;
            }
        }
        return true;
    }

    // TODO: can add body check if our speed is ok
    static boolean isNotBanBody(String article){
        for (String word : banWords) {
            if ((article != null && article.contains(word))) {
                return false;
            }
        }
        return true;
    }

    private static void initLanguageDetect() {
        //        DetectLanguage.apiKey = "2806a039edb701c9b56b642b9a63a0ac";

        languageProfiles = null;
        try {
            languageProfiles = new LanguageProfileReader().readAllBuiltIn();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
        textObjectFactory = CommonTextObjectFactories.forDetectingOnLargeText();

    }

    private static void initBanWords() {
        banWords = new ArrayList<>();
        String PROP_DIR = "banWords";
        Scanner inp = new Scanner(ClassLoader.getSystemResourceAsStream(PROP_DIR));
        while (inp.hasNext()) {
            banWords.add(inp.next());
        }
    }

    static void init() {
        initLanguageDetect();
        initBanWords();
    }

}
