/*
 * $Header: /u/users20/santtu/src/java/MD5/RCS/MD5.java,v 1.5 1996/12/12 10:47:02 santtu Exp $
 *
 * MD5 in Java JDK Beta-2
 * written Santeri Paavolainen, Helsinki Finland 1996
 * (c) Santeri Paavolainen, Helsinki Finland 1996
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * See http://www.cs.hut.fi/~santtu/java/ for more information on this
 * class.
 *
 * This is rather straight re-implementation of the reference implementation
 * given in RFC1321 by RSA.
 *
 * Passes MD5 test suite as defined in RFC1321.
 *
 *
 * This Java class has been derived from the RSA Data Security, Inc. MD5
 * Message-Digest Algorithm and its reference implementation.
 *
 *
 * Revision 1.6 minor changes for j2me, 2003, Matthias Straub
 *
 * $Log: MD5.java,v $
 * Revision 1.5  1996/12/12 10:47:02  santtu
 * Changed GPL to LGPL
 *
 * Revision 1.4  1996/12/12 10:30:02  santtu
 * Some typos, State -> MD5State etc.
 *
 * Revision 1.3  1996/04/15 07:28:09  santtu
 * Added GPL statemets, and RSA derivate stametemetsnnts.
 *
 * Revision 1.2  1996/03/04 08:05:48  santtu
 * Added offsets to Update method
 *
 * Revision 1.1  1996/01/07 20:51:59  santtu
 * Initial revision
 *
 */

/**
 * Contains internal state of the MD5 class
 */
package org.shadowice.flocke.andotp.Utilities;

class MD5State {
    /**
     * 128-byte state
     */
    int	state[];

    /**
     * 64-bit character count (could be true Java long?)
     */
    int	count[];

    /**
     * 64-byte buffer (512 bits) for storing to-be-hashed characters
     */
    byte	buffer[];

    public MD5State() {
        buffer = new byte[64];
        count = new int[2];
        state = new int[4];

        state[0] = 0x67452301;
        state[1] = 0xefcdab89;
        state[2] = 0x98badcfe;
        state[3] = 0x10325476;

        count[0] = count[1] = 0;
    }

    /** Create this State as a copy of another state */
    public MD5State (MD5State from) {
        this();

        int i;

        for (i = 0; i < buffer.length; i++)
            this.buffer[i] = from.buffer[i];

        for (i = 0; i < state.length; i++)
            this.state[i] = from.state[i];

        for (i = 0; i < count.length; i++)
            this.count[i] = from.count[i];
    }
};

/**
 * Implementation of RSA's MD5 hash generator
 *
 * @version	$Revision: 1.5 $
 * @author	Santeri Paavolainen <sjpaavol@cc.helsinki.fi>
 */

public class MD5 {
    /**
     * MD5 state
     */
    MD5State	state;

    /**
     * If Final() has been called, finals is set to the current finals
     * state. Any Update() causes this to be set to null.
     */
    MD5State 	finals;

    /**
     * Padding for Final()
     */
    static byte	padding[] = {
            (byte) 0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    /**
     * Initialize MD5 internal state (object can be reused just by
     * calling Init() after every Final()
     */
    public synchronized void Init () {
        state = new MD5State();
        finals = null;
    }

    /**
     * Class constructor
     */
    public MD5 () {
        this.Init();
    }

    /**
     * Initialize class, and update hash with ob.toString()
     *
     * @param	ob	Object, ob.toString() is used to update hash
     *			after initialization
     */
    public MD5 (Object ob) {
        this();
        Update(ob.toString());
    }

    private int rotate_left (int x, int n) {
        return (x << n) | (x >>> (32 - n));
    }

  /* I wonder how many loops and hoops you'll have to go through to
     get unsigned add for longs in java */

    private int uadd (int a, int b) {
        long aa, bb;
        aa = ((long) a) & 0xffffffffL;
        bb = ((long) b) & 0xffffffffL;

        aa += bb;

        return (int) (aa & 0xffffffffL);
    }

    private int uadd (int a, int b, int c) {
        return uadd(uadd(a, b), c);
    }

    private int uadd (int a, int b, int c, int d) {
        return uadd(uadd(a, b, c), d);
    }

    private int FF (int a, int b, int c, int d, int x, int s, int ac) {
        a = uadd(a, ((b & c) | (~b & d)), x, ac);
        return uadd(rotate_left(a, s), b);
    }

    private int GG (int a, int b, int c, int d, int x, int s, int ac) {
        a = uadd(a, ((b & d) | (c & ~d)), x, ac);
        return uadd(rotate_left(a, s), b);
    }

    private int HH (int a, int b, int c, int d, int x, int s, int ac) {
        a = uadd(a, (b ^ c ^ d), x, ac);
        return uadd(rotate_left(a, s) , b);
    }

    private int II (int a, int b, int c, int d, int x, int s, int ac) {
        a = uadd(a, (c ^ (b | ~d)), x, ac);
        return uadd(rotate_left(a, s), b);
    }

    private int[] Decode (byte buffer[], int len, int shift) {
        int		out[];
        int 	i, j;

        out = new int[16];

        for (i = j = 0; j < len; i++, j += 4) {
            out[i] = ((int) (buffer[j + shift] & 0xff)) |
                    (((int) (buffer[j + 1 + shift] & 0xff)) << 8) |
                    (((int) (buffer[j + 2 + shift] & 0xff)) << 16) |
                    (((int) (buffer[j + 3 + shift] & 0xff)) << 24);

/*      System.out.println("out[" + i + "] = \t" +
			 ((int) buffer[j + 0 + shift] & 0xff) + "\t|\t" +
			 ((int) buffer[j + 1 + shift] & 0xff) + "\t|\t" +
			 ((int) buffer[j + 2 + shift] & 0xff) + "\t|\t" +
			 ((int) buffer[j + 3 + shift] & 0xff));*/
        }

        return out;
    }

    private void Transform (MD5State state, byte buffer[], int shift) {
        int
                a = state.state[0],
                b = state.state[1],
                c = state.state[2],
                d = state.state[3],
                x[];

        x = Decode(buffer, 64, shift);

        /* Round 1 */
        a = FF (a, b, c, d, x[ 0],   7, 0xd76aa478); /* 1 */
        d = FF (d, a, b, c, x[ 1],  12, 0xe8c7b756); /* 2 */
        c = FF (c, d, a, b, x[ 2],  17, 0x242070db); /* 3 */
        b = FF (b, c, d, a, x[ 3],  22, 0xc1bdceee); /* 4 */
        a = FF (a, b, c, d, x[ 4],   7, 0xf57c0faf); /* 5 */
        d = FF (d, a, b, c, x[ 5],  12, 0x4787c62a); /* 6 */
        c = FF (c, d, a, b, x[ 6],  17, 0xa8304613); /* 7 */
        b = FF (b, c, d, a, x[ 7],  22, 0xfd469501); /* 8 */
        a = FF (a, b, c, d, x[ 8],   7, 0x698098d8); /* 9 */
        d = FF (d, a, b, c, x[ 9],  12, 0x8b44f7af); /* 10 */
        c = FF (c, d, a, b, x[10],  17, 0xffff5bb1); /* 11 */
        b = FF (b, c, d, a, x[11],  22, 0x895cd7be); /* 12 */
        a = FF (a, b, c, d, x[12],   7, 0x6b901122); /* 13 */
        d = FF (d, a, b, c, x[13],  12, 0xfd987193); /* 14 */
        c = FF (c, d, a, b, x[14],  17, 0xa679438e); /* 15 */
        b = FF (b, c, d, a, x[15],  22, 0x49b40821); /* 16 */

        /* Round 2 */
        a = GG (a, b, c, d, x[ 1],   5, 0xf61e2562); /* 17 */
        d = GG (d, a, b, c, x[ 6],   9, 0xc040b340); /* 18 */
        c = GG (c, d, a, b, x[11],  14, 0x265e5a51); /* 19 */
        b = GG (b, c, d, a, x[ 0],  20, 0xe9b6c7aa); /* 20 */
        a = GG (a, b, c, d, x[ 5],   5, 0xd62f105d); /* 21 */
        d = GG (d, a, b, c, x[10],   9,  0x2441453); /* 22 */
        c = GG (c, d, a, b, x[15],  14, 0xd8a1e681); /* 23 */
        b = GG (b, c, d, a, x[ 4],  20, 0xe7d3fbc8); /* 24 */
        a = GG (a, b, c, d, x[ 9],   5, 0x21e1cde6); /* 25 */
        d = GG (d, a, b, c, x[14],   9, 0xc33707d6); /* 26 */
        c = GG (c, d, a, b, x[ 3],  14, 0xf4d50d87); /* 27 */
        b = GG (b, c, d, a, x[ 8],  20, 0x455a14ed); /* 28 */
        a = GG (a, b, c, d, x[13],   5, 0xa9e3e905); /* 29 */
        d = GG (d, a, b, c, x[ 2],   9, 0xfcefa3f8); /* 30 */
        c = GG (c, d, a, b, x[ 7],  14, 0x676f02d9); /* 31 */
        b = GG (b, c, d, a, x[12],  20, 0x8d2a4c8a); /* 32 */

        /* Round 3 */
        a = HH (a, b, c, d, x[ 5],   4, 0xfffa3942); /* 33 */
        d = HH (d, a, b, c, x[ 8],  11, 0x8771f681); /* 34 */
        c = HH (c, d, a, b, x[11],  16, 0x6d9d6122); /* 35 */
        b = HH (b, c, d, a, x[14],  23, 0xfde5380c); /* 36 */
        a = HH (a, b, c, d, x[ 1],   4, 0xa4beea44); /* 37 */
        d = HH (d, a, b, c, x[ 4],  11, 0x4bdecfa9); /* 38 */
        c = HH (c, d, a, b, x[ 7],  16, 0xf6bb4b60); /* 39 */
        b = HH (b, c, d, a, x[10],  23, 0xbebfbc70); /* 40 */
        a = HH (a, b, c, d, x[13],   4, 0x289b7ec6); /* 41 */
        d = HH (d, a, b, c, x[ 0],  11, 0xeaa127fa); /* 42 */
        c = HH (c, d, a, b, x[ 3],  16, 0xd4ef3085); /* 43 */
        b = HH (b, c, d, a, x[ 6],  23,  0x4881d05); /* 44 */
        a = HH (a, b, c, d, x[ 9],   4, 0xd9d4d039); /* 45 */
        d = HH (d, a, b, c, x[12],  11, 0xe6db99e5); /* 46 */
        c = HH (c, d, a, b, x[15],  16, 0x1fa27cf8); /* 47 */
        b = HH (b, c, d, a, x[ 2],  23, 0xc4ac5665); /* 48 */

        /* Round 4 */
        a = II (a, b, c, d, x[ 0],   6, 0xf4292244); /* 49 */
        d = II (d, a, b, c, x[ 7],  10, 0x432aff97); /* 50 */
        c = II (c, d, a, b, x[14],  15, 0xab9423a7); /* 51 */
        b = II (b, c, d, a, x[ 5],  21, 0xfc93a039); /* 52 */
        a = II (a, b, c, d, x[12],   6, 0x655b59c3); /* 53 */
        d = II (d, a, b, c, x[ 3],  10, 0x8f0ccc92); /* 54 */
        c = II (c, d, a, b, x[10],  15, 0xffeff47d); /* 55 */
        b = II (b, c, d, a, x[ 1],  21, 0x85845dd1); /* 56 */
        a = II (a, b, c, d, x[ 8],   6, 0x6fa87e4f); /* 57 */
        d = II (d, a, b, c, x[15],  10, 0xfe2ce6e0); /* 58 */
        c = II (c, d, a, b, x[ 6],  15, 0xa3014314); /* 59 */
        b = II (b, c, d, a, x[13],  21, 0x4e0811a1); /* 60 */
        a = II (a, b, c, d, x[ 4],   6, 0xf7537e82); /* 61 */
        d = II (d, a, b, c, x[11],  10, 0xbd3af235); /* 62 */
        c = II (c, d, a, b, x[ 2],  15, 0x2ad7d2bb); /* 63 */
        b = II (b, c, d, a, x[ 9],  21, 0xeb86d391); /* 64 */

        state.state[0] += a;
        state.state[1] += b;
        state.state[2] += c;
        state.state[3] += d;
    }

    /**
     * Updates hash with the bytebuffer given (using at maximum length bytes from
     * that buffer)
     *
     * @param stat	Which state is updated
     * @param buffer	Array of bytes to be hashed
     * @param offset	Offset to buffer array
     * @param length	Use at maximum `length' bytes (absolute
     *			maximum is buffer.length)
     */
    public void Update (MD5State stat, byte buffer[], int offset, int length) {
        int	index, partlen, i, start;

/*    System.out.print("Offset = " + offset + "\tLength = " + length + "\t");
    System.out.print("Buffer = ");
    for (i = 0; i < buffer.length; i++)
	System.out.print((int) (buffer[i] & 0xff) + " ");
    System.out.print("\n");*/

        finals = null;

        /* Length can be told to be shorter, but not inter */
        if ((length - offset)> buffer.length)
            length = buffer.length - offset;

        /* compute number of bytes mod 64 */
        index = (int) (stat.count[0] >>> 3) & 0x3f;

        if ((stat.count[0] += (length << 3)) <
                (length << 3))
            stat.count[1]++;

        stat.count[1] += length >>> 29;

        partlen = 64 - index;

        if (length >= partlen) {
            for (i = 0; i < partlen; i++)
                stat.buffer[i + index] = buffer[i + offset];

            Transform(stat, stat.buffer, 0);

            for (i = partlen; (i + 63) < length; i+= 64)
                Transform(stat, buffer, i);

            index = 0;
        } else
            i = 0;

        /* buffer remaining input */
        if (i < length) {
            start = i;
            for (; i < length; i++)
                stat.buffer[index + i - start] = buffer[i + offset];
        }
    }

    /*
     * Update()s for other datatypes than byte[] also. Update(byte[], int)
     * is only the main driver.
     */

    /**
     * Plain update, updates this object
     */

    public void Update (byte buffer[], int offset, int length) {
        Update(this.state, buffer, offset, length);
    }

    public void Update (byte buffer[], int length) {
        Update(this.state, buffer, 0, length);
    }

    /**
     * Updates hash with given array of bytes
     *
     * @param buffer	Array of bytes to use for updating the hash
     */
    public void Update (byte buffer[]) {
        Update(buffer, 0, buffer.length);
    }

    /**
     * Updates hash with a single byte
     *
     * @param b		Single byte to update the hash
     */
    public void Update (byte b) {
        byte buffer[] = new byte[1];
        buffer[0] = b;

        Update(buffer, 1);
    }

    /**
     * Update buffer with given string.
     *
     * @param s		String to be update to hash (is used as
     *		       	s.getBytes())
     */
    public void Update (String s) {
        byte	chars[];

        chars = new byte[s.length()];
        chars=s.getBytes();

        Update(chars, chars.length);
    }

    /**
     * Update buffer with a single integer (only & 0xff part is used,
     * as a byte)
     *
     * @param i		Integer value, which is then converted to
     *			byte as i & 0xff
     */

    public void Update (int i) {
        Update((byte) (i & 0xff));
    }

    private byte[] Encode (int input[], int len) {
        int		i, j;
        byte	out[];

        out = new byte[len];

        for (i = j = 0; j  < len; i++, j += 4) {
            out[j] = (byte) (input[i] & 0xff);
            out[j + 1] = (byte) ((input[i] >>> 8) & 0xff);
            out[j + 2] = (byte) ((input[i] >>> 16) & 0xff);
            out[j + 3] = (byte) ((input[i] >>> 24) & 0xff);
        }

        return out;
    }

    /**
     * Returns array of bytes (16 bytes) representing hash as of the
     * current state of this object. Note: getting a hash does not
     * invalidate the hash object, it only creates a copy of the real
     * state which is finalized.
     *
     * @return	Array of 16 bytes, the hash of all updated bytes
     */
    public synchronized byte[] Final () {
        byte	bits[];
        int		index, padlen;
        MD5State	fin;

        if (finals == null) {
            fin = new MD5State(state);

            bits = Encode(fin.count, 8);

            index = (int) ((fin.count[0] >>> 3) & 0x3f);
            padlen = (index < 56) ? (56 - index) : (120 - index);

            Update(fin, padding, 0, padlen);
            /**/
            Update(fin, bits, 0, 8);

            /* Update() sets finalds to null */
            finals = fin;
        }

        return Encode(finals.state, 16);
    }

    /**
     * Turns array of bytes into string representing each byte as
     * unsigned hex number.
     *
     * @param hash	Array of bytes to convert to hex-string
     * @return	Generated hex string
     */
    public static String asHex (byte hash[]) {
        StringBuffer buf = new StringBuffer(hash.length * 2);
        int i;

        for (i = 0; i < hash.length; i++) {
            if (((int) hash[i] & 0xff) < 0x10)
                buf.append("0");

            buf.append(Long.toString((int) hash[i] & 0xff, 16));
        }

        return buf.toString();
    }

    /**
     * Returns 32-character hex representation of this objects hash
     *
     * @return String of this object's hash
     */
    public String asHex () {
        return asHex(this.Final());
    }
}
