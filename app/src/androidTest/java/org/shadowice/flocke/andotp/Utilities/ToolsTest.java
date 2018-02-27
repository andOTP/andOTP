/*
 * Copyright (C) 2018 Jakob Nixdorf
 * Copyright (C) 2018 Daniel Weigl
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.shadowice.flocke.andotp.Utilities;

import junit.framework.TestCase;

public class ToolsTest extends TestCase {
   public void testFormatToken() throws Exception {
      assertEquals("123 456", Tools.formatToken("123456", 3));
      assertEquals("12 34 56", Tools.formatToken("123456", 2));
      assertEquals("123456", Tools.formatToken("123456", 0));
      assertEquals("123456", Tools.formatToken("123456", 10));
      assertEquals("1 234 567", Tools.formatToken("1234567", 3));
      assertEquals("1ab 234 567", Tools.formatToken("1ab234567", 3));
      assertEquals("123", Tools.formatToken("123", 3));
      assertEquals("1 234", Tools.formatToken("1234", 3));
      assertEquals("1", Tools.formatToken("1", 3));
      assertEquals("", Tools.formatToken("", 3));
   }
}