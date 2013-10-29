package pt.utl.ist.characters;

import pt.utl.ist.marc.Field;
import pt.utl.ist.marc.Record;
import pt.utl.ist.marc.Subfield;
import pt.utl.ist.util.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HorizonConverter implements CharacterConverterI {


    private static HashMap<Byte, Byte> singleChars;
    private static HashMap<Byte, HashMap<Byte, Byte>> doubleChars;
    private static byte sepI;
    private static byte sepF;

    private static Pattern numerosIndexacaoPattern = Pattern.compile("0x95[^0x96]+0x96");
    private static Pattern findUnicodeCharsPattern = Pattern.compile("<U\\+(....)>");

    static {
        sepI = (byte) 0x95;
        sepF = (byte) 0x96;

        doubleChars = new HashMap<Byte, HashMap<Byte, Byte>>();

        HashMap<Byte, Byte> secCharsMap = new HashMap<Byte, Byte>(2);
        doubleChars.put((byte) 0xD0, secCharsMap);
        secCharsMap.put((byte) 0x63, (byte) 0xe7);
        secCharsMap.put((byte) 0x43, (byte) 0xc7);

        secCharsMap = new HashMap<Byte, Byte>(10);
        doubleChars.put((byte) 0xC1, secCharsMap);
        secCharsMap.put((byte) 0x61, (byte) 0xe0);
        secCharsMap.put((byte) 0x41, (byte) 0xc0);
        secCharsMap.put((byte) 0x65, (byte) 0xe8);
        secCharsMap.put((byte) 0x45, (byte) 0xc8);
        secCharsMap.put((byte) 0x69, (byte) 0xec);
        secCharsMap.put((byte) 0x49, (byte) 0xCC);
        secCharsMap.put((byte) 0x6F, (byte) 0xF2);
        secCharsMap.put((byte) 0x4F, (byte) 0xD2);
        secCharsMap.put((byte) 0x75, (byte) 0xF9);
        secCharsMap.put((byte) 0x55, (byte) 0xD9);


        secCharsMap = new HashMap<Byte, Byte>(12);
        doubleChars.put((byte) 0xC2, secCharsMap);
        secCharsMap.put((byte) 0x61, (byte) 0xE1);
        secCharsMap.put((byte) 0x41, (byte) 0xC1);
        secCharsMap.put((byte) 0x65, (byte) 0xE9);
        secCharsMap.put((byte) 0x45, (byte) 0xC9);
        secCharsMap.put((byte) 0x69, (byte) 0xED);
        secCharsMap.put((byte) 0x49, (byte) 0xCD);
        secCharsMap.put((byte) 0x6F, (byte) 0xF3);
        secCharsMap.put((byte) 0x4F, (byte) 0xD3);
        secCharsMap.put((byte) 0x75, (byte) 0xFA);
        secCharsMap.put((byte) 0x55, (byte) 0xDA);
        secCharsMap.put((byte) 0x79, (byte) 0xFD);
        secCharsMap.put((byte) 0x59, (byte) 0xDD);


        secCharsMap = new HashMap<Byte, Byte>(10);
        doubleChars.put((byte) 0xC3, secCharsMap);
        secCharsMap.put((byte) 0x61, (byte) 0xE2);
        secCharsMap.put((byte) 0x41, (byte) 0xC2);
        secCharsMap.put((byte) 0x65, (byte) 0xEA);
        secCharsMap.put((byte) 0x45, (byte) 0xCA);
        secCharsMap.put((byte) 0x69, (byte) 0xEE);
        secCharsMap.put((byte) 0x49, (byte) 0xCE);
        secCharsMap.put((byte) 0x6F, (byte) 0xF4);
        secCharsMap.put((byte) 0x4F, (byte) 0xD4);
        secCharsMap.put((byte) 0x75, (byte) 0xFB);
        secCharsMap.put((byte) 0x55, (byte) 0xDB);

        secCharsMap = new HashMap<Byte, Byte>(6);
        doubleChars.put((byte) 0xC4, secCharsMap);
        secCharsMap.put((byte) 0x61, (byte) 0xE3);
        secCharsMap.put((byte) 0x41, (byte) 0xC3);
        secCharsMap.put((byte) 0x6F, (byte) 0xF5);
        secCharsMap.put((byte) 0x4F, (byte) 0xD5);
        secCharsMap.put((byte) 0x6E, (byte) 0xF1);
        secCharsMap.put((byte) 0x4E, (byte) 0xD1);

        secCharsMap = new HashMap<Byte, Byte>(6);
        doubleChars.put((byte) 0xC9, secCharsMap);
        secCharsMap.put((byte) 0x61, (byte) 0xE3);
        secCharsMap.put((byte) 0x41, (byte) 0xC3);
        secCharsMap.put((byte) 0x6F, (byte) 0xF5);
        secCharsMap.put((byte) 0x4F, (byte) 0xD5);
        secCharsMap.put((byte) 0x6E, (byte) 0xF1);
        secCharsMap.put((byte) 0x4E, (byte) 0xD1);

        secCharsMap = new HashMap<Byte, Byte>(11);
        doubleChars.put((byte) 0xC8, secCharsMap);
        secCharsMap.put((byte) 0x61, (byte) 0xE4);
        secCharsMap.put((byte) 0x41, (byte) 0xC4);
        secCharsMap.put((byte) 0x65, (byte) 0xEB);
        secCharsMap.put((byte) 0x45, (byte) 0xCB);
        secCharsMap.put((byte) 0x69, (byte) 0xEF);
        secCharsMap.put((byte) 0x49, (byte) 0xCF);
        secCharsMap.put((byte) 0x6F, (byte) 0xF6);
        secCharsMap.put((byte) 0x4F, (byte) 0xD6);
        secCharsMap.put((byte) 0x75, (byte) 0xFC);
        secCharsMap.put((byte) 0x55, (byte) 0xDC);
        secCharsMap.put((byte) 0x79, (byte) 0xFF);

        singleChars = new HashMap<Byte, Byte>();
        singleChars.put((byte) 0x88, (byte) 0x3C);
        singleChars.put((byte) 0x89, (byte) 0x3E);
        singleChars.put((byte) 0xB5, (byte) 0xBA);
        singleChars.put((byte) 0xA7, (byte) 0xBA);
        singleChars.put((byte) 0xA6, (byte) 0xAA);
        singleChars.put((byte) 0xB4, (byte) 0xAA);
        singleChars.put((byte) 0xAA, (byte) 0xAB);
        singleChars.put((byte) 0xBA, (byte) 0xBB);
        singleChars.put((byte) 0x00, (byte) 0x20);
        singleChars.put((byte) 0x01, (byte) 0x20);
        singleChars.put((byte) 0x02, (byte) 0x20);
        singleChars.put((byte) 0x03, (byte) 0x20);
        singleChars.put((byte) 0x04, (byte) 0x20);
        singleChars.put((byte) 0x05, (byte) 0x20);
        singleChars.put((byte) 0x06, (byte) 0x20);
        singleChars.put((byte) 0x07, (byte) 0x20);
        singleChars.put((byte) 0x08, (byte) 0x20);
        singleChars.put((byte) 0x09, (byte) 0x20);
        singleChars.put((byte) 0x0B, (byte) 0x20);
        singleChars.put((byte) 0x0C, (byte) 0x20);
        singleChars.put((byte) 0x0D, (byte) 0x20);
        singleChars.put((byte) 0x0E, (byte) 0x20);
        singleChars.put((byte) 0x0F, (byte) 0x20);
        singleChars.put((byte) 0x10, (byte) 0x20);
        singleChars.put((byte) 0x11, (byte) 0x20);
        singleChars.put((byte) 0x12, (byte) 0x20);
        singleChars.put((byte) 0x13, (byte) 0x20);
        singleChars.put((byte) 0x14, (byte) 0x20);
        singleChars.put((byte) 0x15, (byte) 0x20);
        singleChars.put((byte) 0x16, (byte) 0x20);
        singleChars.put((byte) 0x17, (byte) 0x20);
        singleChars.put((byte) 0x18, (byte) 0x20);
        singleChars.put((byte) 0x19, (byte) 0x20);
        singleChars.put((byte) 0x1A, (byte) 0x20);
        singleChars.put((byte) 0x1B, (byte) 0x20);
        singleChars.put((byte) 0x1C, (byte) 0x20);
        singleChars.put((byte) 0x1D, (byte) 0x20);
        singleChars.put((byte) 0x1E, (byte) 0x20);
        singleChars.put((byte) 0x1F, (byte) 0x20);
        singleChars.put((byte) 0xA4, (byte) 0x24);
    }


    public String convert(String txt) {
        return convertString(txt);
    }


    public static byte[] convertBytes(byte[] array) {
        byte[] ret = new byte[array.length];
        int r = 0;
        for (int k = 0; k < array.length; k++) {
            if (array[k] == sepI) {
                boolean ends = false;
                int sc = k + 1;
                for (; sc < array.length; sc++) {
                    if (array[sc] == sepF) {
                        ends = true;
                        break;
                    }
                }
                if (ends) {
                    k = sc;
                }
                else {
                    ret[r] = array[k];
                    r++;
                }
            }
            else {
                Byte newChar = null;
                if (k < array.length - 1)
                    newChar = findCharAtDouble(array[k], array[k + 1]);
                if (newChar == null)
                    newChar = singleChars.get(array[k]);
                else {
                    k++;

                }
                if (newChar != null) {
                    ret[r] = newChar;
                }
                else {
                    ret[r] = array[k];
                }
                r++;
            }
        }

        //remove possible extra characters 0x00 in the array
        int realsz = ret.length;
        byte empty = (byte) 0x00;
        for (int idx = ret.length - 1; idx >= 0; idx--) {
            if (ret[idx] == empty) {
                realsz = idx;
            }
            else
                break;
        }
        if (realsz != ret.length) {
            byte[] newret = new byte[realsz];
            for (int idx = realsz - 1; idx >= 0; idx--) {
                newret[idx] = ret[idx];
            }
            ret = newret;
        }
        return ret;
    }

    public static String convertString(String str) {

        try {
            Matcher m = numerosIndexacaoPattern.matcher(str);
            str = m.replaceAll("");
            str = new String(convertBytes(str.getBytes()));

            m = numerosIndexacaoPattern.matcher(str);
            str = m.replaceAll("");

            for (Matcher hasUnicodeChars = findUnicodeCharsPattern.matcher(str); hasUnicodeChars.find(); hasUnicodeChars = findUnicodeCharsPattern.matcher(str)) {
                String uc = "0x" + hasUnicodeChars.group(1);
                str = hasUnicodeChars.replaceFirst(UnicodeUtil.append32(Integer.decode(uc)));
            }

            return str;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void convertRecord(Record rec) {
        if (rec == null)
            return;

        if (rec.getLeader() != null)
            rec.setLeader(convertString(rec.getLeader()));

        for (Field field : rec.getFields()) {
            if (field.isControlField()) {
                String newData = convertString(field.getValue());
                field.setValue(newData);
            }
            else {
                for (Subfield subfield : field.getSubfields()) {
                    String newData = convertString(subfield.getValue());
                    subfield.setValue(newData);
                }
            }
        }
    }

    /**
     * ***********************************************************************
     * ***********                  Private Methods           ******************
     * ***********************************************************************
     */

    private static Byte findCharAtDouble(byte c1, byte c2) {
        HashMap<Byte, Byte> secChars = doubleChars.get(c1);
        if (secChars == null)
            return null;
        return secChars.get(c2);
    }

    private static String toSafeRegExp(String str) {
        Pattern p = Pattern.compile("\\+");
        Matcher m = p.matcher(str);
        str = m.replaceAll("\\\\+");

        p = Pattern.compile("\\*");
        m = p.matcher(str);
        str = m.replaceAll("\\\\*");

        p = Pattern.compile("\\)");
        m = p.matcher(str);
        str = m.replaceAll("\\\\)");

        p = Pattern.compile("\\(");
        m = p.matcher(str);
        str = m.replaceAll("\\\\(");
        return str;
    }
}

