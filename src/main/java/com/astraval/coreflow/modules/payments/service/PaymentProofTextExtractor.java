package com.astraval.coreflow.modules.payments.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRParser;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentProofTextExtractor {

    private static final int MAX_TEXT_CHARS = 200_000;

    @Value("${app.ocr.tesseract.language:eng}")
    private String tesseractLanguage;

    @Value("${app.ocr.tesseract.pageSegMode:6}")
    private String tesseractPageSegMode;

    @Value("${app.ocr.tesseract.density:300}")
    private int tesseractDensity;

    @Value("${app.ocr.tesseract.timeoutSeconds:120}")
    private int tesseractTimeoutSeconds;

    @Value("${app.ocr.tesseract.maxFileSizeToOcr:10485760}")
    private long tesseractMaxFileSizeToOcr;

    @Value("${app.ocr.tesseract.minFileSizeToOcr:0}")
    private long tesseractMinFileSizeToOcr;

    @Value("${app.ocr.tesseract.preprocess:true}")
    private boolean tesseractPreprocess;

    @Value("${app.ocr.tesseract.applyRotation:true}")
    private boolean tesseractApplyRotation;

    @Value("${app.ocr.tesseract.preserveInterwordSpacing:true}")
    private boolean tesseractPreserveInterwordSpacing;

    @Value("${app.ocr.tesseract.cli:tesseract}")
    private String tesseractCli;

    private static final List<Pattern> TRANSACTION_ID_PATTERNS = new ArrayList<>();
    private static final List<Pattern> AMOUNT_PATTERNS = new ArrayList<>();

    static {
        TRANSACTION_ID_PATTERNS.add(Pattern.compile(
                "(UTR|UTR\\s*No|UTR\\s*Number|Transaction\\s*ID|Transaction\\s*No|Txn\\s*ID|Txn\\s*No|"
                        + "Reference\\s*No|Reference\\s*Number|Ref\\s*No|RRN|Bank\\s*Ref|"
                        + "Transaction\\s*Reference|UPI\\s*Transaction)\\s*[:#-]?\\s*([A-Z0-9-]{6,})",
                Pattern.CASE_INSENSITIVE));
        TRANSACTION_ID_PATTERNS.add(Pattern.compile(
                "(UPI\\s*Ref|UPI\\s*Ref\\s*No|UPI\\s*Txn|UPI\\s*Txn\\s*ID|UPI\\s*Transaction\\s*ID|"
                        + "Google\\s*Transaction\\s*ID|GPay\\s*Transaction\\s*ID|G\\s*Pay\\s*Transaction\\s*ID)\\s*[:#-]?\\s*([A-Z0-9-]{6,})",
                Pattern.CASE_INSENSITIVE));
        TRANSACTION_ID_PATTERNS.add(Pattern.compile(
                "\\b([0-9]{8,20})\\b"));

        AMOUNT_PATTERNS.add(Pattern.compile(
                "(Amount\\s*(Paid|Debited|Transferred|Credited)?|Total|Total\\s*Amount|Total\\s*Charge\\s*Amount|"
                        + "Txn\\s*Amount|Transaction\\s*Amount|Paid\\s*Amount|Amount)\\s*[:#-]?\\s*(INR|RS\\.?|RUPEES)?\\s*"
                        + "([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)",
                Pattern.CASE_INSENSITIVE));
        AMOUNT_PATTERNS.add(Pattern.compile(
                "\\b(INR|RS\\.?|RUPEES)\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)\\b",
                Pattern.CASE_INSENSITIVE));
    }

    public String extractText(String filePath, String mimeType) {
        if (filePath == null || filePath.isBlank()) {
            return "";
        }
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return "";
        }

        try (InputStream input = Files.newInputStream(path)) {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(MAX_TEXT_CHARS);
            Metadata metadata = new Metadata();
            if (mimeType != null && !mimeType.isBlank()) {
                metadata.set(Metadata.CONTENT_TYPE, mimeType);
            }
            ParseContext context = new ParseContext();
            TesseractOCRConfig ocrConfig = new TesseractOCRConfig();
            if (tesseractLanguage != null && !tesseractLanguage.isBlank()) {
                ocrConfig.setLanguage(tesseractLanguage);
            }
            if (tesseractPageSegMode != null && !tesseractPageSegMode.isBlank()) {
                ocrConfig.setPageSegMode(tesseractPageSegMode);
            }
            if (tesseractDensity > 0) {
                ocrConfig.setDensity(tesseractDensity);
            }
            if (tesseractTimeoutSeconds > 0) {
                ocrConfig.setTimeoutSeconds(tesseractTimeoutSeconds);
            }
            if (tesseractMaxFileSizeToOcr > 0) {
                ocrConfig.setMaxFileSizeToOcr(tesseractMaxFileSizeToOcr);
            }
            if (tesseractMinFileSizeToOcr >= 0) {
                ocrConfig.setMinFileSizeToOcr(tesseractMinFileSizeToOcr);
            }
            ocrConfig.setEnableImagePreprocessing(tesseractPreprocess);
            ocrConfig.setApplyRotation(tesseractApplyRotation);
            ocrConfig.setPreserveInterwordSpacing(tesseractPreserveInterwordSpacing);
            ocrConfig.setSkipOcr(false);
            context.set(TesseractOCRConfig.class, ocrConfig);
            parser.parse(input, handler, metadata, context);
            String content = handler.toString();
            if (mimeType != null && mimeType.startsWith("image/")) {
                BodyContentHandler imageHandler = new BodyContentHandler(MAX_TEXT_CHARS);
                TesseractOCRParser ocrParser = new TesseractOCRParser();
                ocrParser.parse(Files.newInputStream(path), imageHandler, metadata, context);
                String imageText = imageHandler.toString();
                if (imageText == null || imageText.isBlank() || imageText.length() < 20) {
                    String cliText = extractViaTesseractCli(path, tesseractPageSegMode);
                    if (cliText != null && !cliText.isBlank()) {
                        imageText = cliText;
                    }
                }
                if (imageText == null || imageText.isBlank() || imageText.length() < 20) {
                    String cliAlt = extractViaTesseractCli(path, "11");
                    if (cliAlt != null && cliAlt.length() > (imageText == null ? 0 : imageText.length())) {
                        imageText = cliAlt;
                    }
                }
                if (imageText == null || imageText.isBlank() || imageText.length() < 20) {
                    String cliAlt = extractViaTesseractCli(path, "4");
                    if (cliAlt != null && cliAlt.length() > (imageText == null ? 0 : imageText.length())) {
                        imageText = cliAlt;
                    }
                }
                return imageText;
            }
            return content;
        } catch (Exception e) {
            return "";
        }
    }

    private String extractViaTesseractCli(Path path, String psmOverride) {
        if (path == null || !Files.exists(path)) {
            return "";
        }
        try {
            List<String> command = new ArrayList<>();
            command.add(tesseractCli != null && !tesseractCli.isBlank() ? tesseractCli : "tesseract");
            command.add(path.toString());
            command.add("stdout");
            command.add("-l");
            command.add(tesseractLanguage != null && !tesseractLanguage.isBlank() ? tesseractLanguage : "eng");
            String psmValue = (psmOverride != null && !psmOverride.isBlank()) ? psmOverride : tesseractPageSegMode;
            if (psmValue != null && !psmValue.isBlank()) {
                command.add("--psm");
                command.add(psmValue);
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }

            boolean finished = process.waitFor(Math.max(5, tesseractTimeoutSeconds), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "";
            }

            if (process.exitValue() != 0) {
                return "";
            }

            return output.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public String extractTransactionId(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = text.replaceAll("\\s+", " ").trim();

        for (Pattern pattern : TRANSACTION_ID_PATTERNS) {
            Matcher matcher = pattern.matcher(normalized);
            if (matcher.find()) {
                String candidate = matcher.group(matcher.groupCount());
                if (candidate != null && !candidate.isBlank()) {
                    String trimmed = candidate.trim();
                    if (trimmed.matches(".*\\d.*")) {
                        return trimmed;
                    }
                }
            }
        }
        return null;
    }

    public Double extractAmount(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = text.replaceAll("\\s+", " ").trim();

        for (Pattern pattern : AMOUNT_PATTERNS) {
            Matcher matcher = pattern.matcher(normalized);
            if (matcher.find()) {
                String raw = matcher.group(matcher.groupCount());
                if (!raw.matches(".*\\d.*")) {
                    continue;
                }
                Double parsed = parseAmount(raw);
                if (parsed != null) {
                    return parsed;
                }
            }
        }
        Double lineBased = extractAmountFromLines(text);
        if (lineBased != null) {
            return lineBased;
        }
        return null;
    }

    private Double parseAmount(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String cleaned = raw.replaceAll("[^0-9.,]", "");
        if (cleaned.isBlank()) {
            return null;
        }
        cleaned = cleaned.replaceAll(",", "");
        try {
            Double value = Double.valueOf(cleaned);
            if (value == 0.0) {
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double extractAmountFromLines(String text) {
        String[] lines = text.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            String lower = line.toLowerCase();
            if (lower.contains("amount") || lower.contains("total") || lower.contains("charge")) {
                String candidate = line;
                if (i + 1 < lines.length) {
                    candidate = candidate + " " + lines[i + 1].trim();
                }
                if (!candidate.matches(".*\\d.*")) {
                    continue;
                }
                String corrected = correctOcrDigits(candidate);
                Double parsed = parseAmount(corrected);
                if (parsed != null) {
                    return parsed;
                }
            }
        }
        return null;
    }

    private String correctOcrDigits(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == 'O' || c == 'o') {
                sb.append('0');
            } else if (c == 'I' || c == 'l' || c == '|' || c == '!' ) {
                sb.append('1');
            } else if (c == 'S' || c == 's') {
                sb.append('5');
            } else if (c == 'B') {
                sb.append('8');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public String getTesseractVersion() {
        try {
            List<String> command = new ArrayList<>();
            command.add(tesseractCli != null && !tesseractCli.isBlank() ? tesseractCli : "tesseract");
            command.add("--version");
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append('\n');
                }
            }

            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return null;
            }
            if (process.exitValue() != 0) {
                return null;
            }
            String text = output.toString().trim();
            if (text.isBlank()) {
                return null;
            }
            String firstLine = text.split("\\R", 2)[0];
            return firstLine.trim();
        } catch (Exception e) {
            return null;
        }
    }
}
