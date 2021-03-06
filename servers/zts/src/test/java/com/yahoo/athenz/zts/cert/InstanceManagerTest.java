package com.yahoo.athenz.zts.cert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNotNull;

import com.yahoo.athenz.auth.util.Crypto;
import com.yahoo.athenz.common.server.cert.CertSigner;
import com.yahoo.athenz.zts.InstanceIdentity;

public class InstanceManagerTest {

    @Test
    public void testGenerateIdentity() {
        
        final String cert = "cert";
        final String caCert = "caCert";
        CertSigner certSigner = Mockito.mock(com.yahoo.athenz.common.server.cert.CertSigner.class);
        Mockito.when(certSigner.generateX509Certificate(Mockito.anyString())).thenReturn(cert);
        Mockito.when(certSigner.getCACertificate()).thenReturn(caCert);
        
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        InstanceManager instanceManager = new InstanceManager(null);
        InstanceIdentity identity = instanceManager.generateIdentity(certSigner, "csr", "cn", map);
        
        assertNotNull(identity);
        assertEquals(identity.getName(), "cn");
        assertEquals(identity.getX509Certificate(), cert);
        assertEquals(identity.getX509CertificateSigner(), caCert);
        assertEquals(identity.getAttributes().get("key"), "value");
    }
    
    @Test
    public void testGenerateIdentityNullCert() {
        
        CertSigner certSigner = Mockito.mock(com.yahoo.athenz.common.server.cert.CertSigner.class);
        Mockito.when(certSigner.generateX509Certificate(Mockito.anyString())).thenReturn(null);

        InstanceManager instanceManager = new InstanceManager(null);
        InstanceIdentity identity = instanceManager.generateIdentity(certSigner, "csr", "cn", null);
        assertNull(identity);
    }
    
    @Test
    public void testGenerateIdentityEmptyCert() {
        
        CertSigner certSigner = Mockito.mock(com.yahoo.athenz.common.server.cert.CertSigner.class);
        Mockito.when(certSigner.generateX509Certificate(Mockito.anyString())).thenReturn("");

        InstanceManager instanceManager = new InstanceManager(null);
        InstanceIdentity identity = instanceManager.generateIdentity(certSigner, "csr", "cn", null);
        assertNull(identity);
    }
    
    @Test
    public void testGetX509CertRecordWithCertificate() throws IOException {
        
        InstanceManager instance = new InstanceManager(null);
        
        Path path = Paths.get("src/test/resources/athenz.instanceid.pem");
        String pem = new String(Files.readAllBytes(path));
        X509Certificate cert = Crypto.loadX509Certificate(pem);
        
        CertRecordStore certStore = Mockito.mock(CertRecordStore.class);
        CertRecordStoreConnection certConnection = Mockito.mock(CertRecordStoreConnection.class);
        Mockito.when(certStore.getConnection()).thenReturn(certConnection);
        
        X509CertRecord x509CertRecord = new X509CertRecord();
        Mockito.when(certConnection.getX509CertRecord("ostk", "1001")).thenReturn(x509CertRecord);
        instance.setCertStore(certStore);
        
        X509CertRecord certRecord = instance.getX509CertRecord("ostk", cert);
        assertNotNull(certRecord);
    }
    
    @Test
    public void testGetX509CertRecordWithInstanceId() throws IOException {
        
        InstanceManager instance = new InstanceManager(null);
        
        CertRecordStore certStore = Mockito.mock(CertRecordStore.class);
        CertRecordStoreConnection certConnection = Mockito.mock(CertRecordStoreConnection.class);
        Mockito.when(certStore.getConnection()).thenReturn(certConnection);
        
        X509CertRecord x509CertRecord = new X509CertRecord();
        Mockito.when(certConnection.getX509CertRecord("ostk", "1001")).thenReturn(x509CertRecord);
        instance.setCertStore(certStore);
        
        X509CertRecord certRecord = instance.getX509CertRecord("ostk", "1001");
        assertNotNull(certRecord);
    }
    
    @Test
    public void testGetX509CertRecordNoCertStore() {
        InstanceManager instance = new InstanceManager(null);
        X509CertRecord certRecord = instance.getX509CertRecord("ostk", (X509Certificate) null);
        assertNull(certRecord);
    }
    
    @Test
    public void testGetX509CertRecordNoInstanceId() throws IOException {
        
        InstanceManager instance = new InstanceManager(null);

        Path path = Paths.get("src/test/resources/valid_cn_x509.cert");
        String pem = new String(Files.readAllBytes(path));
        X509Certificate cert = Crypto.loadX509Certificate(pem);
        
        CertRecordStore certStore = Mockito.mock(CertRecordStore.class);
        CertRecordStoreConnection certConnection = Mockito.mock(CertRecordStoreConnection.class);
        Mockito.when(certStore.getConnection()).thenReturn(certConnection);
        
        X509CertRecord x509CertRecord = new X509CertRecord();
        Mockito.when(certConnection.getX509CertRecord("ostk", "1001")).thenReturn(x509CertRecord);
        instance.setCertStore(certStore);

        X509CertRecord certRecord = instance.getX509CertRecord("ostk", cert);
        assertNull(certRecord);
    }
    
    @Test
    public void testGetX509CertRecordNoConnection() throws IOException {
        
        InstanceManager instance = new InstanceManager(null);

        Path path = Paths.get("src/test/resources/athenz.instanceid.pem");
        String pem = new String(Files.readAllBytes(path));
        X509Certificate cert = Crypto.loadX509Certificate(pem);
        
        CertRecordStore certStore = Mockito.mock(CertRecordStore.class);
        Mockito.when(certStore.getConnection()).thenReturn(null);
        instance.setCertStore(certStore);

        X509CertRecord certRecord = instance.getX509CertRecord("ostk", cert);
        assertNull(certRecord);
    }
    
    @Test
    public void testUpdateX509CertRecord() {
        InstanceManager instance = new InstanceManager(null);

        CertRecordStore certStore = Mockito.mock(CertRecordStore.class);
        CertRecordStoreConnection certConnection = Mockito.mock(CertRecordStoreConnection.class);
        Mockito.when(certStore.getConnection()).thenReturn(certConnection);
        
        Mockito.when(certConnection.updateX509CertRecord(Matchers.isA(X509CertRecord.class))).thenReturn(true);
        instance.setCertStore(certStore);

        X509CertRecord x509CertRecord = new X509CertRecord();
        boolean result = instance.updateX509CertRecord(x509CertRecord);
        assertTrue(result);
    }
    
    @Test
    public void testUpdateX509CertRecordNoCertStore() {
        InstanceManager instance = new InstanceManager(null);
        X509CertRecord x509CertRecord = new X509CertRecord();
        boolean result = instance.updateX509CertRecord(x509CertRecord);
        assertFalse(result);
    }
    
    @Test
    public void testUpdateX509CertRecordNoConnection() {
        InstanceManager instance = new InstanceManager(null);

        CertRecordStore certStore = Mockito.mock(CertRecordStore.class);
        Mockito.when(certStore.getConnection()).thenReturn(null);
        instance.setCertStore(certStore);

        X509CertRecord x509CertRecord = new X509CertRecord();
        boolean result = instance.updateX509CertRecord(x509CertRecord);
        assertFalse(result);
    }
    
    @Test
    public void testInsertX509CertRecord() {
        InstanceManager instance = new InstanceManager(null);

        CertRecordStore certStore = Mockito.mock(CertRecordStore.class);
        CertRecordStoreConnection certConnection = Mockito.mock(CertRecordStoreConnection.class);
        Mockito.when(certStore.getConnection()).thenReturn(certConnection);
        
        Mockito.when(certConnection.insertX509CertRecord(Matchers.isA(X509CertRecord.class))).thenReturn(true);
        instance.setCertStore(certStore);

        X509CertRecord x509CertRecord = new X509CertRecord();
        boolean result = instance.insertX509CertRecord(x509CertRecord);
        assertTrue(result);
    }
    
    @Test
    public void testInsertX509CertRecordNoCertStore() {
        InstanceManager instance = new InstanceManager(null);
        X509CertRecord x509CertRecord = new X509CertRecord();
        boolean result = instance.insertX509CertRecord(x509CertRecord);
        assertFalse(result);
    }
    
    @Test
    public void testInsertX509CertRecordNoConnection() {
        InstanceManager instance = new InstanceManager(null);

        CertRecordStore certStore = Mockito.mock(CertRecordStore.class);
        Mockito.when(certStore.getConnection()).thenReturn(null);
        instance.setCertStore(certStore);

        X509CertRecord x509CertRecord = new X509CertRecord();
        boolean result = instance.insertX509CertRecord(x509CertRecord);
        assertFalse(result);
    }
    
    @Test
    public void testDeleteX509CertRecord() {
        InstanceManager instance = new InstanceManager(null);

        CertRecordStore certStore = Mockito.mock(CertRecordStore.class);
        CertRecordStoreConnection certConnection = Mockito.mock(CertRecordStoreConnection.class);
        Mockito.when(certStore.getConnection()).thenReturn(certConnection);
        
        Mockito.when(certConnection.deleteX509CertRecord("provider", "instance")).thenReturn(true);
        instance.setCertStore(certStore);

        boolean result = instance.deleteX509CertRecord("provider", "instance");
        assertTrue(result);
    }
    
    @Test
    public void testDeleteX509CertRecordNoCertStore() {
        InstanceManager instance = new InstanceManager(null);
        boolean result = instance.deleteX509CertRecord("provider", "instance");
        assertFalse(result);
    }
    
    @Test
    public void testDeleteX509CertRecordNoConnection() {
        InstanceManager instance = new InstanceManager(null);

        CertRecordStore certStore = Mockito.mock(CertRecordStore.class);
        Mockito.when(certStore.getConnection()).thenReturn(null);
        instance.setCertStore(certStore);

        boolean result = instance.deleteX509CertRecord("provider", "instance");
        assertFalse(result);
    }
}
