package com.example.crio.cred.Utils;

import lombok.experimental.UtilityClass;
import java.security.Key;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@UtilityClass
public class Utils {

    private static final String ALGO = "AES";
    private static final byte[] keyValue = new byte[] {'C', 'R', 'I', 'O', 'C', 'R', 'I', 'O', 'C',
            'R', 'I', 'O', 'C', 'R', 'I', 'O'};


    public LocalDateTime getDateTime() {
        return LocalDateTime.now(ZoneId.systemDefault());
    }

    public long differenceInDays(String monthYear) {
        String month =  monthYear.split("/")[0];
        Integer monthInt = 0;
        if(month.charAt(0) == '0'){
            monthInt = Integer.parseInt(month.substring(1));
        }else{
            monthInt = Integer.parseInt(month);
        }
        DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("[yyyy][yy]");
        YearMonth yearMonth = YearMonth.of(
            Year.parse(monthYear.split("/")[1], YEAR_FORMAT).getValue(),
            monthInt);
         String date = yearMonth+"-01";
        LocalDate localDate1 = LocalDate.parse(date);
        LocalDate localDate2 = LocalDate.now();

        // calculate difference
        long days = Duration.between(localDate2.atStartOfDay(), localDate1.atStartOfDay()).toDays();
        return days;
    }


    /**
     * Encrypt a string using AES encryption algorithm.
     *
     * @param pwd the password to be encrypted
     * @return the encrypted string
     */
    public String encrypt(String pwd) {
        String encodedPwd = "";
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
            System.out.println("MaxAllowedKeyLength=[" + maxKeyLen + "].");
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(pwd.getBytes());
            encodedPwd = Base64.getEncoder().encodeToString(encVal);

        } catch (Exception e) {

            e.printStackTrace();
        }
        return encodedPwd;

    }

    /**
     * Decrypt a string with AES encryption algorithm.
     *
     * @param encryptedData the data to be decrypted
     * @return the decrypted string
     */
    public String decrypt(String encryptedData) {
        String decodedPWD = "";
        try {
            Key key = generateKey();
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decordedValue = Base64.getDecoder().decode(encryptedData);
            byte[] decValue = c.doFinal(decordedValue);
            decodedPWD = new String(decValue);

        } catch (Exception e) {

        }
        return decodedPWD;
    }

    /**
     * Generate a new encryption key.
     */
    private Key generateKey() {
        SecretKeySpec key = new SecretKeySpec(keyValue, ALGO);
        return key;
    }
}
