package com.tanner.devconfig.util;

/**
 * copy from home
 */
public class DES {

    private static final boolean[] ks = new boolean[]{false, false, true, true, true, true, true,
            true, false, true, true, true, true, true, true, false};
    private static final int[][] kp = new int[][]{
            {0, 262144, 16777216, 17039360, 1024, 263168, 16778240, 17040384, 2097152, 2359296, 18874368,
                    19136512, 2098176, 2360320, 18875392, 19137536, 1, 262145, 16777217, 17039361, 1025,
                    263169, 16778241, 17040385, 2097153, 2359297, 18874369, 19136513, 2098177, 2360321,
                    18875393, 19137537, 33554432, 33816576, 50331648, 50593792, 33555456, 33817600, 50332672,
                    50594816, 35651584, 35913728, 52428800, 52690944, 35652608, 35914752, 52429824, 52691968,
                    33554433, 33816577, 50331649, 50593793, 33555457, 33817601, 50332673, 50594817, 35651585,
                    35913729, 52428801, 52690945, 35652609, 35914753, 52429825, 52691969},
            {0, 2, 2048, 2050, 134217728, 134217730, 134219776, 134219778, 65536, 65538, 67584, 67586,
                    134283264, 134283266, 134285312, 134285314, 256, 258, 2304, 2306, 134217984, 134217986,
                    134220032, 134220034, 65792, 65794, 67840, 67842, 134283520, 134283522, 134285568,
                    134285570, 16, 18, 2064, 2066, 134217744, 134217746, 134219792, 134219794, 65552, 65554,
                    67600, 67602, 134283280, 134283282, 134285328, 134285330, 272, 274, 2320, 2322, 134218000,
                    134218002, 134220048, 134220050, 65808, 65810, 67856, 67858, 134283536, 134283538,
                    134285584, 134285586},
            {0, 4, 4096, 4100, 268435456, 268435460, 268439552, 268439556, 32, 36, 4128, 4132, 268435488,
                    268435492, 268439584, 268439588, 524288, 524292, 528384, 528388, 268959744, 268959748,
                    268963840, 268963844, 524320, 524324, 528416, 528420, 268959776, 268959780, 268963872,
                    268963876, 536870912, 536870916, 536875008, 536875012, 805306368, 805306372, 805310464,
                    805310468, 536870944, 536870948, 536875040, 536875044, 805306400, 805306404, 805310496,
                    805310500, 537395200, 537395204, 537399296, 537399300, 805830656, 805830660, 805834752,
                    805834756, 537395232, 537395236, 537399328, 537399332, 805830688, 805830692, 805834784,
                    805834788},
            {0, 1048576, 8, 1048584, 512, 1049088, 520, 1049096, 67108864, 68157440, 67108872, 68157448,
                    67109376, 68157952, 67109384, 68157960, 8192, 1056768, 8200, 1056776, 8704, 1057280, 8712,
                    1057288, 67117056, 68165632, 67117064, 68165640, 67117568, 68166144, 67117576, 68166152,
                    131072, 1179648, 131080, 1179656, 131584, 1180160, 131592, 1180168, 67239936, 68288512,
                    67239944, 68288520, 67240448, 68289024, 67240456, 68289032, 139264, 1187840, 139272,
                    1187848, 139776, 1188352, 139784, 1188360, 67248128, 68296704, 67248136, 68296712,
                    67248640, 68297216, 67248648, 68297224},
            {0, 512, 131072, 131584, 1, 513, 131073, 131585, 134217728, 134218240, 134348800, 134349312,
                    134217729, 134218241, 134348801, 134349313, 2097152, 2097664, 2228224, 2228736, 2097153,
                    2097665, 2228225, 2228737, 136314880, 136315392, 136445952, 136446464, 136314881,
                    136315393, 136445953, 136446465, 2, 514, 131074, 131586, 3, 515, 131075, 131587,
                    134217730, 134218242, 134348802, 134349314, 134217731, 134218243, 134348803, 134349315,
                    2097154, 2097666, 2228226, 2228738, 2097155, 2097667, 2228227, 2228739, 136314882,
                    136315394, 136445954, 136446466, 136314883, 136315395, 136445955, 136446467},
            {0, 16, 536870912, 536870928, 1048576, 1048592, 537919488, 537919504, 2048, 2064, 536872960,
                    536872976, 1050624, 1050640, 537921536, 537921552, 67108864, 67108880, 603979776,
                    603979792, 68157440, 68157456, 605028352, 605028368, 67110912, 67110928, 603981824,
                    603981840, 68159488, 68159504, 605030400, 605030416, 4, 20, 536870916, 536870932, 1048580,
                    1048596, 537919492, 537919508, 2052, 2068, 536872964, 536872980, 1050628, 1050644,
                    537921540, 537921556, 67108868, 67108884, 603979780, 603979796, 68157444, 68157460,
                    605028356, 605028372, 67110916, 67110932, 603981828, 603981844, 68159492, 68159508,
                    605030404, 605030420},
            {0, 4096, 65536, 69632, 33554432, 33558528, 33619968, 33624064, 32, 4128, 65568, 69664,
                    33554464, 33558560, 33620000, 33624096, 262144, 266240, 327680, 331776, 33816576,
                    33820672, 33882112, 33886208, 262176, 266272, 327712, 331808, 33816608, 33820704,
                    33882144, 33886240, 8192, 12288, 73728, 77824, 33562624, 33566720, 33628160, 33632256,
                    8224, 12320, 73760, 77856, 33562656, 33566752, 33628192, 33632288, 270336, 274432, 335872,
                    339968, 33824768, 33828864, 33890304, 33894400, 270368, 274464, 335904, 340000, 33824800,
                    33828896, 33890336, 33894432},
            {0, 1024, 16777216, 16778240, 256, 1280, 16777472, 16778496, 268435456, 268436480, 285212672,
                    285213696, 268435712, 268436736, 285212928, 285213952, 524288, 525312, 17301504, 17302528,
                    524544, 525568, 17301760, 17302784, 268959744, 268960768, 285736960, 285737984, 268960000,
                    268961024, 285737216, 285738240, 8, 1032, 16777224, 16778248, 264, 1288, 16777480,
                    16778504, 268435464, 268436488, 285212680, 285213704, 268435720, 268436744, 285212936,
                    285213960, 524296, 525320, 17301512, 17302536, 524552, 525576, 17301768, 17302792,
                    268959752, 268960776, 285736968, 285737992, 268960008, 268961032, 285737224, 285738248}};
    private static final int[][] spbox = new int[][]{
            {8421888, 0, 32768, 8421890, 8421378, 33282, 2, 32768, 512, 8421888, 8421890, 512, 8389122,
                    8421378, 8388608, 2, 514, 8389120, 8389120, 33280, 33280, 8421376, 8421376, 8389122,
                    32770, 8388610, 8388610, 32770, 0, 514, 33282, 8388608, 32768, 8421890, 2, 8421376,
                    8421888, 8388608, 8388608, 512, 8421378, 32768, 33280, 8388610, 512, 2, 8389122, 33282,
                    8421890, 32770, 8421376, 8389122, 8388610, 514, 33282, 8421888, 514, 8389120, 8389120, 0,
                    32770, 33280, 0, 8421378},
            {1074282512, 1073758208, 16384, 540688, 524288, 16, 1074266128, 1073758224, 1073741840,
                    1074282512, 1074282496, 1073741824, 1073758208, 524288, 16, 1074266128, 540672, 524304,
                    1073758224, 0, 1073741824, 16384, 540688, 1074266112, 524304, 1073741840, 0, 540672,
                    16400, 1074282496, 1074266112, 16400, 0, 540688, 1074266128, 524288, 1073758224,
                    1074266112, 1074282496, 16384, 1074266112, 1073758208, 16, 1074282512, 540688, 16, 16384,
                    1073741824, 16400, 1074282496, 524288, 1073741840, 524304, 1073758224, 1073741840, 524304,
                    540672, 0, 1073758208, 16400, 1073741824, 1074266128, 1074282512, 540672},
            {260, 67174656, 0, 67174404, 67109120, 0, 65796, 67109120, 65540, 67108868, 67108868, 65536,
                    67174660, 65540, 67174400, 260, 67108864, 4, 67174656, 256, 65792, 67174400, 67174404,
                    65796, 67109124, 65792, 65536, 67109124, 4, 67174660, 256, 67108864, 67174656, 67108864,
                    65540, 260, 65536, 67174656, 67109120, 0, 256, 65540, 67174660, 67109120, 67108868, 256,
                    0, 67174404, 67109124, 65536, 67108864, 67174660, 4, 65796, 65792, 67108868, 67174400,
                    67109124, 260, 67174400, 65796, 4, 67174404, 65792},
            {-2143285248, -2147479488, -2147479488, 64, 4198464, -2143289280, -2143289344, -2147479552, 0,
                    4198400, 4198400, -2143285184, -2147483584, 0, 4194368, -2143289344, Integer.MIN_VALUE,
                    4096, 4194304, -2143285248, 64, 4194304, -2147479552, 4160, -2143289280,
                    Integer.MIN_VALUE, 4160, 4194368, 4096, 4198464, -2143285184, -2147483584, 4194368,
                    -2143289344, 4198400, -2143285184, -2147483584, 0, 0, 4198400, 4160, 4194368, -2143289280,
                    Integer.MIN_VALUE, -2143285248, -2147479488, -2147479488, 64, -2143285184, -2147483584,
                    Integer.MIN_VALUE, 4096, -2143289344, -2147479552, 4198464, -2143289280, -2147479552,
                    4160, 4194304, -2143285248, 64, 4194304, 4096, 4198464},
            {128, 17039488, 17039360, 553648256, 262144, 128, 536870912, 17039360, 537133184, 262144,
                    16777344, 537133184, 553648256, 553910272, 262272, 536870912, 16777216, 537133056,
                    537133056, 0, 536871040, 553910400, 553910400, 16777344, 553910272, 536871040, 0,
                    553648128, 17039488, 16777216, 553648128, 262272, 262144, 553648256, 128, 16777216,
                    536870912, 17039360, 553648256, 537133184, 16777344, 536870912, 553910272, 17039488,
                    537133184, 128, 16777216, 553910272, 553910400, 262272, 553648128, 553910400, 17039360, 0,
                    537133056, 553648128, 262272, 16777344, 536871040, 262144, 0, 537133056, 17039488,
                    536871040},
            {268435464, 270532608, 8192, 270540808, 270532608, 8, 270540808, 2097152, 268443648, 2105352,
                    2097152, 268435464, 2097160, 268443648, 268435456, 8200, 0, 2097160, 268443656, 8192,
                    2105344, 268443656, 8, 270532616, 270532616, 0, 2105352, 270540800, 8200, 2105344,
                    270540800, 268435456, 268443648, 8, 270532616, 2105344, 270540808, 2097152, 8200,
                    268435464, 2097152, 268443648, 268435456, 8200, 268435464, 270540808, 2105344, 270532608,
                    2105352, 270540800, 0, 270532616, 8, 8192, 270532608, 2105352, 8192, 2097160, 268443656,
                    0, 270540800, 268435456, 2097160, 268443656},
            {1048576, 34603009, 33555457, 0, 1024, 33555457, 1049601, 34604032, 34604033, 1048576, 0,
                    33554433, 1, 33554432, 34603009, 1025, 33555456, 1049601, 1048577, 33555456, 33554433,
                    34603008, 34604032, 1048577, 34603008, 1024, 1025, 34604033, 1049600, 1, 33554432,
                    1049600, 33554432, 1049600, 1048576, 33555457, 33555457, 34603009, 34603009, 1, 1048577,
                    33554432, 33555456, 1048576, 34604032, 1025, 1049601, 34604032, 1025, 33554433, 34604033,
                    34603008, 1049600, 0, 1, 34604033, 0, 1049601, 34603008, 1024, 33554433, 33555456, 1024,
                    1048577},
            {134219808, 2048, 131072, 134350880, 134217728, 134219808, 32, 134217728, 131104, 134348800,
                    134350880, 133120, 134350848, 133152, 2048, 32, 134348800, 134217760, 134219776, 2080,
                    133120, 131104, 134348832, 134350848, 2080, 0, 0, 134348832, 134217760, 134219776, 133152,
                    131072, 133152, 131072, 134350848, 2048, 32, 134348832, 2048, 133152, 134219776, 32,
                    134217760, 134348800, 134348832, 134217728, 131072, 134219808, 0, 134350880, 131104,
                    134217760, 134348800, 134219776, 134219808, 0, 134350880, 133120, 133120, 2080, 2080,
                    131104, 134217728, 134350848}};
    private int[] subkey1 = new int[16];
    private int[] subkey2 = new int[16];

    public DES(long key) {
        this.setKey(key);
    }

    public long bytes2long(byte[] rd) {
        long dd = 0L;

        for (int i = 0; i <= 7; ++i) {
            dd = dd << 8 | (long) rd[i] & 255L;
        }

        return dd;
    }

    public long decrypt(long d) {
        d = this.ip(d);
        int l = (int) (d >>> 32);
        int r = (int) d;

        for (int i = 15; i >= 0; --i) {
            int t = l;
            l = r;
            r = t;
            int t1 = (t >>> 3 | t << 29) ^ this.subkey1[i];
            int t2 = (t << 1 | t >>> 31) ^ this.subkey2[i];
            l ^=
                    spbox[0][t1 >>> 24 & 63] | spbox[1][t2 >>> 24 & 63] | spbox[2][t1 >>> 16 & 63] | spbox[3][
                            t2 >>> 16 & 63] | spbox[4][t1 >>> 8 & 63] | spbox[5][t2 >>> 8 & 63] | spbox[6][t1
                            & 63] | spbox[7][t2 & 63];
        }

        d = (long) l << 32 | (long) r & 4294967295L;
        return this.rip(d);
    }

    public long encrypt(long d) {
        d = this.ip(d);
        int l = (int) (d >>> 32);
        int r = (int) d;

        for (int i = 0; i <= 15; ++i) {
            int t1 = (r >>> 3 | r << 29) ^ this.subkey1[i];
            int t2 = (r << 1 | r >>> 31) ^ this.subkey2[i];
            l ^=
                    spbox[0][t1 >>> 24 & 63] | spbox[1][t2 >>> 24 & 63] | spbox[2][t1 >>> 16 & 63] | spbox[3][
                            t2 >>> 16 & 63] | spbox[4][t1 >>> 8 & 63] | spbox[5][t2 >>> 8 & 63] | spbox[6][t1
                            & 63] | spbox[7][t2 & 63];
            int t = l;
            l = r;
            r = t;
        }

        d = (long) l << 32 | (long) r & 4294967295L;
        return this.rip(d);
    }

    private long ip(long d) {
        long t = (d << 36 ^ d) & -1085102596613472256L;
        d ^= t >>> 36 | t;
        t = (d << 48 ^ d) & -281474976710656L;
        d ^= t >>> 48 | t;
        t = (d << 30 ^ d) & 3689348813882916864L;
        d ^= t >>> 30 | t;
        t = (d << 24 ^ d) & 71777214277877760L;
        d ^= t >>> 24 | t;
        t = (d << 33 ^ d) & -6148914694099828736L;
        d ^= t >>> 33 | t;
        return d;
    }

    public void long2bytes(long sd, byte[] dd) {
        for (int i = 7; i >= 0; --i) {
            dd[i] = (byte) ((int) sd);
            sd >>>= 8;
        }

    }

    private long rip(long d) {
        long t = (d << 33 ^ d) & -6148914694099828736L;
        d ^= t >>> 33 | t;
        t = (d << 24 ^ d) & 71777214277877760L;
        d ^= t >>> 24 | t;
        t = (d << 30 ^ d) & 3689348813882916864L;
        d ^= t >>> 30 | t;
        t = (d << 48 ^ d) & -281474976710656L;
        d ^= t >>> 48 | t;
        t = (d << 36 ^ d) & -1085102596613472256L;
        d ^= t >>> 36 | t;
        return d;
    }

    public void setKey(long key) {
        long t = (key << 36 ^ key) & -1085102596613472256L;
        key ^= t >>> 36 | t;
        t = (key << 18 ^ key) & -3689573991287357440L;
        key ^= t | t >>> 18;
        t = (key << 9 ^ key) & -6196766167432910336L;
        key ^= t | t >>> 9;
        int d = (int) (key >>> 28 & 268435440L | key >>> 24 & 15L);
        int c = (int) (key << 20 & 267386880L | key << 4 & 1044480L | key >>> 12 & 4080L
                | key >>> 28 & 15L);

        for (int i = 0; i < 16; ++i) {
            if (ks[i]) {
                c = (c << 2 | c >>> 26) & 268435455;
                d = (d << 2 | d >>> 26) & 268435455;
            } else {
                c = (c << 1 | c >>> 27) & 268435455;
                d = (d << 1 | d >>> 27) & 268435455;
            }

            int t1 = kp[0][c >>> 22 & 63] | kp[1][c >>> 16 & 48 | c >>> 15 & 15] | kp[2][c >>> 9 & 60
                    | c >>> 8 & 3] | kp[3][c >>> 3 & 32 | c >>> 1 & 24 | c & 7];
            int t2 =
                    kp[4][c >>> 22 & 63] | kp[5][c >>> 15 & 48 | c >>> 14 & 15] | kp[6][c >>> 7 & 63] | kp[7][
                            c >>> 1 & 60 | c & 3];
            this.subkey1[i] =
                    t1 & -16777216 | (t1 & '\uff00') << 8 | (t2 & -16777216) >>> 16 | (t2 & '\uff00') >>> 8;
            this.subkey2[i] = (t1 & 16711680) << 8 | (t1 & 255) << 16 | (t2 & 16711680) >>> 8 | t2 & 255;
        }

    }
}
