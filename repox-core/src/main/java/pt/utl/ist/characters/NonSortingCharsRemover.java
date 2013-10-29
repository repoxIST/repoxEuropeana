/*
 * AccentRemover.java
 *
 * Created on 4 de Junho de 2003, 23:28
 */

package pt.utl.ist.characters;

import pt.utl.ist.marc.Record;
import pt.utl.ist.marc.iso2709.IteratorIso2709;

import java.io.File;
import java.util.regex.Pattern;

/**
 *
 * @author  Nuno Freire
 */
public class NonSortingCharsRemover implements CharacterConverterI{

    private static Pattern nonSortingCharsPattern=Pattern.compile("[\u0098\u0088]([^\u009C\u0089]+)[\\u009C\\u0089]\\s*");

    public String convert(String s){
    	return nonSortingCharsPattern.matcher(s).replaceAll("$1");
    }
}
