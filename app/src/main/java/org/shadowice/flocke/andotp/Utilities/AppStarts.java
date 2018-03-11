package org.shadowice.flocke.andotp.Utilities;

/**
     * Distinguishes different kinds of app starts: <li>
     * <ul>
     * First start ever ({@link #FIRST_NAME})
     * </ul>
     * <ul>
     * First start in this version ({@link #FIRST_TIME_VERSION})
     * </ul>
     * <ul>
     * Normal app start ({@link #NORMAL})
     * </ul>
     *
     * @author Gigabyte Developers Incorporated
     * inspired by
     * @author Emmanuel Nwokoma
     *
     */

public class AppStarts {
    public enum AppStart {
        FIRST_TIME, FIRST_TIME_VERSION, NORMAL;
    }
}