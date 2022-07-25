package fr.thomas.maugin.flutter_play_licensing.flutter_play_licensing;

import android.text.TextUtils;
import android.util.Log;

import com.google.android.vending.licensing.ResponseData;
import com.google.android.vending.licensing.util.Base64;
import com.google.android.vending.licensing.util.Base64DecoderException;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

class ServerSideLicenseValidator {
    private static final String TAG = "LicenseValidator";

    // Server response codes.
    private static final int LICENSED = 0x0;
    private static final int NOT_LICENSED = 0x1;
    private static final int LICENSED_OLD_KEY = 0x2;
    private static final int ERROR_NOT_MARKET_MANAGED = 0x3;
    private static final int ERROR_SERVER_FAILURE = 0x4;
    private static final int ERROR_OVER_QUOTA = 0x5;

    private static final int ERROR_CONTACTING_SERVER = 0x101;
    private static final int ERROR_INVALID_PACKAGE_NAME = 0x102;
    private static final int ERROR_NON_MATCHING_UID = 0x103;

    private final ServerSideLicenseCheckerCallback mCallback;
    private final int mNonce;
    private final String mPackageName;
    private final String mVersionCode;

    ServerSideLicenseValidator(ServerSideLicenseCheckerCallback callback,
                               int nonce, String packageName, String versionCode) {
        mCallback = callback;
        mNonce = nonce;
        mPackageName = packageName;
        mVersionCode = versionCode;
    }

    public ServerSideLicenseCheckerCallback getCallback() {
        return mCallback;
    }

    public int getNonce() {
        return mNonce;
    }

    public String getPackageName() {
        return mPackageName;
    }

    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    /**
     * Verifies the response from server and calls appropriate callback method.
     *
     * @param publicKey    public key associated with the developer account
     * @param responseCode server response code
     * @param signedData   signed data from server
     * @param signature    server signature
     */
    public void verify(PublicKey publicKey, int responseCode, String signedData, String signature) {
        String userId = null;
        // Skip signature check for unsuccessful requests
        ResponseData data;
        if (responseCode == LICENSED || responseCode == NOT_LICENSED ||
                responseCode == LICENSED_OLD_KEY) {
            // Verify signature.
            try {
                if (TextUtils.isEmpty(signedData)) {
                    Log.e(TAG, "Signature verification failed: signedData is empty. " +
                            "(Device not signed-in to any Google accounts?)");
                    handleInvalidResponse();
                    return;
                }

                Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
                sig.initVerify(publicKey);
                sig.update(signedData.getBytes());

                if (!sig.verify(Base64.decode(signature))) {
                    Log.e(TAG, "Signature verification failed.");
                    handleInvalidResponse();
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                // This can't happen on an Android compatible device.
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                handleApplicationError(ServerSideLicenseCheckerCallback.ERROR_INVALID_PUBLIC_KEY);
                return;
            } catch (SignatureException e) {
                throw new RuntimeException(e);
            } catch (Base64DecoderException e) {
                Log.e(TAG, "Could not Base64-decode signature.");
                handleInvalidResponse();
                return;
            }

            // Parse and validate response.
            try {
                data = ResponseData.parse(signedData);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Could not parse response.");
                handleInvalidResponse();
                return;
            }

            if (data.responseCode != responseCode) {
                Log.e(TAG, "Response codes don't match.");
                handleInvalidResponse();
                return;
            }

            if (data.nonce != mNonce) {
                Log.e(TAG, "Nonce doesn't match.");
                handleInvalidResponse();
                return;
            }

            if (!data.packageName.equals(mPackageName)) {
                Log.e(TAG, "Package name doesn't match.");
                handleInvalidResponse();
                return;
            }

            if (!data.versionCode.equals(mVersionCode)) {
                Log.e(TAG, "Version codes don't match.");
                handleInvalidResponse();
                return;
            }

            // Application-specific user identifier.
            userId = data.userId;
            if (TextUtils.isEmpty(userId)) {
                Log.e(TAG, "User identifier is empty.");
                handleInvalidResponse();
                return;
            }
        }

        switch (responseCode) {
            case LICENSED:
            case LICENSED_OLD_KEY:
                handleResponse(responseCode, signedData);
                break;
            case NOT_LICENSED:
                Log.w(TAG, "Error not licensed.");
                handleResponse(responseCode, signedData);
                break;
            case ERROR_CONTACTING_SERVER:
                Log.w(TAG, "Error contacting licensing server.");
                handleResponse(responseCode, signedData);
                break;
            case ERROR_SERVER_FAILURE:
                Log.w(TAG, "An error has occurred on the licensing server.");
                handleResponse(responseCode, signedData);
                break;
            case ERROR_OVER_QUOTA:
                Log.w(TAG, "Licensing server is refusing to talk to this device, over quota.");
                handleResponse(responseCode, signedData);
                break;
            case ERROR_INVALID_PACKAGE_NAME:
            case ERROR_NON_MATCHING_UID:
            case ERROR_NOT_MARKET_MANAGED:
                handleApplicationError(responseCode);
                break;
            default:
                Log.e(TAG, "Unknown response code for license check.");
                handleApplicationError(ServerSideLicenseCheckerCallback.UNKNOWN_ERROR);
        }
    }

    /**
     * Confers with policy and calls appropriate callback method.
     *
     * @param response
     * @param rawData
     */
    private void handleResponse(int response, String rawData) {
        mCallback.response(response, rawData);
    }

    private void handleApplicationError(int code) {
        mCallback.applicationError(code);
    }

    private void handleInvalidResponse() {
        mCallback.applicationError(ServerSideLicenseCheckerCallback.ERROR_INVALID_RESPONSE);
    }
}
