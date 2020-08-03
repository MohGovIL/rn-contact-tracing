package moh.gov.il.crypto.utilities;

/**
 * Created by hagai on 11/05/2020.
 */
public class BytesUtils {

    public static byte[] numToBytes(int num, int numBytes) {

        byte[] result = new byte[numBytes];
        for(int i = 0; i < numBytes; i ++) {
            result[i] = (byte)(num & 0xff);
            num >>= 8;
        }

        assert num == 0 : "Sanity Check";

        return result;
    }

    // TODO: check if this method is correct
    public static byte[] hexToBytes(String s) {

        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] pad(byte[] array, int size) {

        assert array.length <= size : "Padded array should be smaller";
        byte[] pad = new byte[size - array.length];
        for(int i = 0; i < pad.length; i ++) {
            pad[i] = 0;
        }
        byte[] result = byteConcatenation(array, pad);

        return result;
    }

    // TODO: check if this method is correct
    public static byte[] xor(byte[] first, byte[] second) {

        assert first.length == second.length;

        byte[] result = new byte[first.length];
        int i = 0;
        for(byte b : first) {
            result[i] = (byte) (b ^ second[i++]);
        }
        return result;
    }

    public static final byte[] byteConcatenation(byte[] firstArray, byte[] secondArray) {

        int firstLen = firstArray.length;
        int secondLen = secondArray.length;

        byte[] result = new byte[firstLen + secondLen];
        System.arraycopy(firstArray, 0, result, 0, firstLen);
        System.arraycopy(secondArray, 0, result, firstLen, secondLen);

        return result;
    }
}
