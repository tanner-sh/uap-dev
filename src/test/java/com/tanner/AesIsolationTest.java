package com.tanner;

import com.tanner.devconfig.util.AESEncode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AesIsolationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void keysAreResolvedPerHomeEvenWhenPathContainsBin() throws Exception {
        File first = temporaryFolder.newFolder("bin-project-one");
        File second = temporaryFolder.newFolder("project-two");
        String firstKey = "00112233445566778899AABBCCDDEEFF"
                + "00112233445566778899AABBCCDDEEFF";
        String secondKey = "FFEEDDCCBBAA99887766554433221100"
                + "FFEEDDCCBBAA99887766554433221100";
        AESEncode.insert(firstKey, first.getPath());
        AESEncode.insert(secondKey, second.getPath());

        String firstCipher = AESEncode.encrypt("密码-one", first.getPath());
        String secondCipher = AESEncode.encrypt("密码-two", second.getPath());

        assertNotEquals(firstCipher, secondCipher);
        assertEquals(firstKey, AESEncode.query(first.getPath()));
        assertEquals(secondKey, AESEncode.query(second.getPath()));
        assertEquals("密码-one", AESEncode.decrypt(firstCipher, first.getPath()));
        assertEquals("密码-two", AESEncode.decrypt(secondCipher, second.getPath()));
    }
}
