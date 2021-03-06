/**
 * Appia: Group communication and protocol composition framework library
 * Copyright 2006 University of Lisbon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *
 * Initial developer(s): Alexandre Pinto and Hugo Miranda.
 * Contributor(s): See Appia web page for a list of contributors.
 */
 package net.sf.appia.test.broadcast.signature;




import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;

import net.sf.appia.core.Direction;
import net.sf.appia.core.Event;
import net.sf.appia.core.Session;
import net.sf.appia.core.events.SendableEvent;
import net.sf.appia.core.message.Message;
import net.sf.appia.xml.interfaces.InitializableSession;
import net.sf.appia.xml.utils.SessionProperties;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class SignatureSession extends Session implements InitializableSession{
    
    /*
     * Security properties
     */
    private String storeType = "JKS";
    private String keystoreFile=null;
    private char[] keystorePass=null;
    private String publicKeysFile=null;
    private char[] publicKeysPass=null;
	
    private String userAlias;
    
    private KeyStore publicKeyStore;
    private PrivateKey privateKey;
    
    private BASE64Encoder enc;
    private BASE64Decoder dec;


    public SignatureSession(SignatureLayer l) {
    	super(l);
    	enc = new BASE64Encoder();
    	dec = new BASE64Decoder();
    }
    
    /**
     * Initializes the session using the parameters given in the XML configuration.
     * 
     * @param params The parameters given in the XML configuration.
     */
    public void init(SessionProperties props) {
    	if(props.containsKey("user_alias"))
    		userAlias = props.getString("user_alias");
    	if(props.containsKey("store_type"))
    		storeType = props.getString("store_type");
    	if(props.containsKey("keystore_file"))
    		keystoreFile = props.getString("keystore_file");
    	if(props.containsKey("passphrase"))
    		keystorePass = props.getCharArray("keystore_pass");
    	if(props.containsKey("keystore_file"))
    		keystoreFile = props.getString("keystore_file");
    	if(props.containsKey("keystore_pass"))
    		keystorePass = props.getCharArray("keystore_pass");
    	if(props.containsKey("public_keys_file"))
    		publicKeysFile = props.getString("public_keys_file");
    	if(props.containsKey("public_keys_pass"))
    		publicKeysPass = props.getCharArray("public_keys_pass");
    	    	
    	try{
            final KeyStore keyStore = KeyStore.getInstance(storeType);
            keyStore.load(new FileInputStream(keystoreFile), keystorePass);
            
            Key key = keyStore.getKey(userAlias,keystorePass);
            if(key instanceof PrivateKey){
            	privateKey = (PrivateKey)(key);
            }
            
            publicKeyStore = KeyStore.getInstance(storeType);
            publicKeyStore.load(new FileInputStream(publicKeysFile), publicKeysPass);
    	} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void init (String useralias, String keystorefile, String keystorepass, String trustedcertsfile, String trustedcertspass)
    {
    	userAlias = useralias;
    	keystoreFile = keystorefile;
    	keystorePass = keystorepass.toCharArray();
    	publicKeysFile = trustedcertsfile;
    	publicKeysPass = trustedcertspass.toCharArray();
    	
    	try{
            final KeyStore keyStore = KeyStore.getInstance(storeType);
            keyStore.load(new FileInputStream(keystoreFile), keystorePass);
            
            //FIXME for simplicity assuming same password as keystore
            Key key = keyStore.getKey(userAlias,keystorePass);
            if(key instanceof PrivateKey){
            	privateKey = (PrivateKey)(key);
            }
            
            publicKeyStore = KeyStore.getInstance(storeType);
            publicKeyStore.load(new FileInputStream(publicKeysFile), publicKeysPass);
    	} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void handle(Event e) {
    	
    	if(e instanceof SendableEvent) {
        
    		SendableEvent evt = (SendableEvent) e;
    		Message message = evt.getMessage();

    		
    		if(e.getDir() == Direction.DOWN){
    			message.pushString(userAlias);
    			
    			
    			try {
        			String signature = enc.encode(signData(message.toByteArray(), privateKey));
        			message.pushString(signature);
        			e.go();        			
    			} catch(Exception ex){
    				System.err.println("Error on signing outgoing message.");
    				ex.printStackTrace();
    			}
    		} else {
    			String signature = message.popString();
    			String userAlias = message.popString();
    			
    			try{
    				if(verifySignature(message, userAlias, signature, publicKeyStore)){
    					message.pushString(userAlias);
    					message.pushString(signature);
						e.go();
    				}
    			} catch(Exception ex){
    				System.err.println("Error on verifying signature of ingoing message.");
    				ex.printStackTrace();
    			}

    		}
    	}
    }
	
	public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
		Signature signer = Signature.getInstance("SHA1withRSA");
		signer.initSign(key);
		signer.update(data);
		return (signer.sign());
	}

	public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
		Signature signer = Signature.getInstance("SHA1withRSA");
		signer.initVerify(key);
		signer.update(data);
		return (signer.verify(sig));
	}
	
	public static boolean verifySignature(Message message, String userAlias, String signature, KeyStore trustedStore) throws Exception
	{
		boolean verified = false;
		
		BASE64Decoder dec = new BASE64Decoder();
		if(trustedStore.containsAlias(userAlias)){
			Certificate userCert = trustedStore.getCertificate(userAlias);
			message.pushString(userAlias);
			if(verifySig(message.toByteArray(), userCert.getPublicKey(), dec.decodeBuffer(signature))){
				verified = true;
			} else {
				System.err.println("Failure on verifying signature of user " + userAlias + ".");
			}
			message.popString();
		} else {
			System.err.println("Message from untrusted user: " + userAlias + ".");
		}
		
		return verified;
	}
}
