/*
 * Copyright (C) 2009 Google Inc.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myapplication.kunal52.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;


/**
 * A convenience wrapper to generate an {@link SSLServerSocketFactory} that uses
 * the given {@link KeyManager} and {@link TrustManager} instances.
 */
public class SSLServerSocketFactoryWrapper extends SSLServerSocketFactory {
  
  /**
   * The internal SSLServerSocketFactory which will be wrapped.
   */
  private SSLServerSocketFactory mFactory;

  public SSLServerSocketFactoryWrapper(KeyManager[] keyManagers,
      TrustManager[] trustManagers)
      throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext sslcontext = SSLContext.getInstance("TLS");
    sslcontext.init(keyManagers, trustManagers, null);
    mFactory = sslcontext.getServerSocketFactory();
  }
  
  public static SSLServerSocketFactoryWrapper CreateWithDummyTrustManager(
      KeyManager[] keyManagers) throws KeyManagementException,
      NoSuchAlgorithmException {
    TrustManager[] trustManagers = { new DummyTrustManager() };
    return new SSLServerSocketFactoryWrapper(keyManagers, trustManagers);
  }

  @Override
  public ServerSocket createServerSocket(int port) throws IOException {
    return mFactory.createServerSocket(port);
  }

  @Override
  public ServerSocket createServerSocket(int port, int backlog)
      throws IOException {
    return mFactory.createServerSocket(port, backlog);
  }

  @Override
  public ServerSocket createServerSocket(int port, int backlog,
      InetAddress ifAddress) throws IOException {
    return mFactory.createServerSocket(port, backlog, ifAddress);
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return mFactory.getDefaultCipherSuites();
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return mFactory.getSupportedCipherSuites();
  }

}
