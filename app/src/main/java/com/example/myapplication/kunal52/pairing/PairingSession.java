package com.example.myapplication.kunal52.pairing;

import android.content.Context;

import com.example.myapplication.kunal52.AndroidRemoteContext;
import com.example.myapplication.kunal52.exception.PairingException;
import com.example.myapplication.kunal52.ssl.DummyTrustManager;
import com.example.myapplication.kunal52.ssl.KeyStoreManager;
import com.example.myapplication.kunal52.util.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class PairingSession {

//    private final Logger logger = LoggerFactory.getLogger(PairingSession.class);

    private final BlockingQueue<Pairingmessage.PairingMessage> mMessagesQueue;

    private final PairingMessageManager mPairingMessageManager;

    SecretProvider secretProvider;

    private SSLSocket mSslSocket;

    public PairingSession() {
        mMessagesQueue = new LinkedBlockingDeque<>();
        mPairingMessageManager = new PairingMessageManager();
    }

    public void pair(Context context, String host, int port, PairingListener pairingListener) throws GeneralSecurityException, IOException, InterruptedException, PairingException {

        KeyStoreManager keyStoreManager = new KeyStoreManager(context);
        SSLContext sSLContext = SSLContext.getInstance("TLS");
        if (!keyStoreManager.hasServerIdentityAlias()) {
            keyStoreManager.initializeKeyStore();
        }
        sSLContext.init(keyStoreManager.getKeyManagers(), new TrustManager[]{new DummyTrustManager()}, new SecureRandom());
        SSLSocketFactory sslsocketfactory = sSLContext.getSocketFactory();
        SSLSocket sSLSocket = (SSLSocket) sslsocketfactory.createSocket(host, port);
//        sSLSocket.setEnabledProtocols(new String[]{"TLSv1", "TLSv1.1", "TLSv1.2"});
//        sSLSocket.setSoTimeout(5000);
//        sSLSocket.connect(new InetSocketAddress(host, port), 5000);
        mSslSocket = sSLSocket;
//        sSLSocket.setSoTimeout(0);
//        sSLSocket.setNeedClientAuth(true);
//        sSLSocket.setUseClientMode(true);
//        sSLSocket.setKeepAlive(true);
//        sSLSocket.setTcpNoDelay(true);
//        sSLSocket.startHandshake();

        pairingListener.onSessionCreated();
        PairingPacketParser pairingPacketParser = new PairingPacketParser(sSLSocket.getInputStream(), mMessagesQueue);
        pairingPacketParser.start();

        final OutputStream outputStream = sSLSocket.getOutputStream();

        byte[] pairingMessage = mPairingMessageManager.createPairingMessage(AndroidRemoteContext.getInstance().getClientName(), AndroidRemoteContext.getInstance().getServiceName());
        outputStream.write(pairingMessage);
        Pairingmessage.PairingMessage pairingMessageResponse = waitForMessage();
        logReceivedMessage(pairingMessageResponse.toString());

        byte[] pairingOption = new PairingMessageManager().createPairingOption();
        outputStream.write(pairingOption);
        Pairingmessage.PairingMessage pairingOptionAck = waitForMessage();
        logReceivedMessage(pairingOptionAck.toString());

        byte[] configMessage = new PairingMessageManager().createConfigMessage();
        outputStream.write(configMessage);
        Pairingmessage.PairingMessage pairingConfigAck = waitForMessage();
        logReceivedMessage(pairingConfigAck.toString());

        if (secretProvider != null)
            secretProvider.requestSecret(this);
        pairingListener.onSecretRequested();
//        logger.info("Waiting for secret");
        Pairingmessage.PairingMessage pairingSecretMessage = waitForMessage();
        byte[] secretMessage = mPairingMessageManager.createSecretMessage(pairingSecretMessage);
        outputStream.write(secretMessage);
        Pairingmessage.PairingMessage pairingSecretAck = waitForMessage();
        logReceivedMessage(pairingSecretAck.toString());

        pairingListener.onPaired();
        pairingListener.onSessionEnded();
    }


    Pairingmessage.PairingMessage waitForMessage() throws InterruptedException, PairingException {
        Pairingmessage.PairingMessage pairingMessage = mMessagesQueue.take();
        if (pairingMessage.getStatus() != Pairingmessage.PairingMessage.Status.STATUS_OK) {
            throw new PairingException(pairingMessage.toString());
        }
        return pairingMessage;
    }


    public void provideSecret(String secret) {
        createCodeSecret(secret);
    }

    private void createCodeSecret(String code) {
        code = code.substring(2);
        PairingChallengeResponse pairingChallengeResponse = new PairingChallengeResponse(Utils.getLocalCert(mSslSocket.getSession()), Utils.getPeerCert(mSslSocket.getSession()));
        byte[] secret = Utils.hexStringToBytes(code);
        System.out.println(Arrays.toString(secret));
        try {
            pairingChallengeResponse.checkGamma(secret);
        } catch (PairingException e) {
            throw new RuntimeException(e);
        }
        byte[] pairingChallengeResponseAlpha;
        try {
            pairingChallengeResponseAlpha = pairingChallengeResponse.getAlpha(secret);
        } catch (PairingException e) {
            throw new RuntimeException(e);
        }
        Pairingmessage.PairingMessage secretMessageProto = new PairingMessageManager().createSecretMessageProto(pairingChallengeResponseAlpha);
        try {
            mMessagesQueue.put(secretMessageProto);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    void logSendMessage(String message) {
//        logger.info("Send Message : {}", message);
    }

    void logReceivedMessage(String message) {
//        logger.info("Received Message : {}", message);
    }

}
