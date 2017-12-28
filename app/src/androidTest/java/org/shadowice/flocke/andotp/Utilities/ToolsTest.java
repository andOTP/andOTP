package org.shadowice.flocke.andotp.Utilities;

import junit.framework.TestCase;

public class ToolsTest extends TestCase {
   public void testFormatToken() throws Exception {
      assertEquals("123 456", Tools.formatToken("123456", 3));
      assertEquals("1 234 567", Tools.formatToken("1234567", 3));
      assertEquals("1ab 234 567", Tools.formatToken("1ab234567", 3));
      assertEquals("123", Tools.formatToken("123", 3));
      assertEquals("1 234", Tools.formatToken("1234", 3));
      assertEquals("1", Tools.formatToken("1", 3));
      assertEquals("", Tools.formatToken("", 3));
   }
}