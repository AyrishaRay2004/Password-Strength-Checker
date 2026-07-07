import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 *  1. Real-time password evaluation using String manipulation + Regex.
 *  2. Classifies password as Weak / Medium / Strong.
 *  3. Cryptography concept: generates a random SALT and computes a
 *     SHA-256 HASH of the password (salted hashing), demonstrating how
 *     real systems store passwords securely instead of as plain text.
 */
public class PasswordStrengthChecker {

    // Regex patterns for complexity rules
    private static final Pattern UPPERCASE   = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE   = Pattern.compile("[a-z]");
    private static final Pattern DIGIT       = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[^a-zA-Z0-9]"); // anything not alphanumeric
    private static final int MIN_LENGTH = 8;
    private static final int STRONG_LENGTH = 12;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================================");
        System.out.println("   PASSWORD STRENGTH CHECKER  (with Cryptography)");
        System.out.println("=================================================");
        System.out.println("Type a password to evaluate it in real time.");
        System.out.println("Type 'exit' to quit.\n");

        while (true) {
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            if (password.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            if (password.isEmpty()) {
                System.out.println("Password cannot be empty. Try again.\n");
                continue;
            }

            evaluatePassword(password);
            System.out.println("-------------------------------------------------\n");
        }

        scanner.close();
    }

    
    private static void evaluatePassword(String password) {
        int score = 0;

        boolean hasMinLength    = password.length() >= MIN_LENGTH;
        boolean hasStrongLength = password.length() >= STRONG_LENGTH;
        boolean hasUpper        = UPPERCASE.matcher(password).find();
        boolean hasLower        = LOWERCASE.matcher(password).find();
        boolean hasDigit        = DIGIT.matcher(password).find();
        boolean hasSpecial      = SPECIAL_CHAR.matcher(password).find();
        boolean noWhitespace    = !password.contains(" ");

        // Basic string manipulation check: repeated characters (e.g. "aaaa")
        boolean noObviousRepeat = !hasLongRepeat(password);

        if (hasMinLength) score++;
        if (hasUpper) score++;
        if (hasLower) score++;
        if (hasDigit) score++;
        if (hasSpecial) score++;
        if (hasStrongLength) score++;
        if (noObviousRepeat) score++;

        // ---------- Feedback ----------
        System.out.println("\nRule check:");
        printRule("At least 8 characters", hasMinLength);
        printRule("12+ characters (bonus)", hasStrongLength);
        printRule("Contains uppercase letter", hasUpper);
        printRule("Contains lowercase letter", hasLower);
        printRule("Contains digit", hasDigit);
        printRule("Contains special character", hasSpecial);
        printRule("No spaces", noWhitespace);
        printRule("No long repeated-character runs", noObviousRepeat);

        // ---------- Classification ----------
        String strength;
        if (!hasMinLength || !noWhitespace || score <= 2) {
            strength = "WEAK";
        } else if (score <= 5) {
            strength = "MEDIUM";
        } else {
            strength = "STRONG";
        }

        System.out.println("\n>>> Password Strength: " + strength + " (score: " + score + "/7)");

        // Cryptography demonstration
        demonstrateHashing(password);
    }

    private static void printRule(String label, boolean passed) {
        System.out.println((passed ? "  [OK]  " : "  [ X ] ") + label);
    }

    /** Detects 4 or more of the same character in a row, e.g. "aaaa" or "1111". */
    private static boolean hasLongRepeat(String password) {
        int repeatCount = 1;
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == password.charAt(i - 1)) {
                repeatCount++;
                if (repeatCount >= 4) return true;
            } else {
                repeatCount = 1;
            }
        }
        return false;
    }

    private static void demonstrateHashing(String password) {
        try {
            byte[] salt = generateSalt();
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hash = hashPassword(password, salt);

            System.out.println("\n[Cryptography] Secure storage simulation:");
            System.out.println("  Salt (Base64) : " + saltBase64);
            System.out.println("  SHA-256 Hash  : " + hash);
            System.out.println("  Note: The system would store ONLY the salt + hash,");
            System.out.println("        never the original password.");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Hashing algorithm not available: " + e.getMessage());
        }
    }

    //Generates a cryptographically secure random 16-byte salt
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    /** Computes SHA-256 hash of (salt + password), returned as a hex string. */
    private static String hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(salt);
        byte[] hashedBytes = digest.digest(password.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashedBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
