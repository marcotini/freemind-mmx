/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2006  Christian Foltin <christianfoltin@users.sourceforge.net>
 *See COPYING for Details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
/*$Id: HtmlTools.java,v 1.1.2.3 2006-05-25 21:38:35 christianfoltin Exp $*/

package freemind.main;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;

/** */
public class HtmlTools {

    private static final String FIND_TAGS_PATTERN = "<[^<>]+>";

    private static HtmlTools sInstance = new HtmlTools();

    /**
     * 
     */
    private HtmlTools() {
        super();

    }

    public static HtmlTools getInstance() {
        return sInstance;
    }

    public String toXhtml(String htmlText) {
        StringReader reader = new StringReader(htmlText);
        StringWriter writer = new StringWriter();
        try {
            XHTMLWriter.html2xhtml(reader, writer);
            return writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        // fallback:
        htmlText = htmlText.replaceAll("<", "&gt;");
        htmlText = htmlText.replaceAll(">", "&lt;");
        return htmlText;
    }

    public String toHtml(String xhtmlText) {
        // Remove '/' from <.../> of elements that do not have '/' there in HTML
        return xhtmlText.replaceAll("<(("+
                    "br|area|base|basefont|"+
                    "bgsound|button|col|colgroup|embed|hr"+
                    "|img|input|isindex|keygen|link|meta"+
                    "|object|plaintext|spacer|wbr"+
                    ")(\\s[^>]*)?)/>",
                    "<$1>");
    }

    public static class IndexPair {
        public int originalStart;
        public int originalEnd;
        public int replacedStart;
        public int replacedEnd;
        /**
         * @param pOriginalStart
         * @param pOriginalEnd
         * @param pReplacedStart
         * @param pReplacedEnd
         */
        public IndexPair(int pOriginalStart, int pOriginalEnd, int pReplacedStart, int pReplacedEnd)
        {
            super();
            
            originalStart = pOriginalStart;
            originalEnd = pOriginalEnd;
            replacedStart = pReplacedStart;
            replacedEnd = pReplacedEnd;
        }
        /**
         * @generated by CodeSugar http://sourceforge.net/projects/codesugar */
        
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("[IndexPair:");
            buffer.append(" originalStart: ");
            buffer.append(originalStart);
            buffer.append(" originalEnd: ");
            buffer.append(originalEnd);
            buffer.append(" replacedStart: ");
            buffer.append(replacedStart);
            buffer.append(" replacedEnd: ");
            buffer.append(replacedEnd);
            buffer.append("]");
            return buffer.toString();
        }
    }
    
    /** Replaces text in node content without replacing tags.
     * @param pattern
     * @param replacement
     * @param text
     */
    public String getReplaceResult(Pattern pattern, String replacement, String text) {
        ArrayList l = new ArrayList();
        StringBuffer sb = new StringBuffer();
        String input = text;
        // remove tags and denote their positions:
        {
            Pattern p = Pattern.compile(FIND_TAGS_PATTERN);
            Matcher m = p.matcher(input);
            int lastMatchEnd = 0;
            while (m.find())
            {
                int replStart = sb.length();
                m.appendReplacement(sb, "");
                IndexPair indexPair = new IndexPair(lastMatchEnd, m.end(),
                        replStart, sb.length());
                lastMatchEnd = m.end();
                System.out.println(sb.toString() + ", " + indexPair);
                l.add(indexPair);
            }
            int replStart = sb.length();
            m.appendTail(sb);
            IndexPair indexPair = new IndexPair(lastMatchEnd, input.length(),
                    replStart, sb.length());
            System.out.println(sb.toString() + ", " + indexPair);
            l.add(indexPair);
            System.out.println(sb.toString());
        }
        
        String replaceBy = replacement;
        
        Pattern p = pattern;
        String replacedString = sb.toString();
        Matcher m = p.matcher(replacedString);
        StringBuffer sb2 = new StringBuffer();
        StringBuffer sbResult = new StringBuffer();
        if(m.find()) {
            m.appendReplacement(sb2, replaceBy);
            /* now, take all from 0 to m.start() from original.
             * append the replaced text, append all removed tags from the original
             * that stays in between and append the rest.
             */
            int mStart = m.start();
            int mEnd = m.end();
            
            //take all from 0 to m.start() from original.
            append(sbResult, input, l, 0, mStart);
            //append the replaced text
            sbResult.append(sb2.toString());
            //append all removed tags from the original that stays in between
            StringBuffer sbTemp = new StringBuffer();
            append(sbTemp, input, l, mStart+1, mEnd);
            sbResult.append(sbTemp.toString().replaceAll(".*?("+FIND_TAGS_PATTERN+")", "$1"));
            //append the rest.
            append(sbResult, input, l, mEnd, replacedString.length());
            // if there are tags at the end:
            append(sbResult, input, l, replacedString.length(), replacedString.length());
        }
//        m.appendTail(sb2);
        String result = sbResult.toString();
        System.out.println("Result:'"+result+"'");
        return result;
    }
    
    
    private void append(StringBuffer pResult, String pInput, ArrayList pListOfIndices, int pReducedStart, int pReducedEnd)
    {
        if(pReducedStart == pReducedEnd) {
            int minj = getMinimalOriginalPosition(pReducedStart, pListOfIndices);
            int maxj = getMaximalOriginalPosition(pReducedStart, pListOfIndices);
            pResult.append(pInput.substring(minj, maxj));
            return;
        }
        for(int i = pReducedStart; i < pReducedEnd; ++i) {
            int minj = getMinimalOriginalPosition(i, pListOfIndices);
            int maxj = getMaximalOriginalPosition(i, pListOfIndices);
            pResult.append(pInput.substring(minj, maxj));
        }
//        pResult.append(getChunk(pInput, pReducedEnd, pListOfIndices));
    }


//    private String getChunk(String pInput, int pPosition, ArrayList pListOfIndices)
//    {
//        StringBuffer sbTemp = new StringBuffer();
//        for (Iterator iter = pListOfIndices.iterator(); iter.hasNext();)
//        {
//            IndexPair pair = (IndexPair) iter.next();
//            if(pPosition == pair.replacedStart && pPosition == pair.replacedEnd) {
//                sbTemp.append(pInput.subSequence(pair.originalStart, pair.originalEnd));
//            }
//        }
//        return sbTemp.toString();
//    }
//

    public int getMinimalOriginalPosition(int pI, ArrayList pListOfIndices)
    {
        for (Iterator iter = pListOfIndices.iterator(); iter.hasNext();)
        {
            IndexPair pair = (IndexPair) iter.next();
            if(pI >= pair.replacedStart) {
                return pair.originalStart + pI - pair.replacedStart;
            }
        }
        throw new IllegalArgumentException("Position "+pI+" not found.");
    }
    public int getMaximalOriginalPosition(int pI, ArrayList pListOfIndices)
    {
        int lastResult = -1; 
        for (Iterator iter = pListOfIndices.iterator(); iter.hasNext();)
        {
            IndexPair pair = (IndexPair) iter.next();
            if(pI > pair.replacedEnd && lastResult >= 0) {
                return lastResult;
            }
            if(pI >= pair.replacedStart) {
                lastResult = pair.originalStart + pI - pair.replacedStart;
            }
        }
        if(lastResult >= 0) {
            return lastResult;
        }
        throw new IllegalArgumentException("Position "+pI+" not found.");
    }


}
