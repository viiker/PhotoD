package com.viisoo.PhotoD;

/**
 * Created by dam on 2014/11/24.
 */
import java.util.UUID;

/**
 * User: dam
 * Date: 13-11-22
 */
public class UUIDGen {

    static final String[] strs = new String[]{"a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z"};
    public static String genShortPK(){
        String uuid = UUID.randomUUID().toString().replace("-","");
        StringBuffer sb = new StringBuffer("");
        for(int i = 0;i < 8; i ++){
            String str = uuid.substring(i *4 ,i*4+4);
            int x = Integer.parseInt(str,16);
            sb.append(strs[x % 0x3E]);
        }
        return sb.toString();
    }
}