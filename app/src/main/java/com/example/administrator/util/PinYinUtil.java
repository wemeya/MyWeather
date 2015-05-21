package com.example.administrator.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * Created by Administrator on 2015/4/8.
 */
public class PinYinUtil {

    public static String ConvertToSpell(String chinese){
        String pinyinName="";
        char[] nameChar=chinese.toCharArray();
        HanyuPinyinOutputFormat defaultFormat=new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i=0;i<nameChar.length;i++){
            if (nameChar[i]>128){
                try {
                    pinyinName+= PinyinHelper.toHanyuPinyinStringArray(nameChar[i],
                            defaultFormat)[0];
                }catch (BadHanyuPinyinOutputFormatCombination e){
                    e.printStackTrace();
                }
            }
            else {
                pinyinName+=nameChar[i];
            }
        }
        return pinyinName;
    }

}
