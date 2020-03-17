// An example of an error that's legitimate, but is issued in
// the wrong place. For now, I'm okay with this error showing up
// in the wrong place, because on correct code it isn't issued -
// see the second part of the example. Fixing this would require
// the CF to support local inference of array component types.
//
// This example assumes that WPI has been run already.

import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;

import org.checkerframework.checker.noliteral.qual.MaybeDerivedFromConstant;

public class SecretKeySpecTestFieldToLocal {

    private static final @MaybeDerivedFromConstant String KEY = "kj34PXF65ze70uFG";

    public SecretKeySpec getKeySpecBad() {
        byte[] theBytes = new byte[0];

        try {
            // :: error: assignment.type.incompatible
            theBytes = KEY.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // it would be better if the error was issued here
        return new SecretKeySpec(theBytes, "AES");
    }

    // CORRECT CODE FOLLOWS

    private String secretKey;

    public SecretKeySpec getKeySpecOk() {
        if (secretKey != null) {
            byte[] theBytes = new byte[0];
            try {
                //
                theBytes = secretKey.getBytes("utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return new SecretKeySpec(theBytes, "AES");
        } else {
            return null;
        }
    }

    public void setSecretKey(String theKey) {
        secretKey = theKey;
    }
}
