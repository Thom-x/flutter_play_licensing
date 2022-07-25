package fr.thomas.maugin.flutter_play_licensing.flutter_play_licensing;

import com.google.android.vending.licensing.ResponseData;

/**
 * Callback for the license checker library.
 * <b>The callback does not occur in the original checking thread.</b> Your
 * application should post to the appropriate handling thread or lock
 * accordingly.
 * <p>
 * The reason that is passed back with allow/dontAllow is the base status handed
 * to the policy for allowed/disallowing the license. Policy.RETRY will call
 * allow or dontAllow depending on other statistics associated with the policy,
 * while in most cases Policy.NOT_LICENSED will call dontAllow and
 * Policy.LICENSED will Allow.
 */
public interface ServerSideLicenseCheckerCallback {

    /**
     * Allow use. App should proceed as normal.
     *
     * @param reason Policy.LICENSED or Policy.RETRY typically. (although in
     *            theory the policy can return Policy.NOT_LICENSED here as well)
     */
    public void response(int reason, String rawData);

    /** Application error codes. */
    public static final int ERROR_INVALID_PACKAGE_NAME = 1;
    public static final int ERROR_NON_MATCHING_UID = 2;
    public static final int ERROR_NOT_MARKET_MANAGED = 3;
    public static final int ERROR_CHECK_IN_PROGRESS = 4;
    public static final int ERROR_INVALID_PUBLIC_KEY = 5;
    public static final int ERROR_MISSING_PERMISSION = 6;
    public static final int ERROR_CONNECTION_ERROR = 7;
    public static final int ERROR_INVALID_RESPONSE = 8;
    public static final int UNKNOWN_ERROR = 99;

    /**
     * Error in application code. Caller did not call or set up license checker
     * correctly. Should be considered fatal.
     */
    public void applicationError(int errorCode);
}
