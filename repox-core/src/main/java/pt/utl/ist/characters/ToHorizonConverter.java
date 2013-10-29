package pt.utl.ist.characters;

import pt.utl.ist.marc.Field;
import pt.utl.ist.marc.Record;
import pt.utl.ist.marc.Subfield;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToHorizonConverter {

    private static byte[][] doubleCharsOld;
    private static byte[][] doubleCharsNew;
    private static byte[] charsOld;
    private static byte[] charsNew;
    private static byte[] charsToStringsOld;
    private static String[] charsToStringsNew;
    private static byte sepI;
    private static byte sepF;

    private static Pattern numerosIndexacaoPattern=Pattern.compile("0x95[^0x96]+0x96");
    private static Pattern findUnicodeCharsPattern=Pattern.compile("<U\\+(....)>");

    static {
        sepI=getByte("0x95");
        sepF=getByte("0x96");

        doubleCharsOld=new byte[7][];
        doubleCharsNew=new byte[7][];

        doubleCharsOld[0]=new byte[3];
        doubleCharsNew[0]=new byte[3];
        doubleCharsOld[0][0]=getByte("0xD0");
        doubleCharsOld[0][1]=getByte("0x63");
        doubleCharsOld[0][2]=getByte("0x43");
        doubleCharsNew[0][0]=getByte("0x00");
        doubleCharsNew[0][1]=getByte("0xe7");//ç
        doubleCharsNew[0][2]=getByte("0xc7");//Ç

        doubleCharsOld[1]=new byte[11];
        doubleCharsNew[1]=new byte[11];
        doubleCharsOld[1][0]=getByte("0xC1");
        doubleCharsOld[1][1]=getByte("0x61");
        doubleCharsOld[1][2]=getByte("0x41");
        doubleCharsOld[1][3]=getByte("0x65");
        doubleCharsOld[1][4]=getByte("0x45");
        doubleCharsOld[1][5]=getByte("0x69");
        doubleCharsOld[1][6]=getByte("0x49");
        doubleCharsOld[1][7]=getByte("0x6F");
        doubleCharsOld[1][8]=getByte("0x4F");
        doubleCharsOld[1][9]=getByte("0x75");
        doubleCharsOld[1][10]=getByte("0x55");
        doubleCharsNew[1][0]=getByte("0x00");
        doubleCharsNew[1][1]=getByte("0xe0");//
        doubleCharsNew[1][2]=getByte("0xc0");//
        doubleCharsNew[1][3]=getByte("0xe8");//
        doubleCharsNew[1][4]=getByte("0xc8");//
        doubleCharsNew[1][5]=getByte("0xec");//
        doubleCharsNew[1][6]=getByte("0xCC");//
        doubleCharsNew[1][7]=getByte("0xF2");//
        doubleCharsNew[1][8]=getByte("0xD2");//
        doubleCharsNew[1][9]=getByte("0xF9");//
        doubleCharsNew[1][10]=getByte("0xD9");//

        doubleCharsOld[2]=new byte[13];
        doubleCharsNew[2]=new byte[13];
        doubleCharsOld[2][0]=getByte("0xC2");
        doubleCharsOld[2][1]=getByte("0x61");
        doubleCharsOld[2][2]=getByte("0x41");
        doubleCharsOld[2][3]=getByte("0x65");
        doubleCharsOld[2][4]=getByte("0x45");
        doubleCharsOld[2][5]=getByte("0x69");
        doubleCharsOld[2][6]=getByte("0x49");
        doubleCharsOld[2][7]=getByte("0x6F");
        doubleCharsOld[2][8]=getByte("0x4F");
        doubleCharsOld[2][9]=getByte("0x75");
        doubleCharsOld[2][10]=getByte("0x55");
        doubleCharsOld[2][11]=getByte("0x79");
        doubleCharsOld[2][12]=getByte("0x59");
        doubleCharsNew[2][0]=getByte("0x00");
        doubleCharsNew[2][1]=getByte("0xE1");//
        doubleCharsNew[2][2]=getByte("0xC1");//
        doubleCharsNew[2][3]=getByte("0xE9");//
        doubleCharsNew[2][4]=getByte("0xC9");//
        doubleCharsNew[2][5]=getByte("0xED");//
        doubleCharsNew[2][6]=getByte("0xCD");//
        doubleCharsNew[2][7]=getByte("0xF3");//
        doubleCharsNew[2][8]=getByte("0xD3");//
        doubleCharsNew[2][9]=getByte("0xFA");//
        doubleCharsNew[2][10]=getByte("0xDA");//
        doubleCharsNew[2][11]=getByte("0xFD");//
        doubleCharsNew[2][12]=getByte("0xDD");//

        doubleCharsOld[3]=new byte[11];
        doubleCharsNew[3]=new byte[11];
        doubleCharsOld[3][0]=getByte("0xC3");
        doubleCharsOld[3][1]=getByte("0x61");
        doubleCharsOld[3][2]=getByte("0x41");
        doubleCharsOld[3][3]=getByte("0x65");
        doubleCharsOld[3][4]=getByte("0x45");
        doubleCharsOld[3][5]=getByte("0x69");
        doubleCharsOld[3][6]=getByte("0x49");
        doubleCharsOld[3][7]=getByte("0x6F");
        doubleCharsOld[3][8]=getByte("0x4F");
        doubleCharsOld[3][9]=getByte("0x75");
        doubleCharsOld[3][10]=getByte("0x55");
        doubleCharsNew[3][0]=getByte("0x00");
        doubleCharsNew[3][1]=getByte("0xE2");//
        doubleCharsNew[3][2]=getByte("0xC2");//
        doubleCharsNew[3][3]=getByte("0xEA");//
        doubleCharsNew[3][4]=getByte("0xCA");//
        doubleCharsNew[3][5]=getByte("0xEE");//
        doubleCharsNew[3][6]=getByte("0xCE");//
        doubleCharsNew[3][7]=getByte("0xF4");//
        doubleCharsNew[3][8]=getByte("0xD4");//
        doubleCharsNew[3][9]=getByte("0xFB");//
        doubleCharsNew[3][10]=getByte("0xDB");//

        doubleCharsOld[4]=new byte[7];
        doubleCharsNew[4]=new byte[7];
        doubleCharsOld[4][0]=getByte("0xC4");
        doubleCharsOld[4][1]=getByte("0x61");
        doubleCharsOld[4][2]=getByte("0x41");
        doubleCharsOld[4][3]=getByte("0x6F");
        doubleCharsOld[4][4]=getByte("0x4F");
        doubleCharsOld[4][5]=getByte("0x6E");
        doubleCharsOld[4][6]=getByte("0x4E");
        doubleCharsNew[4][0]=getByte("0x00");
        doubleCharsNew[4][1]=getByte("0xE3");//
        doubleCharsNew[4][2]=getByte("0xC3");//
        doubleCharsNew[4][3]=getByte("0xF5");//
        doubleCharsNew[4][4]=getByte("0xD5");//
        doubleCharsNew[4][5]=getByte("0xF1");//
        doubleCharsNew[4][6]=getByte("0xD1");//

        doubleCharsOld[5]=new byte[7];
        doubleCharsNew[5]=new byte[7];
        doubleCharsOld[5][0]=getByte("0xC9");
        doubleCharsOld[5][1]=getByte("0x61");
        doubleCharsOld[5][2]=getByte("0x41");
        doubleCharsOld[5][3]=getByte("0x6F");
        doubleCharsOld[5][4]=getByte("0x4F");
        doubleCharsOld[5][5]=getByte("0x6E");
        doubleCharsOld[5][6]=getByte("0x4E");
        doubleCharsNew[5][0]=getByte("0x00");
        doubleCharsNew[4][1]=getByte("0xE3");//
        doubleCharsNew[4][2]=getByte("0xC3");//
        doubleCharsNew[4][3]=getByte("0xF5");//
        doubleCharsNew[4][4]=getByte("0xD5");//
        doubleCharsNew[4][5]=getByte("0xF1");//
        doubleCharsNew[4][6]=getByte("0xD1");//

        doubleCharsOld[6]=new byte[12];
        doubleCharsNew[6]=new byte[12];
        doubleCharsOld[6][0]=getByte("0xC8");
        doubleCharsOld[6][1]=getByte("0x61");
        doubleCharsOld[6][2]=getByte("0x41");
        doubleCharsOld[6][3]=getByte("0x65");
        doubleCharsOld[6][4]=getByte("0x45");
        doubleCharsOld[6][5]=getByte("0x69");
        doubleCharsOld[6][6]=getByte("0x49");
        doubleCharsOld[6][7]=getByte("0x6F");
        doubleCharsOld[6][8]=getByte("0x4F");
        doubleCharsOld[6][9]=getByte("0x75");
        doubleCharsOld[6][10]=getByte("0x55");
        doubleCharsOld[6][11]=getByte("0x79");
        doubleCharsNew[6][0]=getByte("0x00");
        doubleCharsNew[6][1]=getByte("0xE4");//
        doubleCharsNew[6][2]=getByte("0xC4");//
        doubleCharsNew[6][3]=getByte("0xEB");//
        doubleCharsNew[6][4]=getByte("0xCB");//
        doubleCharsNew[6][5]=getByte("0xEF");//
        doubleCharsNew[6][6]=getByte("0xCF");//
        doubleCharsNew[6][7]=getByte("0xF6");//
        doubleCharsNew[6][8]=getByte("0xD6");//
        doubleCharsNew[6][9]=getByte("0xFC");//
        doubleCharsNew[6][10]=getByte("0xDC");//
        doubleCharsNew[6][11]=getByte("0xFF");//

        charsOld=new byte[4];
        charsNew=new byte[4];
        charsOld[0]=getByte("0x88");//        
        charsNew[0]=getByte("0x3C");//
        charsOld[1]=getByte("0x89");//        
        charsNew[1]=getByte("0x3E");//
        charsOld[2]=getByte("0x20");//        
        charsNew[2]=getByte("0x00");//
        charsOld[3]=getByte("0xB4");//        
        charsNew[3]=getByte("0x27");//

        charsToStringsOld=new byte[4];
        charsToStringsNew=new String[4];
        charsToStringsOld[0]=getByte("0xAA");
        charsToStringsNew[0]="<U+00AA>";
        charsToStringsOld[1]=getByte("0xBA");
        charsToStringsNew[1]="<U+00BA>";
        charsToStringsOld[2]=getByte("0x7E");
        charsToStringsNew[2]="<U+007E>";
        charsToStringsOld[3]=getByte("0xF1");
        charsToStringsNew[3]="<U+00E6>";
    }


    public static byte[] convertBytes(byte[] array) {
        byte[] ret=null;
        int retries=0;
        boolean retry=true;
        while(retry){
            int r=0;
            retry=false;
            ret=new byte[array.length * (2 + retries)];
            try{
                for (byte anArray : array) {
                    Byte[] newChar = findChar(anArray);
                    if (newChar != null) {
                        for (Byte aNewChar : newChar) {
                            ret[r] = aNewChar.byteValue();
                            r++;
                        }
                    } else {
                        byte[] newStr = findCharToString(anArray);
                        if (newStr != null) {
                            for (byte aNewStr : newStr) {
                                ret[r] = aNewStr;
                                r++;
                            }
                        } else {
                            ret[r] = anArray;
                            r++;
                        }
                    }
                }
            }catch (java.lang.ArrayIndexOutOfBoundsException e){
                retry=true;
                retries++;
            }
        }

        //remove possible extra characters 0x00 in the array
        int realsz=ret.length;
        byte empty=getByte("0x00");
        for (int idx=ret.length-1 ; idx>=0 ; idx--) {
            if (ret[idx]==empty){
                realsz=idx;
            }else
                break;
        }
        if (realsz != ret.length){
            byte[] newret=new byte[realsz];
            for (int idx=realsz-1 ; idx>=0 ; idx--) {
                newret[idx]=ret[idx];
            }
            ret=newret;
        }
        return ret;
    }

    public static String convertString(String str) {

//        Matcher m = numerosIndexacaoPattern.matcher(str);
//        str=m.replaceAll("");
        str=new String(convertBytes(str.getBytes()));

//        m = numerosIndexacaoPattern.matcher(str);
//        str=m.replaceAll("");
//
//        for ( Matcher hasUnicodeChars=findUnicodeCharsPattern.matcher(str) ; hasUnicodeChars.find() ; hasUnicodeChars=findUnicodeCharsPattern.matcher(str) ){
//            String uc="0x"+hasUnicodeChars.group(1);
//            str=hasUnicodeChars.replaceFirst(UnicodeUtil.append32(Integer.decode(uc).intValue()));
//        }

        return str;
    }


    public static void convertRecord(Record rec){
        if (rec==null)
            return;
        List fields=rec.getFields();

        for (Object field : fields) {
            Field f = (Field) field;
            if (f.isControlField()) {
                String newData = convertString(f.getValue());
                f.setValue(newData);
            } else {
                for (Object o : f.getSubfields()) {
                    Subfield sf = (Subfield) o;
                    String newData = convertString(sf.getValue());
                    sf.setValue(newData);
                }
//                if (f.getTag()==200) {
//                    for (Iterator isf = f.getSubfields().iterator(); isf.hasNext();) {
//                        Subfield sf=(Subfield)isf.next();
//                        if (sf.getCode()=='a' || sf.getCode()=='c' || sf.getCode()=='d' || sf.getCode()=='e') {
//                            String newData=convertNaoAlfabetizacao(sf.getValue());
//                            sf.setValue(newData);
//                        }
//                    }
//                }else if(f.getTag()==225 || (f.getTag()>=500 && f.getTag()<600)) {
//                    for (Iterator isf = f.getSubfields().iterator(); isf.hasNext();) {
//                        Subfield sf=(Subfield)isf.next();
//                        if (sf.getCode()=='a') {
//                            String newData=convertNaoAlfabetizacao(sf.getValue());
//                            sf.setValue(newData);
//                        }
//                    }
//                }else if(f.getTag()==327) {
//                    for (Iterator isf = f.getSubfields().iterator(); isf.hasNext();) {
//                        Subfield sf=(Subfield)isf.next();
//                        if (sf.getCode()=='a') {
//                            String newData=convertNaoAlfabetizacao(sf.getValue());
//                            sf.setValue(newData);
//                            break;
//                        }
//                    }                	
//                }
            }
        }
    }

    /**************************************************************************
     ************                  Private Methods           ******************
     *************************************************************************/

//    private static String convertNaoAlfabetizacao(String val) {
//    	return val;
//    }

    private static byte getByte(String b){
        return Short.decode(b).byteValue();
    }

    private static Byte findCharAtDouble(byte c1, byte c2){
        for (int c=0; c < doubleCharsOld.length; c++){
            if (doubleCharsOld[c][0]==c1){
                for (int i=0; i < doubleCharsOld[c].length; i++){
                    if (doubleCharsOld[c][i]==c2)
                        return new Byte(doubleCharsNew[c][i]);
                }
            }
        }
        return null;
    }

    private static String toSafeRegExp(String str){
        Pattern p = Pattern.compile("\\+");
        Matcher m = p.matcher(str);
        str=m.replaceAll("\\\\+");

        p = Pattern.compile("\\*");
        m = p.matcher(str);
        str=m.replaceAll("\\\\*");

        p = Pattern.compile("\\)");
        m = p.matcher(str);
        str=m.replaceAll("\\\\)");

        p = Pattern.compile("\\(");
        m = p.matcher(str);
        str=m.replaceAll("\\\\(");
        return str;
    }

    private static Byte[] findChar(byte ch){

        for (int i=0; i < charsNew.length; i++){
            if (charsNew[i]==ch){
                return new Byte[]{new Byte(charsOld[i])};
            }
        }
        for (int c=0; c < doubleCharsNew.length; c++){
            for (int i=0; i < doubleCharsNew[c].length; i++){
                if (doubleCharsNew[c][i]==ch)
                    return new Byte[]{new Byte(doubleCharsOld[c][0]),new Byte(doubleCharsOld[c][i])};
            }
        }
        return null;
    }


    private static byte[] findCharToString(byte ch){
        try{
            for (int i=0; i < charsToStringsOld.length; i++){
                if (charsToStringsOld[i]==ch){
                    return charsToStringsNew[i].getBytes("ISO8859_1");
                }
            }
            return null;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}