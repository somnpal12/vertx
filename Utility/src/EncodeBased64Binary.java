import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class EncodeBased64Binary {

    public static void main(String[] args) throws IOException {
        System.out.println(encodeFileToBase64Binary("d:/rest-api-with-spring.pdf"));
    }

    private static String encodeFileToBase64Binary(String fileName)
            throws IOException {

        File file = new File(fileName);
        System.out.println(file.getName() +" >>>> "+file.length());
        byte[] bytes = loadFile(file);
        byte[] encoded = Base64.getEncoder().encode(bytes);
        String encodedString = new String(encoded);

        return encodedString;
    }

    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int)length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        is.close();
        return bytes;
    }
}
